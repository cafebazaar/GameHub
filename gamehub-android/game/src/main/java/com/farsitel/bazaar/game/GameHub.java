package com.farsitel.bazaar.game;

import static com.farsitel.bazaar.game.constants.Constant.BAZAAR_GAME_PACKAGE_NAME;
import static com.farsitel.bazaar.game.constants.Constant.BROADCAST_IS_CREATED;
import static com.farsitel.bazaar.game.constants.Constant.CONNECT_TO_SERVICE_FIRST;
import static com.farsitel.bazaar.game.constants.Constant.GET_RANKING_NEEDS_UPDATE_BAZAAR;
import static com.farsitel.bazaar.game.constants.Constant.INSTALL_BAZAAR;
import static com.farsitel.bazaar.game.constants.Constant.LOGIN_TO_BAZAAR_FIRST;
import static com.farsitel.bazaar.game.constants.Constant.REQUIRED_BAZAAR_VERSION_FOR_EVENT;
import static com.farsitel.bazaar.game.constants.Constant.REQUIRED_BAZAAR_VERSION_FOR_GAMEHUB;
import static com.farsitel.bazaar.game.constants.Constant.REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT;
import static com.farsitel.bazaar.game.constants.Constant.SERVICE_IS_CONNECTED;
import static com.farsitel.bazaar.game.constants.Constant.SERVICE_IS_DISCONNECTED;
import static com.farsitel.bazaar.game.constants.Constant.SERVICE_IS_STARTED;
import static com.farsitel.bazaar.game.constants.Constant.TOURNAMENT_RANKING_IS_SHOWN;
import static com.farsitel.bazaar.game.constants.Constant.UPDATE_BAZAAR;

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
import com.farsitel.bazaar.game.callbacks.IEventDoneCallback;
import com.farsitel.bazaar.game.callbacks.IGetEventsCallback;
import com.farsitel.bazaar.game.callbacks.IRankingCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;
import com.farsitel.bazaar.game.constants.Constant;
import com.farsitel.bazaar.game.constants.Key;
import com.farsitel.bazaar.game.constants.Method;
import com.farsitel.bazaar.game.constants.Param;
import com.farsitel.bazaar.game.data.Event;
import com.farsitel.bazaar.game.data.Match;
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
        // Check player has Bazaar app or  it`s already updated to the latest version
        Result installState = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_GAMEHUB, showPrompts);
        if (installState.status != Status.SUCCESS) {
            callback.onFinish(installState.status.getLevelCode(), installState.message, "");
            return;
        }

        logger.logDebug(SERVICE_IS_STARTED);
        ServiceConnection gameHubConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger.logDebug(SERVICE_IS_DISCONNECTED);
                gameHubService = null;
                callback.onFinish(Status.DISCONNECTED.getLevelCode(), SERVICE_IS_DISCONNECTED, "");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (isDisposed) return;
                logger.logDebug(SERVICE_IS_DISCONNECTED);
                gameHubService = IGameHub.Stub.asInterface(service);
                connectionCallback(context, showPrompts, callback);
            }
        };

        // Bind to bazaar game hub
        Intent serviceIntent = new Intent(BAZAAR_GAME_PACKAGE_NAME);
        serviceIntent.setPackage(Constant.BAZAAR_PACKAGE_NAME);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> intentServices = pm.queryIntentServices(serviceIntent, 0);
        if (!intentServices.isEmpty()) {
            // service available to handle that Intent
            try {
                isBroadcastMode = !context.bindService(serviceIntent, gameHubConnection, Context.BIND_AUTO_CREATE);

            } catch (Throwable ignore) {
                isBroadcastMode = true;
            }
            if (isBroadcastMode) {
                gameHubBroadcast = new BroadcastService(context);
                logger.logDebug(BROADCAST_IS_CREATED);
                connectionCallback(context, showPrompts, callback);
            }
        }
    }

    public void getLoginState(IBroadcastCallback callback) {
        final Result result = new Result(Status.SUCCESS, "");
        if (gameHubService == null && gameHubBroadcast == null) {
            result.status = Status.DISCONNECTED;
            result.message = CONNECT_TO_SERVICE_FIRST;
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
                result.status = Status.LOGIN_BAZAAR;
                result.message = LOGIN_TO_BAZAAR_FIRST;
            } catch (Exception e) {
                e.printStackTrace();
                result.status = Status.FAILURE;
                result.message = e.getMessage();
                result.stackTrace = Arrays.toString(e.getStackTrace());
            }
            MainThread.run(() -> callback.call(result));
        });
    }

    public void getTournaments(Context context, ITournamentsCallback callback) {
        logger.logDebug("Call " + Method.GET_TOURNAMENTS);

        // Check if the CafeBazaar version matches minimum required version
        Result installationState = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT, true);
        if (installationState.status != Status.SUCCESS) {
            callback.onFinish(installationState.status.getLevelCode(), installationState.message, installationState.stackTrace, null);
            return;
        }

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                tournamentsCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.getTournamentTimes(tournamentResult -> tournamentsCallback(tournamentResult, callback));
                return;
            }

            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.getTournamentTimes(context.getPackageName());
                    result.setBundle(bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> tournamentsCallback(result, callback));
            });
        });
    }

    public void startTournamentMatch(
            Context context,
            ITournamentMatchCallback callback,
            String matchId,
            String metadata
    ) {
        logger.logDebug("Call " + Method.START_TOURNAMENT_MATCH);

        // Check if the CafeBazaar version matches minimum required version
        Result installationState = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT, true);
        if (installationState.status != Status.SUCCESS) {
            callback.onFinish(installationState.status.getLevelCode(), installationState.message, installationState.stackTrace, null);
            return;
        }

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                startTournamentMatchCallback(loginResult, callback, matchId, metadata);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.startTournamentMatch(matchId, metadata, startResult ->
                        startTournamentMatchCallback(startResult, callback, matchId, metadata)
                );
                return;
            }
            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.startTournamentMatch(context.getPackageName(), matchId, metadata);
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

    public void endTournamentMatch(
            Context context,
            ITournamentMatchCallback callback,
            String sessionId,
            float score
    ) {
        logger.logDebug("Call " + Method.END_TOURNAMENT_MATCH);

        // Check if the CafeBazaar version matches minimum required version
        Result installationState = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT, true);
        if (installationState.status != Status.SUCCESS) {
            callback.onFinish(installationState.status.getLevelCode(), installationState.message, installationState.stackTrace, null);
            return;
        }

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        if (isBroadcastMode) {
            gameHubBroadcast.endTournamentMatch(sessionId, score, endResult ->
                    endTournamentMatchCallback(endResult, callback, sessionId)
            );
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

    public void showTournamentRanking(Context context, String tournamentId, IConnectionCallback callback) {
        logger.logDebug("Call showTournamentRanking");

        // Check player has Bazaar app or  it`s already updated to the latest version
        Result result = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT, true);
        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                callback.onFinish(
                        loginResult.status.getLevelCode(),
                        loginResult.message,
                        loginResult.stackTrace
                );
                if (result.status == Status.LOGIN_BAZAAR) {
                    showLoginPrompt(context);
                }
                return;
            }

            result.message = TOURNAMENT_RANKING_IS_SHOWN;
            String data = Constant.BAZAAR_TOURNAMENT_URL + context.getPackageName();
            logger.logDebug(data);
            try {
                startActionViewIntent(context, data, Constant.BAZAAR_PACKAGE_NAME);
            } catch (Exception e) {
                callback.onFinish(
                        Status.UPDATE_BAZAAR.getLevelCode(),
                        GET_RANKING_NEEDS_UPDATE_BAZAAR,
                        Arrays.toString(e.getStackTrace())
                );
                return;
            }
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace);
        });
    }

    public void getTournamentRanking(
            Context context,
            String tournamentId,
            IRankingCallback callback
    ) {
        logger.logDebug("Call getTournamentRanking");

        // Check if the CafeBazaar version matches minimum required version
        Result installationState = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_TOURNAMENT, true);
        if (installationState.status != Status.SUCCESS) {
            callback.onFinish(installationState.status.getLevelCode(), installationState.message, installationState.stackTrace, null);
            return;
        }

        // Check one of services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                getTournamentRankingCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.getCurrentLeaderboard(rankResult ->
                        getTournamentRankingCallback(rankResult, callback)
                );
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

    public void eventDoneNotify(
            Context context,
            String eventId,
            IEventDoneCallback callback
    ) {
        logger.logDebug("Call eventDoneNotify");

        // Check if one of the services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        // Check if Bazaar update is needed
        Result installationResult = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_EVENT, true);
        if (installationResult.status != Status.SUCCESS) {
            callback.onFinish(
                    installationResult.status.getLevelCode(),
                    installationResult.message,
                    installationResult.stackTrace,
                    null
            );
            return;
        }

        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                eventDoneNotifyCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.eventDoneNotify(
                        eventId, notifyResult -> eventDoneNotifyCallback(notifyResult, callback)
                );
                return;
            }

            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.eventDoneNotify(eventId);
                    result.setBundle(bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> eventDoneNotifyCallback(result, callback));
            });
        });
    }

    public void getEvents(
            Context context,
            IGetEventsCallback callback
    ) {
        logger.logDebug("Call getEvents");

        // Check if one of the services is connected
        if (areServicesUnavailable()) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), CONNECT_TO_SERVICE_FIRST, "", null);
            return;
        }

        // Check if Bazaar update is needed
        Result installationResult = getBazaarInstallationState(context, REQUIRED_BAZAAR_VERSION_FOR_EVENT, true);
        if (installationResult.status != Status.SUCCESS) {
            callback.onFinish(
                    installationResult.status.getLevelCode(),
                    installationResult.message,
                    installationResult.stackTrace,
                    null
            );
            return;
        }
        String packageName = context.getPackageName();
        // Check player is already logged-in
        getLoginState(loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                getEventsCallback(loginResult, callback);
                return;
            }

            if (isBroadcastMode) {
                gameHubBroadcast.getEventsByPackageName(
                        packageName, eventsResult -> getEventsCallback(eventsResult, callback)
                );
                return;
            }

            executorService.submit(() -> {
                final Result result = new Result();
                try {
                    Bundle bundle = gameHubService.getEventsByPackageName(packageName);
                    result.setBundle(bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    result.status = Status.FAILURE;
                    result.message = e.getMessage();
                    result.stackTrace = Arrays.toString(e.getStackTrace());
                }
                MainThread.run(() -> getEventsCallback(result, callback));
            });
        });
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

    void connectionCallback(Context context, boolean showPrompts, IConnectionCallback callback) {
        // Check login to Bazaar
        getLoginState(loginResult -> {
            if (loginResult.status == Status.SUCCESS) {
                callback.onFinish(loginResult.status.getLevelCode(), SERVICE_IS_CONNECTED, "");
            } else {
                callback.onFinish(loginResult.status.getLevelCode(), loginResult.message, loginResult.stackTrace);
                if (showPrompts) {
                    showLoginPrompt(context);
                }
            }
        });
    }

    boolean areServicesUnavailable() {
        return gameHubService != null && gameHubBroadcast != null;
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

    Result getBazaarInstallationState(
            Context context,
            int requiredBazaarVersion,
            boolean showPrompts
    ) {
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
            return new Result(Status.INSTALL_BAZAAR, INSTALL_BAZAAR);
        }
        if (packageInfo.versionCode < requiredBazaarVersion) {
            if (showPrompts) {
                startActionViewIntent(context, Constant.BAZAAR_DETAILS_URL, Constant.BAZAAR_PACKAGE_NAME);
            }
            return new Result(Status.UPDATE_BAZAAR, UPDATE_BAZAAR);
        }
        return new Result(Status.SUCCESS, "");
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

    void startTournamentMatchCallback(
            Result result,
            ITournamentMatchCallback callback,
            String matchId,
            String metadata
    ) {
        logger.logDebug("Start: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String sessionId = result.extras.containsKey(Param.SESSION_ID) ? result.extras.getString(Param.SESSION_ID) : Param.SESSION_ID;
        callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, new Match(matchId, sessionId, metadata));
    }

    void endTournamentMatchCallback(
            Result result,
            ITournamentMatchCallback callback,
            String sessionId
    ) {
        logger.logDebug("End: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String matchId = result.extras.containsKey(Param.MATCH_ID) ? result.extras.getString(Param.MATCH_ID) : Param.MATCH_ID;
        String metaData = result.extras.containsKey(Param.META_DATA) ? result.extras.getString(Param.META_DATA) : Param.META_DATA;
        callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, new Match(matchId, sessionId, metaData));
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

    void eventDoneNotifyCallback(Result result, IEventDoneCallback callback) {
        logger.logDebug("event done: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String eventDoneTime = result.extras.containsKey(Param.EVENT_DONE_TIMESTAMP) ?
                result.extras.getString(Param.EVENT_DONE_TIMESTAMP) : "";
        callback.onFinish(result.status.getLevelCode(), "Event done notify", "", eventDoneTime);
    }

    void getEventsCallback(Result result, IGetEventsCallback callback) {
        logger.logDebug("get events: " + result.toString());

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }

        List<Event> eventList = new ArrayList<>();
        String jsonString = result.extras.getString(Key.EVENTS);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray events = jsonObject.optJSONArray(Key.EVENTS);
            int eventsCount = events != null ? events.length() : 0;
            for (int i = 0; i < eventsCount; i++) {
                JSONObject obj = events.getJSONObject(i);
                eventList.add(new Event(
                        obj.getString(Key.EVENT_ID),
                        obj.getString(Key.START_TIMESTAMP),
                        obj.getString(Key.END_TIMESTAMP)
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFinish(Status.FAILURE.getLevelCode(), "Error on events data parsing!", "", null);
            return;
        }

        callback.onFinish(result.status.getLevelCode(), "Get events by packageName", "", eventList);
    }
}