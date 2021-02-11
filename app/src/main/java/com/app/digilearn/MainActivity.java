package com.app.digilearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "ec1df0aa-1819-4d51-8364-ab6c1c7322bb"; //code onesignal
    public static final boolean PUSH_ENABLED = false;
    public static final boolean PUSH_ENCHANCED_WBVIEW_URL = false;
    public static final boolean PUSH_RELOAD_ON_USER_ID = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            //change font app


            //hidden keyboard
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            //Remove title bar
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);

            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.activity_main);




            //change transition open and close layout
            //   overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_scale);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();


                }
            }, 1000L); //3000 L = 3 detik

        } catch (Exception e) {
            Log.e("errrorr", e.getMessage());
        }
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }

}



