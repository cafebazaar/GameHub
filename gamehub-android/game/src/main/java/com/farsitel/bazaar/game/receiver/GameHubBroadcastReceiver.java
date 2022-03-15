package com.farsitel.bazaar.game.receiver;

import static com.farsitel.bazaar.game.constants.Constant.LOGIN_TO_BAZAAR_FIRST;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.constants.Constant;
import com.farsitel.bazaar.game.constants.Method;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.Logger;

import java.util.Map;
import java.util.WeakHashMap;

public class GameHubBroadcastReceiver extends BroadcastReceiver {

    private boolean mDisposed;
    private final Logger logger = new Logger();
    public final Map<String, IBroadcastCallback> callbacks = new WeakHashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.logDebug(intent.toUri(0));
        String action = intent.getAction();
        if (action == null) {
            logger.logError("action is null.");
            return;
        }

        if (disposed()) {
            logger.logError("Broadcast already disposed.");
            return;
        }

        if (callbacks.containsKey(action)) {
            Result result = new Result();
            IBroadcastCallback callback = callbacks.get(action);
            if (callback == null) {
                return;
            }
            if (action.equals(getAction(Method.IS_LOGIN))) {
                boolean isLogin = intent.getBooleanExtra(Method.IS_LOGIN, false);
                result.status = isLogin ? Status.SUCCESS : Status.LOGIN_BAZAAR;
                result.message = isLogin ? "" : LOGIN_TO_BAZAAR_FIRST;
                callback.call(result);
            } else {
                callback.call(result.setBundle(intent.getExtras()));
            }
            callbacks.remove(action);
        }
    }

    public static String getAction(String methodName) {
        return Constant.BAZAAR_PACKAGE_NAME + "." + methodName;
    }

    public void dispose() {
        callbacks.clear();
        mDisposed = true;
    }

    private boolean disposed() {
        return mDisposed;
    }
}
