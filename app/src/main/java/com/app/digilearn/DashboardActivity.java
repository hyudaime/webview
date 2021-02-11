package com.app.digilearn;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.app.digilearn.APIInterface.RegisterAPIInterface;
import com.app.digilearn.app.AppConstants;
import com.app.digilearn.model.DeviceId;
import com.app.digilearn.model.ModelNotif;
import com.app.digilearn.model.Notification;
import com.app.digilearn.model.PostContact;
import com.app.digilearn.model.ResponseWebview;
import com.app.digilearn.permission.PermissionsActivity;
import com.app.digilearn.permission.PermissionsChecker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayoutStates;

import okhttp3.OkHttpClient;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.graphics.BitmapFactory.*;

import com.onesignal.OSDeviceState;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;
import org.json.JSONObject;


public class DashboardActivity extends AppCompatActivity {

    private PermissionsChecker checker;

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    private Uri uriContact;
    private String contactID;     // contacts unique ID
    private String TAG = DashboardActivity.class.getSimpleName();
    private WebView webView;
    private PostContact postContact;

    private List<PostContact> postContactModel = new ArrayList<>();

    private PostContact singlePostContact = new PostContact();
    private Boolean isPostcontact = false;
    private String token;


    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private Notification notification;
    private boolean isNotif = false;
    private boolean isLoadWeb = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        checker = new PermissionsChecker(this);
        postContact = new PostContact();

        if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
            startPermissionsActivity(PERMISSIONS_READ_STORAGE);
        } else {
            readContacts();
        }



        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);


        // Tiga baris di bawah ini agar laman yang dimuat dapat
        // melakukan zoom.
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        // Baris di bawah untuk menambahkan scrollbar di dalam WebView-nya
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setUserAgentString(System.getProperty("http.agent"));

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                if (url.contains(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        //user does not have a pdf viewer installed
                    }
                } else {
                    webView.loadUrl(url);
                }

                return false; // then it is not handled by default action

            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                Log.e("error", description);
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {        //show progressbar here

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //hide progressbar here

            }


        });


        webView.setWebChromeClient(new ChromeClient());


                        //  readContacts();
                        //   retrieveContactNumber();

                        readContacts();

                        if (!isPostcontact)
                            mPostContact();

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        DeviceId deviceId = new DeviceId();

                        // Get new FCM registration token
                        deviceId.setDeviceID(task.getResult());

                        mPostContactDeviceId(deviceId);


                        // Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
                    }
                });
        if (!isLoadWeb)
            mGetWebview();
        //7 dari dibawah Memperbaiki bug masalah refresh saat orientasi layar saat memutar video fullscreen
        if (savedInstanceState==null){
            webView.post(new Runnable() {
                @Override
                public void run() {
                mGetWebview();
                }
            });
        }//akhir dari code diatas

        notification = getIntent().getParcelableExtra("detail");

        if (notification != null) {
            Log.v(TAG, "detail");
            mGetDetailNotification(notification);
        } else {
            onNewIntent(getIntent());
        }

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v(TAG, url);

                if (url.startsWith("tel:") || url.contains("https://wa.me/")
                        || url.contains("whatsapp")
                        || url.contains("twitter")
                        || url.contains("instagram")
                        || url.contains("youtube")
                        || url.contains("facebook")

                ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

        });

    }
