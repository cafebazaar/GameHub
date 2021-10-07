package com.farsitel.bazaar.games;


public class GamesServiceConnection implements ServiceConnection {
    public static int STATUS_SUCCESS = 0;
    public static int STATUS_DISCONNECT = 1;
    public static int STATUS_NEEDS_UPDATE = 2;
    public static int STATUS_FAILURE = 3;
    public static int STATUS_UNKNOWN = 4;

    private final Context context;
    private final IConnectionCallback callback;
    private GameService service;

    public GamesServiceConnection(IConnectionCallback iUnityCallbacks, Context context, IConnectionCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void onServiceConnected(ComponentName name, IBinder boundService) {
        try {
            service = GameService.Stub.asInterface(boundService);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFinish(STATUS_FAILURE, "Bazaar Game Service connection failed.", e.getMessage());
        }

        Log.e(GameServiceBridge.TAG, "onServiceConnected(): Connected");
        callback.onFinish(STATUS_SUCCESS, "Bazaar Game Service connected.", "");

    }

    public void onServiceDisconnected(ComponentName name) {
        service = null;
        callback.onFinish(STATUS_DISCONNECT, "Bazaar Game Service disconnected.", "");
        Log.i(GameServiceBridge.TAG, "onServiceDisconnected(): Disconnected");
    }

    public boolean needsUpdate() {
        return true;
    }

    public Bundle startTournamentMatch(ITournamentMatchCallback callback, String matchId, String metaData) {
        try {
            Bundle bundle = service.startTournamentMatch(context.getPackageName(), matchId, metaData);
        } catch (RemoteException e) {
            callback.onFinish(STATUS_UNKNOWN, "Error on startTournamentMatch", e.getMessage());
            e.printStackTrace();
        }
        callback.onFinish(STATUS_SUCCESS, "no data", "");
        return null;
    }

    public Bundle endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient) {
        try {
            Bundle bundle = service.endTournamentMatch(sessionId, coefficient);
        } catch (RemoteException e) {
            callback.onFinish(STATUS_UNKNOWN, "Error on startTournamentMatch", e.getMessage());
            e.printStackTrace();
        }

        callback.onFinish(STATUS_SUCCESS, "no data", "");
        return null;
    }

    public void showLastTournamentLeaderboard() {
        try {
            service.showLastTournamentLeaderboard();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}