package com.farsitel.bazaar.game;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.callbacks.IRankingCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;
import com.farsitel.bazaar.game.data.RankItem;
import com.farsitel.bazaar.game.data.Tournament;
import com.farsitel.bazaar.game.utils.Logger;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.MainThread;
import com.farsitel.bazaar.game.utils.ParamNames;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameHubBridge extends AbstractGameHub {
    private static GameHubBridge instance;

    private boolean isBroadcastMode;
    private IGameHub gameHubService;
    private GameHubBroadcast gameHubBroadcast;
    private ServiceConnection gameHubConnection;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public GameHubBridge() {
        super(new Logger());
    }

    public static GameHubBridge getInstance() {
        if (instance == null) {
            instance = new GameHubBridge();
        }
        return instance;
    }

    public String getVersion() {
        return BuildConfig.GAMEHUB_VERSION;
    }

    @Override
    public void connect(Context context, boolean showPrompts, IConnectionCallback callback) {
        // Check player has CafeBazaar app or  it`s already updated to the latest version
        connectionState = isCafebazaarInstalled(context, showPrompts);
        if (connectionState.status != Status.SUCCESS) {
            connectionState.call(callback);
            return;
        }

        logger.logDebug("GameHub service started.");
        gameHubConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger.logDebug("GameHub service disconnected.");
                gameHubService = null;
                connectionState = new Result(Status.DISCONNECTED, "GameHub service disconnected.", "");
                connectionState.call(callback);
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (disposed()) return;
                logger.logDebug("GameHub service connected.");
                gameHubService = IGameHub.Stub.asInterface(service);
                connectionState.status = Status.SUCCESS;
                // Check login to cafebazaar
                isLogin(context, showPrompts, result -> {
                    connectionState = result;
                    if (connectionState.status == Status.SUCCESS) {
                        connectionState.message = "GameHub service connected.";
                        connectionState.call(callback);
                    }
                });
            }
        };

        // Bind to bazaar game hub
        Intent serviceIntent = new Intent("com.farsitel.bazaar.Game.BIND");
        serviceIntent.setPackage(BAZAAR_PACKAGE_NAME);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> intentServices = pm.queryIntentServices(serviceIntent, 0);
        if (!intentServices.isEmpty()) {
            // service available to handle that Intent
            isBroadcastMode = !context.bindService(serviceIntent, gameHubConnection, Context.BIND_AUTO_CREATE);
            if (isBroadcastMode) {
                gameHubBroadcast = new GameHubBroadcast(context, logger);
            }
        }
    }

    public Result areServicesAvailable() {
        boolean isAvailable = gameHubService != null || gameHubBroadcast != null;
        return new Result(isAvailable ? Status.SUCCESS : Status.DISCONNECTED, isAvailable ? "" : "Connect to service before!");
    }

    @Override
    public void isLogin(Context context, boolean showPrompts, IBroadcastCallback callback) {
        final Result result = new Result(Status.SUCCESS, "");
        if (gameHubService == null && gameHubBroadcast == null) {
            result.status = Status.DISCONNECTED;
            result.message = "Connect to service before!";
            callback.call(result);
            return;
        }

        if (isBroadcastMode) {
            gameHubBroadcast.isLogin(callback);
            return;
        }
        executorService.submit(() -> {
            try {
                if (gameHubService.isLogin()) {
                    callback.call(result);
                    return;
                }
                result.status = Status.LOGIN_CAFEBAZAAR;
                result.message = "Login to Cafebazaar before!";
            } catch (Exception e) {
                e.printStackTrace();
                result.status = Status.FAILURE;
                result.message = e.getMessage();
                result.stackTrace = Arrays.toString(e.getStackTrace());
            }
            MainThread.run(() -> {
                callback.call(result);
                if (showPrompts) {
                    startActionViewIntent(context, "bazaar://login", BAZAAR_PACKAGE_NAME);
                }
            });
        });
    }

    public void getTournaments(Activity activity, ITournamentsCallback callback) {
        logger.logDebug("Call getTournaments");


        // Check one of services is connected
        Result serviceResult = areServicesAvailable();
        if (serviceResult.status != Status.SUCCESS) {
            serviceResult.call(callback);
            return;
        }

        // Check player is already logged-in
        isLogin(activity, false, loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                tournamentsCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.getTournamentTimes(tournamentResult -> {
                    tournamentsCallback(tournamentResult, callback);
                });
                return;
            }
            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.getTournamentTimes(activity.getPackageName());
                    result.setBundle(bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> {
                    tournamentsCallback(result, callback);
                });
            });
        });
    }

    void tournamentsCallback(Result result, ITournamentsCallback callback) {
        logger.logDebug("Tournaments: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }

        long startAt = result.extras.containsKey(ParamNames.START_TIMESTAMP) ? result.extras.getLong(ParamNames.START_TIMESTAMP) : 0;
        long endAt = result.extras.containsKey(ParamNames.END_TIMESTAMP) ? result.extras.getLong(ParamNames.END_TIMESTAMP) : 0;
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(new Tournament("-1", "Tournament -1", startAt, endAt));
        callback.onFinish(Status.SUCCESS.getLevelCode(), "Get Tournaments", "", tournaments);
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback
            callback, String matchId, String metadata) {
        logger.logDebug("Call startTournamentMatch");

        // Check one of services is connected
        Result serviceResult = areServicesAvailable();
        if (serviceResult.status != Status.SUCCESS) {
            serviceResult.call(callback);
            return;
        }

        // Check player is already logged-in
        isLogin(activity, false, loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                startTournamentMatchCallback(loginResult, callback, matchId, metadata);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.startTournamentMatch(matchId, metadata, startResult -> {
                    startTournamentMatchCallback(startResult, callback, matchId, metadata);
                });
                return;
            }
            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.startTournamentMatch(activity.getPackageName(), matchId, metadata);
                    result.setBundle(bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> startTournamentMatchCallback(result, callback, matchId, metadata));
            });
        });
    }

    void startTournamentMatchCallback(Result result, ITournamentMatchCallback callback, String matchId, String metadata) {
        logger.logDebug("Start: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String sessionId = result.extras.containsKey(ParamNames.SESSION_ID) ? result.extras.getString(ParamNames.SESSION_ID) : ParamNames.SESSION_ID;
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metadata);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId,
                                   float score) {
        logger.logDebug("Call endTournamentMatch");

        // Check one of services is connected
        Result serviceResult = areServicesAvailable();
        if (serviceResult.status != Status.SUCCESS) {
            serviceResult.call(callback);
            return;
        }

        if (isBroadcastMode) {
            gameHubBroadcast.endTournamentMatch(sessionId, score, endResult -> {
                endTournamentMatchCallback(endResult, callback, sessionId);
            });
            return;
        }
        executorService.submit(() -> {
            final Result result = new Result();
            try {
                Bundle bundle = gameHubService.endTournamentMatch(sessionId, score);
                result.setBundle(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
                result.status = Status.FAILURE;
                result.message = e.getMessage();
                result.stackTrace = Arrays.toString(e.getStackTrace());
            }
            MainThread.run(() -> endTournamentMatchCallback(result, callback, sessionId));
        });
    }

    void endTournamentMatchCallback(Result result, ITournamentMatchCallback callback, String sessionId) {
        logger.logDebug("End: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String matchId = result.extras.containsKey(ParamNames.MATCH_ID) ? result.extras.getString(ParamNames.MATCH_ID) : ParamNames.MATCH_ID;
        String metaData = result.extras.containsKey(ParamNames.META_DATA) ? result.extras.getString(ParamNames.META_DATA) : ParamNames.META_DATA;
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metaData);
    }

    public void showTournamentRanking(Context context, String tournamentId, IConnectionCallback callback) {
        logger.logDebug("Call showTournamentRanking");

        // Check player has CafeBazaar app or  it`s already updated to the latest version
        connectionState = isCafebazaarInstalled(context, true);
        if (connectionState.status != Status.SUCCESS) {
            connectionState.call(callback);
            return;
        }

        // Check player is already logged-in
        isLogin(context, true, loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                callback.onFinish(loginResult.status.getLevelCode(), loginResult.message, loginResult.stackTrace);
                return;
            }

            connectionState.message = "Last tournament ranking table shown.";
            String data = "bazaar://tournament_leaderboard?package_name=" + context.getPackageName();
            logger.logInfo(data);
            try {
                startActionViewIntent(context, data, BAZAAR_PACKAGE_NAME);
            } catch (Exception e) {
                callback.onFinish(Status.UPDATE_CAFEBAZAAR.getLevelCode(), "Get Ranking-data needs to new version of CafeBazaar!", Arrays.toString(e.getStackTrace()));
                return;
            }
            connectionState.call(callback);
        });
    }

    @Override
    public void getTournamentRanking(Context context, String tournamentId, IRankingCallback
            callback) {
        logger.logDebug("Call getTournamentRanking");

        // Check one of services is connected
        Result serviceResult = areServicesAvailable();
        if (serviceResult.status != Status.SUCCESS) {
            serviceResult.call(callback);
            return;
        }

        // Check player is already logged-in
        isLogin(context, false, loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                getTournamentRankingCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.getCurrentLeaderboard(rankResult -> {
                    getTournamentRankingCallback(rankResult, callback);
                });
                return;
            }
            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.getCurrentLeaderboard(context.getPackageName());
                    result.setBundle(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> getTournamentRankingCallback(result, callback));
            });
        });
    }

    void getTournamentRankingCallback(Result result, IRankingCallback callback) {
        logger.logDebug("Ranking: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }

        List<RankItem> rankItems = new ArrayList<>();
        JSONArray participants = null;
        String jsonString = result.extras.getString(ParamNames.LEADERBOARD_DATA);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            participants = jsonObject.optJSONArray(ParamNames.PARTICIPANTS);
            int participantsCount = participants != null ? participants.length() : 0;
            for (int i = 0; i < participantsCount; i++) {
                JSONObject obj = participants.getJSONObject(i);
                rankItems.add(new RankItem(obj.getString(ParamNames.NICKNAME),
                        obj.getString(ParamNames.SCORE),
                        obj.getString(ParamNames.AWARD),
                        obj.getBoolean(ParamNames.HAS_FOLLOWING_ELLIPSIS),
                        obj.getBoolean(ParamNames.IS_CURRENT_USER),
                        obj.getBoolean(ParamNames.IS_WINNER)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFinish(Status.FAILURE.getLevelCode(), "Error on Ranking data parsing!", "", null);
            return;
        }

        callback.onFinish(result.status.getLevelCode(), "getTournamentRanking", jsonString, rankItems);
    }

    @Override
    public boolean disposed() {
        if (gameHubBroadcast != null) {
            gameHubBroadcast.dispose();
        }

        if (executorService != null) {
            executorService.shutdown();
        }

        return super.disposed();
    }
}