// 3 overide dibawah adalah lanjutan untuk meperbaiki masalah video fullscreen dan reload
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState ) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
    //end of overide untuk mengatasi masalah playback video tidak bisa fullscreen dan refresh

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent");
        try {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Set<String> keys = bundle.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (key.toLowerCase().contains("body")) {
                        String mBody = bundle.get(key) + "";
                        Log.v(TAG, bundle.get(key) + "");
                        //mIdLoan = Integer.parseInt(mBody);
                        // mGetDetailDayOff();
                        mGetDetailNotifIntent(mBody);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            if (requestCode == FILECHOOSER_RESULTCODE) {

                if (null == this.mUploadMessage) {
                    return;

                }

                Uri result = null;

                try {
                    if (resultCode != RESULT_OK) {

                        result = null;

                    } else {

                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    // Toast.makeText(getApplicationContext(), "activity :" + e,
                    //Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

            }
        }

        return;
    }


    public class ChromeClient extends WebChromeClient {

        // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e("ErrorCreatingFile", "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;

        }

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

            mUploadMessage = uploadMsg;
            // Create AndroidExampleFolder at sdcard
            // Create AndroidExampleFolder at sdcard

            File imageStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES)
                    , "AndroidExampleFolder");

            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }

            // Create camera captured image file path and name
            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");

            mCapturedImageURI = Uri.fromFile(file);

            // Camera capture image intent
            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[]{captureIntent});

            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);


        }

        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {

            openFileChooser(uploadMsg, acceptType);
        }
        //ini code untuk mengaktifkan fullscrenn di videoplayer
        public View mCustomView;
        public WebChromeClient.CustomViewCallback mCustomViewCallback;
        public int mOriginalOrientation;
        public int mOriginalSystemUiVisibility;
        protected FrameLayout mFullscreenContainer;

        ChromeClient() {}

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return null;
        }

        public void onHideCustomView (){
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);//untuk mengatasu status bar hilang saat memutar video fullscreen
        }//akhir dari mengaktifkan fullscreen di video player
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    //request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, DashboardActivity.this);
    }

    //start permission
    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    void readContacts() {
        try {
            postContactModel.clear();

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


            if (cursor.getCount() > 0) {


                while (cursor.moveToNext()) {

                    PostContact postContact = new PostContact();

                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String Numbers = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[-\\[\\]^/,'*:.!><~@#$%=?|\"\\\\()]+", "");


                    String a = Numbers.replace("+62", "0");
                    String b = a.replace(" ", "");

                    postContact.setName(name);
                    postContact.setPhone(b);
                    postContact.setEmail("");

                    singlePostContact.setName(name);
                    singlePostContact.setPhone(b);
                    singlePostContact.setEmail("");
                    postContactModel.add(postContact);


                    // Toast.makeText(getApplicationContext(), b + " " + name, Toast.LENGTH_SHORT).show();

                }


            } else {
                //  Toast toast = Toast.makeText(getApplicationContext(), "Nothing", Toast.LENGTH_SHORT);
                // toast.show();
            }
        } catch (Exception e) {

        }


    }


    private void mPostContactDeviceId(DeviceId deviceId) {

        Log.v(TAG, "mPostContact");
        Log.v(TAG, postContactModel.size() + " mPostContact");


        try {


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .readTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .writeTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            RegisterAPIInterface api = retrofit.create(RegisterAPIInterface.class);


            Call<Void> call = api.mPostDeviceID("aa", deviceId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    Log.v(TAG, response.toString());


                    if (response.isSuccessful()) {
                        isPostcontact = true;
                        //Toast.makeText(DashboardActivity.this, "Update device ID berhasil", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(TAG, response.errorBody().toString());
                    }

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.v(TAG, t.getMessage());
                    Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                }

            });


        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
        }
    }


    private void mPostContact() {

        Log.v(TAG, "mPostContact");
        Log.v(TAG, postContactModel.size() + " mPostContact");

        try {


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .readTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .writeTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            RegisterAPIInterface api = retrofit.create(RegisterAPIInterface.class);


            Call<Void> call = api.mPostContactList("aa", postContactModel);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    Log.v(TAG, response.toString());


                    if (response.isSuccessful()) {
                        isPostcontact = true;
                        //   Toast.makeText(DashboardActivity.this, "Kontak berhasil diupload", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(TAG, response.errorBody().toString());
                    }

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.v(TAG, t.getMessage());
                    Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                }

            });


        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {


        if (isNotif) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }


    }


    public void mGetWebview() {

        try {

            String Authorization = "aaa";


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .readTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .writeTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            RegisterAPIInterface api = retrofit.create(RegisterAPIInterface.class);

            // simplified call to request the news with already initialized service
            Call<ResponseWebview> call = api.mGetWebview(Authorization);

            call.enqueue(new Callback<ResponseWebview>() {
                @Override
                public void onResponse(Call<ResponseWebview> call, Response<ResponseWebview> response) {

                    Log.v(TAG, response.toString());
                    try {
                        if (response.isSuccessful()) {
                            Log.v(TAG, response.body().getLinkUrl());

                            webView.loadUrl(response.body().getLinkUrl());

                            isLoadWeb = true;
                            webView.loadUrl("https://belajardigitalmarketing.id/");
                        } else {
                            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.v(TAG, e.getMessage());
                        Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                    }
                }


                @Override
                public void onFailure(Call<ResponseWebview> call, Throwable t) {
                    Log.v(TAG, t.getMessage());
                    Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
        }
    }

    public void mGetDetailNotification(Notification notification) {

        try {

            String Authorization = "aaa";

          /*  Map<String, String> datas = new HashMap<>();

            datas.put("id", id);

            Log.v(TAG, notification.toString());*/


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .readTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .writeTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            RegisterAPIInterface api = retrofit.create(RegisterAPIInterface.class);

            // simplified call to request the news with already initialized service
            Call<ModelNotif> call = api.mGetDetailNotif(Authorization, notification.getId(), notification.getTitle());

            call.enqueue(new Callback<ModelNotif>() {
                @Override
                public void onResponse(Call<ModelNotif> call, Response<ModelNotif> response) {

                    Log.v(TAG, response.toString());
                    try {
                        if (response.isSuccessful()) {
                            Log.v(TAG, response.body().getLink());
                            isNotif = true;
                            webView.loadUrl(response.body().getLink());
                            //webView.loadUrl("https://kiwari.id/");
                        } else {
                            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.v(TAG, e.getMessage());
                        Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ModelNotif> call, Throwable t) {
                    Log.v(TAG, t.getMessage());
                    Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
        }
    }

    public void mGetDetailNotifIntent(String id) {

        try {

            String Authorization = "aaa";

          /*  Map<String, String> datas = new HashMap<>();

            datas.put("id", id);

            Log.v(TAG, notification.toString());*/


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .readTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .writeTimeout(AppConstants.TIMEOUT_REQUEST, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            RegisterAPIInterface api = retrofit.create(RegisterAPIInterface.class);

            // simplified call to request the news with already initialized service
            Call<ModelNotif> call = api.mGetDetailNotif(Authorization, id, "");

            call.enqueue(new Callback<ModelNotif>() {
                @Override
                public void onResponse(Call<ModelNotif> call, Response<ModelNotif> response) {

                    Log.v(TAG, response.toString());
                    try {
                        if (response.isSuccessful()) {
                            Log.v(TAG, response.body().getLink());
                            isNotif = true;
                            webView.loadUrl(response.body().getLink());
                            //webView.loadUrl("https://kiwari.id/");
                        } else {
                            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.v(TAG, e.getMessage());
                        Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ModelNotif> call, Throwable t) {
                    Log.v(TAG, t.getMessage());
                    Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            Snackbar.make(getWindow().getDecorView(), R.string.loading_error, Snackbar.LENGTH_LONG).show();
        }
    }
}

