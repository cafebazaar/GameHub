package com.farsitel.bazaar.game.view;

import static com.farsitel.bazaar.game.constants.Constant.BAZAAR_LOGIN_URL;
import static com.farsitel.bazaar.game.constants.Constant.BAZAAR_PACKAGE_NAME;
import static com.farsitel.bazaar.game.constants.Constant.EVENT_DONE_NOTIFY_REQUEST_CODE;
import static com.farsitel.bazaar.game.constants.Constant.KEY_LOGIN_REQUEST_CODE;
import static com.farsitel.bazaar.game.constants.Key.CALLBACK;
import static com.farsitel.bazaar.game.constants.Key.EVENT_ID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.farsitel.bazaar.game.GameHub;
import com.farsitel.bazaar.game.callbacks.IEventDoneCallback;

public class LoginResultActivity extends Activity {

    private GameHub gameHub;

    public static void startLoginResultActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, LoginResultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameHub = GameHub.getInstance();
        startLoginForResult();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            handleLoginResult();
        }
        finish();
    }

    private void startLoginForResult() {
        Intent loginIntent = new Intent(Intent.ACTION_VIEW);
        getIntent().setData(Uri.parse(BAZAAR_LOGIN_URL));
        getIntent().setPackage(BAZAAR_PACKAGE_NAME);
        this.startActivityForResult(loginIntent, getRequestCode());
    }

    private int getRequestCode() {
        return getIntent().getIntExtra(KEY_LOGIN_REQUEST_CODE, 0);
    }

    private void handleLoginResult() {
        if (getRequestCode() == EVENT_DONE_NOTIFY_REQUEST_CODE) {
            getEvents();
        }
    }

    public void getEvents() {
        try {
            IEventDoneCallback callback = (IEventDoneCallback) getIntent().getSerializableExtra(CALLBACK);
            String eventId = getIntent().getStringExtra(EVENT_ID);
            gameHub.eventDoneNotify(getApplicationContext(), eventId, callback);
        } catch (Exception e) {
            Log.d("getEvents", "Error is happened after login: " + e);
        }
    }
}

