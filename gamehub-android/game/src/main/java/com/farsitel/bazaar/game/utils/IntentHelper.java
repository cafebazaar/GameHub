package com.farsitel.bazaar.game.utils;

import static com.farsitel.bazaar.game.constants.Constant.BAZAAR_LOGIN_URL;
import static com.farsitel.bazaar.game.constants.Constant.BAZAAR_PACKAGE_NAME;
import static com.farsitel.bazaar.game.constants.Constant.EVENT_DONE_NOTIFY_REQUEST_CODE;
import static com.farsitel.bazaar.game.constants.Constant.KEY_LOGIN_REQUEST_CODE;
import static com.farsitel.bazaar.game.constants.Key.CALLBACK;
import static com.farsitel.bazaar.game.constants.Key.EVENT_ID;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.farsitel.bazaar.game.callbacks.IEventDoneCallback;
import com.farsitel.bazaar.game.constants.Constant;

public class IntentHelper {

    private static Intent loginIntent;
    private static final String TAG = "IntentDataHelper";

    public static @Nullable
    Pair<String, IEventDoneCallback> getNotifyEventParams() {
        if (loginIntent.getIntExtra(KEY_LOGIN_REQUEST_CODE, 0) != EVENT_DONE_NOTIFY_REQUEST_CODE) {
            Log.d(TAG, "Login request code does not match EVENT_DONE_NOTIFY_REQUEST_CODE");
            return null;
        }

        try {
            IEventDoneCallback callback = (IEventDoneCallback) loginIntent.getSerializableExtra(CALLBACK);
            String eventId = loginIntent.getStringExtra(EVENT_ID);
            return Pair.create(eventId, callback);
        } catch (Exception e) {
            Log.d(TAG, "Error is happened after login: " + e);
            return null;
        }
    }


    public static Intent getEventDoneNotifyLoginIntent(
            String eventId,
            IEventDoneCallback callback
    ) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_LOGIN_REQUEST_CODE, EVENT_DONE_NOTIFY_REQUEST_CODE);
        bundle.putString(EVENT_ID, eventId);
        bundle.putSerializable(CALLBACK, callback);
        return getLoginIntent(bundle);
    }

    public static void showLoginPrompt(Context context) {
        startActionViewIntent(context, Constant.BAZAAR_LOGIN_URL, Constant.BAZAAR_PACKAGE_NAME);
    }

    public static void startActionViewIntent(Context context, String uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(uri));
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        context.startActivity(intent);
    }

    private static Intent getLoginIntent(Bundle bundle) {
        loginIntent = new Intent(Intent.ACTION_VIEW);
        loginIntent.setData(Uri.parse(BAZAAR_LOGIN_URL));
        loginIntent.setPackage(BAZAAR_PACKAGE_NAME);
        loginIntent.putExtras(bundle);
        return loginIntent;
    }
}