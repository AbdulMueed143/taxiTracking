package team.inventeaze.taxitracking;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.Models.CModelServerMessage;
import team.inventeaze.taxitracking.Views.CBookingScreen;
import team.inventeaze.taxitracking.Views.CMapScreen;
import team.inventeaze.taxitracking.Views.CPostMessageScreen;
import team.inventeaze.taxitracking.Views.CSearchScreen;
import team.inventeaze.taxitracking.Views.CSendMessageScreen;
import team.inventeaze.taxitracking.Views.CloginScreen;


public class MyActivity extends Activity implements IViewPageInterface {

    private ViewFlipper viewFlipper;
    public static String username;

    public static MyActivity selfPointer;


    private static final int LOGIN_SCREEN = 0;
    private static final int MAP_SCREEN = 1;
    private static final int SEND_MESSAGE_SCREEN = 2;
    private static final int SEARCH_MAP_SCREEN = 3;
    private static final int BOOKING_SCREEN = 4;
    private static final int POST_MESSAGE_SCREEN = 5;


    private CloginScreen loginScreen;
    private CMapScreen mapScreen;
    private CSendMessageScreen sendMessageScreen;
    private CSearchScreen searchMapScreen;
    private CBookingScreen bookingScreen;
    private CPostMessageScreen postBookingScreen;

    TelephonyManager telephonyManager;
    String DEVICE_IMEI;
    private static final String TAG = "GCM";

    CAppManager managerInstance;

    //for google play services
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        selfPointer = this;

        InitializeElement();

        // Check device for Play Services APK.
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.

        }

    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }




    public void InitializeElement() {
        viewFlipper = (ViewFlipper) findViewById(R.id.ViewSwitcher); //here we have view switcher

        loginScreen = new CloginScreen(this,LOGIN_SCREEN);
        mapScreen = new CMapScreen(this,MAP_SCREEN);
        sendMessageScreen = new CSendMessageScreen(this,SEND_MESSAGE_SCREEN);
        searchMapScreen = new CSearchScreen(this,SEARCH_MAP_SCREEN);
        bookingScreen = new CBookingScreen(this,BOOKING_SCREEN);
        postBookingScreen = new CPostMessageScreen(this,POST_MESSAGE_SCREEN);


        managerInstance = CAppManager.getInstance();
        managerInstance.setContext(this); //stting the context :p

        loginScreen.setViewPageInterface(this);

        mapScreen.setViewPageInterface(this);

        sendMessageScreen.setViewPageInterface(this);
        searchMapScreen.setViewPageInterface(this);
        bookingScreen.setViewPageInterface(this);
        postBookingScreen.setViewPageInterface(this);


        loginScreen.setAppManagerInstance(managerInstance);


        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        DEVICE_IMEI = telephonyManager.getDeviceId().toString();
        Log.d("DEVICE ID ", DEVICE_IMEI);

        loginScreen.SetIMEI(DEVICE_IMEI);
        mapScreen.SetIMEI(DEVICE_IMEI);
        sendMessageScreen.SetIMEI(DEVICE_IMEI);
        postBookingScreen.SetIMEI(DEVICE_IMEI);


        viewFlipper.addView(loginScreen);
        viewFlipper.addView(mapScreen);
        viewFlipper.addView(sendMessageScreen);
        viewFlipper.addView(searchMapScreen);
        viewFlipper.addView(bookingScreen);
        viewFlipper.addView(postBookingScreen);


        viewFlipper.setDisplayedChild(LOGIN_SCREEN);

        viewFlipper.setInAnimation(this,R.anim.flip_in_previous_animation);
        viewFlipper.setOutAnimation(this,R.anim.flip_out_previous_animation);

    }

    @Override
    public void onBackPressed() {

        int currentScreen = viewFlipper.getDisplayedChild(); //get id if display child
        if(     currentScreen == BOOKING_SCREEN ||
                currentScreen == SEARCH_MAP_SCREEN ||
                currentScreen == SEND_MESSAGE_SCREEN ||
                currentScreen == POST_MESSAGE_SCREEN) {
            viewFlipper.setDisplayedChild(MAP_SCREEN);
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapScreen.Pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapScreen.Resume();
        checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void ViewPageDone(int viewid) {
        switch (viewid) {
            case LOGIN_SCREEN:
                mapScreen.SetVariables();
                mapScreen.StartTimer(); //start the time and do the stuff
                viewFlipper.setDisplayedChild(MAP_SCREEN);
                break;

            case MAP_SCREEN:
                viewFlipper.setDisplayedChild(SEND_MESSAGE_SCREEN);
                break;

            case SEND_MESSAGE_SCREEN:
                viewFlipper.setDisplayedChild(MAP_SCREEN);
            break;
        }
    }

    @Override
    public void ViewPageStart(int viewid) {

    }

    @Override
    public void EventInPage(int viewid, String eventname, Object object) {

        switch (viewid) {

            case POST_MESSAGE_SCREEN:
                if(eventname.toLowerCase().contains("destinationpath")) {
                    String destpath = (String) object; //here we get the destination path string :p
                    Toast.makeText(this,"Destimnation: "+destpath,Toast.LENGTH_SHORT).show();
                    mapScreen.SetDestination(destpath);
                    viewFlipper.setDisplayedChild(MAP_SCREEN);
                }
                break;

            case LOGIN_SCREEN:
                if(eventname.toLowerCase().contains("setusername")) {
                    username = (String) object;
                }
            break;
            case SEARCH_MAP_SCREEN:
                if(eventname.toLowerCase().contains("backtomapscreen"))
                    viewFlipper.setDisplayedChild(MAP_SCREEN);
                break;

            case MAP_SCREEN:

                if(eventname.toLowerCase().contains("logout"))
                    viewFlipper.setDisplayedChild(LOGIN_SCREEN);
                else if(eventname.toLowerCase().contains("allbookings")) {
                    //sdfs
                            //here hgave to add booking screen
                    bookingScreen.InitializeList();
                    viewFlipper.setDisplayedChild(BOOKING_SCREEN);
                }
                else if(eventname.toLowerCase().contains("opengooglesearch")) {
                    viewFlipper.setDisplayedChild(SEARCH_MAP_SCREEN);
                }
                else if(eventname.toLowerCase().contains("openpostmessage")) {
                    viewFlipper.setDisplayedChild(POST_MESSAGE_SCREEN);
                }


                if(eventname.toLowerCase().contains("servermessage")) {
                    CModelServerMessage servermessage = (CModelServerMessage) object; //get the message
                    if(sendMessageScreen.isNewMessage(servermessage.id)) {
                        //if its new message add it to the list
                        sendMessageScreen.AddMessageToList(servermessage);
                    }
                }

                break;

        }

    }
}
