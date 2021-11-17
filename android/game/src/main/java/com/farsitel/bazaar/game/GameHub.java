package com.farsitel.bazaar.game;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.IRankingCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;
import com.farsitel.bazaar.game.constants.Constant;
import com.farsitel.bazaar.game.constants.Key;
import com.farsitel.bazaar.game.constants.Method;
import com.farsitel.bazaar.game.constants.Param;
import com.farsitel.bazaar.game.data.RankItem;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.data.Tournament;
import com.farsitel.bazaar.game.utils.Logger;
import com.farsitel.bazaar.game.utils.MainThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameHub {
    private static GameHub instance;

    private final Logger logger;
    private boolean isDisposed = false;
    private boolean isBroadcastMode;
    private IGameHub gameHubService;
    private BroadcastService gameHubBroadcast;
    private ExecutorService executorService;

    public GameHub() {
        logger = new Logger();
        executorService = Executors.newSingleThreadExecutor();
    }

    boolean disposed() {
        return isDisposed;
    }

    public static GameHub getInstance() {
        if (instance == null) {
            instance = new GameHub();
        }
        return instance;
    }

    public String getVersion() {
        return BuildConfig.GAMEHUB_VERSION;
    }

    public void connect(Context context, boolean showPrompts, IConnectionCallback callback) {
        // Check player has CafeBazaar app or  it`s already updated to the latest version
        Result installState = getBazaarInstallationState(context, showPrompts);
        if (installState.status != Status.SUCCESS) {
            callback.onFinish(installState.status.getLevelCode(), installState.message, "");
            return;
        }

        logger.logDebug("GameHub service started.");
        ServiceConnection gameHubConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger.logDebug("GameHub service disconnected.");
                gameHubService = null;
                callback.onFinish(Status.DISCONNECTED.getLevelCode(), "GameHub service disconnected.", "");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (disposed()) return;
                logger.logDebug("GameHub service connected.");
                gameHubService = IGameHub.Stub.asInterface(service);
                // Check login to cafebazaar
                getLoginState(result -> {
                    if (result.status == Status.SUCCESS) {
                        callback.onFinish(result.getLevelCode(), "GameHub service connected.", "");
                    } else {
                        if (showPrompts) {
                            showLoginPrompt(context);
                        }
                    }
                });
            }
        };

        // Bind to bazaar game hub
        Intent serviceIntent = new Intent("com.farsitel.bazaar.Game.BIND");
        serviceIntent.setPackage(Constant.BAZAAR_PACKAGE_NAME);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> intentServices = pm.queryIntentServices(serviceIntent, 0);
        if (!intentServices.isEmpty()) {
            // service available to handle that Intent
            isBroadcastMode = !context.bindService(serviceIntent, gameHubConnection, Context.BIND_AUTO_CREATE);
            if (isBroadcastMode) {
                gameHubBroadcast = new BroadcastService(context, logger);
            }
        }
    }

    boolean areServicesUnavailable() {
        return gameHubService != null && gameHubBroadcast != null;
    }

    public void getLoginState(IBroadcastCallback callback) {
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
            });
        });
    }

    void showLoginPrompt(Context context) {
        startActionViewIntent(context, Constant.BAZAAR_LOGIN_URL, Constant.BAZAAR_PACKAGE_NAME);
    }

    void startActionViewIntent(Context context, String uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(uri));
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        context.startActivity(intent);
    }

    Result getBazaarInstallationState(Context context, boolean showPrompts) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(Constant.BAZAAR_PACKAGE_NAME, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageInfo == null) {
            if (showPrompts) {
                startActionViewIntent(context, Constant.BAZAAR_INSTALL_URL, null);
            }
            return new Result(Status.INSTALL_CAFEBAZAAR, "Install cafebazaar to support GameHub!");
        }
        if (packageInfo.versionCode < Constant.MINIMUM_BAZAAR_VERSION) {
            if (showPrompts) {
                startActionViewIntent(context, Constant.BAZAAR_DETAILS_URL, Constant.BAZAAR_PACKAGE_NAME);
            }
            return new Result(Status.UPDATE_CAFEBAZAAR, "Install new version of cafebazaar to support GameHub!");
        }
        return new Result(Status.SUCCESS, "");
    }

    public void getTournaments(Activity activity, ITournamentsCallback callback) {
        logger.logDebug("Call " + Method.GET_TOURNAMENTS);

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
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

        long startAt = result.extras.containsKey(Param.START_TIMESTAMP) ? result.extras.getLong(Param.START_TIMESTAMP) : 0;
        long endAt = result.extras.containsKey(Param.END_TIMESTAMP) ? result.extras.getLong(Param.END_TIMESTAMP) : 0;
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(new Tournament(startAt, endAt));
        callback.onFinish(Status.SUCCESS.getLevelCode(), "Get Tournaments", "", tournaments);
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback
            callback, String matchId, String metadata) {
        logger.logDebug("Call " + Method.START_TOURNAMENT_MATCH);

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
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
        String sessionId = result.extras.containsKey(Param.SESSION_ID) ? result.extras.getString(Param.SESSION_ID) : Param.SESSION_ID;
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metadata);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId,
                                   float score) {
        logger.logDebug("Call " + Method.END_TOURNAMENT_MATCH);

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
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
        String matchId = result.extras.containsKey(Param.MATCH_ID) ? result.extras.getString(Param.MATCH_ID) : Param.MATCH_ID;
        String metaData = result.extras.containsKey(Param.META_DATA) ? result.extras.getString(Param.META_DATA) : Param.META_DATA;
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metaData);
    }

    public void showTournamentRanking(Context context, String tournamentId, IConnectionCallback callback) {
        logger.logDebug("Call showTournamentRanking");

        // Check player has CafeBazaar app or  it`s already updated to the latest version
        Result result = getBazaarInstallationState(context, true);
        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                callback.onFinish(loginResult.status.getLevelCode(), loginResult.message, loginResult.stackTrace);
                showLoginPrompt(context);
                return;
            }

            result.message = "Last tournament ranking table shown.";
            String data = Constant.BAZAAR_TOURNAMENT_URL + tournamentId;
            try {
                startActionViewIntent(context, data, Constant.BAZAAR_PACKAGE_NAME);
            } catch (Exception e) {
                callback.onFinish(Status.UPDATE_CAFEBAZAAR.getLevelCode(), "Get Ranking-data needs to new version of CafeBazaar!", Arrays.toString(e.getStackTrace()));
                return;
            }
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace);
        });
    }

    public void getTournamentRanking(Context context, String tournamentId, IRankingCallback
            callback) {
        logger.logDebug("Call getTournamentRanking");

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
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
        String jsonString = result.extras.getString(Key.LEADERBOARD_DATA);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray participants = jsonObject.optJSONArray(Key.PARTICIPANTS);
            int participantsCount = participants != null ? participants.length() : 0;
            for (int i = 0; i < participantsCount; i++) {
                JSONObject obj = participants.getJSONObject(i);
                rankItems.add(new RankItem(obj.getString(Key.NICKNAME),
                        obj.getString(Key.SCORE),
                        obj.getString(Key.AWARD),
                        obj.getBoolean(Key.HAS_FOLLOWING_ELLIPSIS),
                        obj.getBoolean(Key.IS_CURRENT_USER),
                        obj.getBoolean(Key.IS_WINNER)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFinish(Status.FAILURE.getLevelCode(), "Error on Ranking data parsing!", "", null);
            return;
        }

        callback.onFinish(result.status.getLevelCode(), "getTournamentRanking", jsonString, rankItems);
    }

    public void dispose() {
        isDisposed = true;

        if (gameHubBroadcast != null) {
            gameHubBroadcast.dispose();
            gameHubBroadcast = null;
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }
}