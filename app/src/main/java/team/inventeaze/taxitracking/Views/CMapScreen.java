package team.inventeaze.taxitracking.Views;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import team.inventeaze.taxitracking.CAppManager;
import team.inventeaze.taxitracking.Interfaces.ILocationInterface;
import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.Models.ArrayItemAdapter;
import team.inventeaze.taxitracking.Models.CBookingModel;
import team.inventeaze.taxitracking.Models.CModelServerMessage;
import team.inventeaze.taxitracking.MyActivity;
import team.inventeaze.taxitracking.R;
import team.inventeaze.taxitracking.Tracker.DirectionsJSONParser;
import team.inventeaze.taxitracking.Tracker.GPSTracker;
import team.inventeaze.taxitracking.Utility.MultipartUtility;

/**
 * Created by DELL on 12/6/2014.
 */
public class CMapScreen extends LinearLayout implements View.OnClickListener, ILocationInterface, SensorEventListener {

    Context context;
    int viewId;
    MultipartUtility utility;
    Button btnhired;
    Button btnLogout;
    Button btnListAllBookings;
    TextView txtusername;
    ImageView imvadddirection;
   // TelephonyManager telephonyManager;

    final Handler handler = new Handler();
    Timer timer;
    TimerTask timertask;
    Button btndispatch;
    Button btnInbox;
    Button btnSearchGoogleMaps;
    Button btnPostMessage;
    ListView listView;
    TextView txtvspeeddata;

    private IViewPageInterface pageEvents;
    private String hiredStatus = "Not Hired";
    boolean ishired = false; //initialy person is not hired

    GoogleMap googleMap;

    //all about sensors
    private SensorManager sensorManager;
    private Sensor sensorAcc;

    public void setViewPageInterface(IViewPageInterface ppageInterface) {
        pageEvents = ppageInterface;
    }

    GPSTracker trackerObject;
    Calendar calendar;

    public CMapScreen(Context pContext,int pViewId) {
        super(pContext,null);
        viewId = pViewId;
        context = pContext; //save the context of the screen

        InitializeElements();
        InitializeSensors();
        calendar = Calendar.getInstance();

    }

    public void InitializeSensors() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensorAcc,SensorManager.SENSOR_DELAY_NORMAL);
    }

    View view;
    MapFragment mapFragment;
    FragmentManager fmanager;
    Marker marker;
    LatLng position;
    String DEVICE_IMEI;

    public void Pause() {
        sensorManager.unregisterListener(this);
    }

    public void Resume() {
        sensorManager.registerListener(this,sensorAcc,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void SetIMEI(String pDeviceIMEI) {
        DEVICE_IMEI = pDeviceIMEI;
    }

    public void SetVariables() {
        txtusername.setText(MyActivity.username); //the user we are logged in with :p
    }

    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.map_screen ,this,true);

        btndispatch = (Button) view.findViewById(R.id.btndispatch); //here we go....
        btndispatch.setOnClickListener(this);

        fmanager = MyActivity.selfPointer.getFragmentManager();
        mapFragment = (MapFragment) fmanager.findFragmentById(R.id.map);

        googleMap = mapFragment.getMap(); //here we got our map


        trackerObject = new GPSTracker(context);
        trackerObject.setOnTrackerLocationChangeListener(this);

        btnhired =  (Button) view.findViewById(R.id.btnhiredbutton);
        btnhired.setText(hiredStatus);
        btnhired.setOnClickListener(this);

        btnPostMessage = (Button) view.findViewById(R.id.btnpostmessage);
        btnPostMessage.setOnClickListener(this);


        btnListAllBookings = (Button) view.findViewById(R.id.btnListAllBookings);
        btnListAllBookings.setOnClickListener(this);

        btnSearchGoogleMaps = (Button) view.findViewById(R.id.btnsearchgoogle);
        btnSearchGoogleMaps.setOnClickListener(this);

        btnLogout = (Button) view.findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(this);

        btnInbox = (Button) view.findViewById(R.id.btnInbox);
        btnInbox.setOnClickListener(this);

        txtvspeeddata = (TextView) view.findViewById(R.id.txtvspeeddata);


        txtusername = (TextView) view.findViewById(R.id.txtvusername);
        imvadddirection = (ImageView) view.findViewById(R.id.imvadddirection); //add user directions
        imvadddirection.setOnClickListener(this);





//        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
  //      DEVICE_IMEI = telephonyManager.getDeviceId().toString();
    //    Log.d("DEVICE ID ",DEVICE_IMEI);
    }

    double getLatitude;
    double getLongitude;
    String username;


    public void StartTimer() {
        timer = new Timer();
        InitializeTimerTask();
        timer.schedule(timertask,2000,4000);
    }

    MarkerOptions markeroptions ;
    Marker mkrposition;
    BitmapDescriptor pickupMarker;
    BitmapDescriptor destinationMarker;

    public void FirstTimeSetting(LatLng pos) {
        markeroptions = new MarkerOptions();
                 markeroptions
                .position(pos)
                .icon(pickupMarker)
                .alpha(0.8f).title("Pickup Point");
//                .alpha(0.7f).title(MyActivity.username);

        Log.d("LatLong"," Longitude : "+pos.longitude+" Latitude: "+pos.latitude); //here we go long and lat



        mkrposition = googleMap.addMarker(markeroptions);
        firstTime = false;
    }

    public void AddDestinationMarker(LatLng position) {
        markeroptions = new MarkerOptions();
        markeroptions.position(position)
                .icon(destinationMarker)
                .title("Destination");

        googleMap.addMarker(markeroptions);
    }

    public void setPositionInMap(LatLng pos) {
        if(mkrposition != null)
            mkrposition.setPosition(pos);
    }

    public void InitializeTimerTask() {

        pickupMarker = BitmapDescriptorFactory.fromResource(R.drawable.pickupmarker);
        destinationMarker = BitmapDescriptorFactory.fromResource(R.drawable.destinationmarker);

        timertask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (CAppManager.getInstance().isNetworkAvailable()) {

                            //this is for getting messages...
                            new RecievingUserMessages().execute();

                            if (trackerObject.canGetLocation()) { //if we can get location...
                                getLatitude = trackerObject.getLatitude();
                                getLongitude = trackerObject.getLongitude(); //get the location
                                username = MyActivity.username;

                                position = new LatLng(getLatitude, getLongitude);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));

                                if (firstTime)
                                    FirstTimeSetting(position);
                                else
                                    setPositionInMap(position);

                                new myTask().execute();

                                //here we have to hit on server to check for any message from the server
                                Log.d("Taxi", "Server Message: " + isMessageRecieved);
                                if (!isMessageRecieved) {
                                    new getServerMessage().execute();
                                }

                                Log.d("user", "Here we go: sending data " + getLatitude + " " + getLongitude);
                                //    Toast.makeText(context,"Cannot send lat long: "+getLatitude+" "+getLongitude,Toast.LENGTH_LONG);
                            } else {
                                Log.d("user", "Here we go: cannot send data ");
                                Toast.makeText(context, "Cannot send lat long: Make sure GPS is on. ", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
            }
        };

    }

    public void SetDestination(String destinationPath) {

        destinationPath = destinationPath.replace(" ","+");
        //add + at every place where there is space
        gpsLink = "https://maps.googleapis.com/maps/api/geocode/json?address="+destinationPath+"&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw";
        //https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw

        new Clatlong().execute();

    }

    boolean firstTime = true;

    public String GetTimeStamap() {
        String date = calendar.getTime().toString();
        Log.d("user","Date: "+date);
        return date;
    }

    Button btnaddDest;
    Button btnCancel;
    AlertDialog dialog;
    EditText edtaddress;
    String gpsLink;

    public void Finish() {
        if(pageEvents != null)
            pageEvents.ViewPageDone(viewId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnListAllBookings:
                pageEvents.EventInPage(viewId,"allbookings",null);
                break;

            case R.id.btnsearchgoogle:
                pageEvents.EventInPage(viewId,"opengooglesearch",null);
            break;

            case R.id.btnInbox:
                Finish();
                break;

            case R.id.btnpostmessage:
                pageEvents.EventInPage(viewId,"openpostmessage",null);
                break;

            case R.id.btndispatch:
                playDefaultNotificationSound();
                CustomServerMessageBox();

                break;

            case R.id.btnadd:
                Log.d("data"," address: "+edtaddress.getText().toString());
                String address = edtaddress.getText().toString();
                address = address.replace(" ","+");
                //add + at every place where there is space
                gpsLink = "https://maps.googleapis.com/maps/api/geocode/json?address="+address+"&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw";
                //https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw

                new Clatlong().execute();

                dialog.cancel();
                break;

            case R.id.btncancel:
                dialog.cancel();
                break;

            case R.id.imvadddirection:
                //when user has clicked on direction
                AlertDialog.Builder dialogbox = new AlertDialog.Builder(context);
                dialogbox.setMessage("Destination Address");
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View directionView  = inflater.inflate(R.layout.layout_direction_data,null);
                dialogbox.setView(directionView); //here we set the view
                edtaddress = (EditText) directionView.findViewById(R.id.edtdestinationaddress);
                btnaddDest = (Button) directionView.findViewById(R.id.btnadd);
                btnCancel = (Button) directionView.findViewById(R.id.btncancel);

                btnCancel.setOnClickListener(this);
                btnaddDest.setOnClickListener(this);

                dialog = dialogbox.create();
                dialog.show();


                break;

            case R.id.btnlogout:

                //here we have to logout :p
                timertask.cancel();

                //pageEvents.ViewPageDone(viewId);
                pageEvents.EventInPage(viewId,"logout",null);

                break;

            case R.id.btnhiredbutton:

                if(ishired){
                    hiredStatus = "Hired";
                    ishired = false; //now taxi is hired
                    btnhired.setText(hiredStatus);
                }
                else  {
                    hiredStatus = "Not Hired";
                    ishired = true;
                    btnhired.setText(hiredStatus);
                }

                new userstatus().execute();

                break;

        }
    }

    AlertDialog serverDialog;
    View messageView;


    public void CustomServerMessageBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Server Says");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        messageView = inflater.inflate(R.layout.server_message_dialoge,null);

        listView = (ListView) messageView.findViewById(R.id.listvserveritems);
        isMessageRecieved = true;

        builder.setView(messageView);

        String[] values = new String[] {
                "Booking Id: "+tempModel.bookingid,
                "Booking Type: "+tempModel.bookingtype,
                "Booking Date: "+tempModel.bookingdatetime,
                "Customer name: "+tempModel.customername,
                "Pickup: "+tempModel.pickup,
                "Destination: "+tempModel.destination,
                "Contact: "+tempModel.contactnumber,
                "Details: "+tempModel.details,
                "Cost: "+tempModel.cost,
                "Flight Information: "+tempModel.flightinformation,
                "Service Type: "+tempModel.servicetype,
                "Driver Id: "+tempModel.driverid,
                "Contact Number: "+tempModel.contactnumber,
                "Dispatcher Id: "+tempModel.dispatcherid,
                "Cab Id: "+tempModel.cabid,
                "Comments: "+tempModel.comments,
                "Booking Status: "+tempModel.bookingstatus,
                "Payment Status: "+tempModel.paymentstatus,
                "Entry Time Stamp: "+tempModel.entrytimestamp
        };


        ArrayItemAdapter sAdaptr  = new ArrayItemAdapter(context,R.layout.serverdialog_item,values);

        listView.setAdapter(sAdaptr);
        sAdaptr.notifyDataSetChanged();

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.dismiss();
                isMessageRecieved = false;
            }
        });

        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                isMessageRecieved = false;
            }
        });

        serverDialog = builder.create();

        serverDialog.show();


        builder.setView(messageView);

    }

    @Override
    public void onTrackerLocationChange(LatLng newlatlon, float accuracy) {
        //here we get new location that we must update on the server
        Toast.makeText(context,"Location Change With Accuracy: "+accuracy+" lat: "+newlatlon.latitude+" lon: "+newlatlon.longitude,Toast.LENGTH_LONG).show();
        getLongitude = newlatlon.latitude;
        getLongitude = newlatlon.longitude;


        setPositionInMap(newlatlon);
    }

    long lastUpdate = 0;
    float last_x=0.0f,last_y=0.0f,last_z=0.0f;
    private static final int SHAKE_THRESHOLD = 2;

    //all about sensors :p
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(txtvspeeddata == null)
            return;


        Sensor tempSensor = sensorEvent.sensor;

        if(tempSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //only when we get data in values
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if(position == null)
                        return;

                    txtvspeeddata.setText("Latitude: "+position.latitude+" Longitude: "+position.longitude+" Speed: "+speed);
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //here it ends

    private class myTask extends AsyncTask<String,String,String>{
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://ehmad11.com/labs/cab/track.php");
                utility.addFormField("action","location");
                utility.addFormField("timestamp",GetTimeStamap());

                Log.d("server","Sending to server: "+String.valueOf(getLatitude)+" lon: "+String.valueOf(getLongitude));

                utility.addFormField("lat",String.valueOf(getLatitude));
                utility.addFormField("long",String.valueOf(getLongitude));
                utility.addFormField("imei",String.valueOf(DEVICE_IMEI));
                utility.addFormField("sender","mobile");

                result = utility.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public boolean isMessageRecieved = false;
    CBookingModel tempModel = new CBookingModel();
    String oldbookingId = "-1";

    private class getServerMessage extends AsyncTask<String,String,String>{
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {

                utility = new MultipartUtility("http://ehmad11.com/labs/cab/index.php?r=booking/check");

               // utility.addFormField("read","0");
                utility.addFormField("imei",String.valueOf(DEVICE_IMEI));

                Log.d("taxi","Sending: "+String.valueOf(DEVICE_IMEI));

                result = utility.finish();

                Log.d("taxi","utitly result: "+result);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            if(s == null) {
                Log.d("Server","Message is null.");
                return;
            }




            try {
                JSONObject object = new JSONObject(s);
                if(object.has("error")) {
                    //if there is error in json object
                    String value = object.getString("error");
                    if(value.toLowerCase().contains("no bookings")) {
                        //if there are no bookings it means its ok leave it
                    }
                    else {

                        //JSONArray jarray = new JSONArray(s);
                        //JSONObject object = new JSONObject(s);


                    }
                }
                else {
                    //if there is no error.. which means there is booking and we have to show that booking

                    String currentBookingID = "-1";

                    tempModel.bookingid =  object.getString("booking_id");
                    currentBookingID = tempModel.bookingid;

                    Log.d("Taxi","Here booking id: "+currentBookingID);

                    tempModel.bookingtype = object.getString("booking_type");
                    tempModel.customername = object.getString("customer_name");
                    tempModel.company = object.getString("company");
                    tempModel.bookingdatetime = object.getString("booking_date_time");
                    tempModel.pickup = object.getString("pickup");
                    tempModel.destination = object.getString("destination");
                    tempModel.flightinformation = object.getString("flight_information");
                    tempModel.bookingstatus = object.getString("booking_status");
                    tempModel.dispatcherid = object.getString("dispatcher_id");
                    tempModel.driverid = object.getString("driver_id");
                    tempModel.cabid = object.getString("cabid");
                    tempModel.cost = object.getString("cost");
                    tempModel.servicetype = object.getString("service_type");
                    tempModel.comments = object.getString("comments");
                    tempModel.details = object.getString("details");
                    tempModel.paymentstatus = object.getString("payment_status");
                    tempModel.contactnumber = object.getString("contact_number");
                    tempModel.entrytimestamp = object.getString("EntryTimeStamp");


                    //here we got the destination address and now we have to sent it to that destination
                    Log.d("data"," address: "+tempModel.destination);

                    String address = tempModel.destination;
                    address = address.replace(" ","+");
                    //add + at every place where there is space
                    gpsLink = "https://maps.googleapis.com/maps/api/geocode/json?address="+address+"&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw";
                    //https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyAdXRKCAZAOjmKY8C9PUYRg-tFdFn1Qtsw

                    new Clatlong().execute();
                    //send us to that address

                    if(!oldbookingId.contains(currentBookingID)) {
                        playDefaultNotificationSound(); //playingn sound on notificatio
                        CustomServerMessageBox();
                    }
                    oldbookingId = currentBookingID;
                }
            }
            catch (JSONException ex) {
                ex.printStackTrace();
            }

            if(s != null) {
                Log.d("taxi","Message: "+s);
            }
            else {
                Log.d("taxi","Message is null ");
            }
        }
    }




LatLng destinationPosition;

    private class Clatlong extends AsyncTask<String,String,String>{
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility(gpsLink);
                result = utility.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null) {
                try {
                    JSONObject jsonobject = new JSONObject(s);
                    JSONArray resultData = jsonobject.getJSONArray("results");
                    jsonobject = new JSONObject(resultData.getString(0));

                    JSONObject latlong  = jsonobject.getJSONObject("geometry").getJSONObject("location");

                    double lat = latlong.getDouble("lat");
                    double lon = latlong.getDouble("lng");
                    LatLng destination = new LatLng(lat,lon);
                    destinationPosition = destination;
                    LatLng source = new LatLng(getLatitude,getLongitude);

                            // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(source, destination);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                    Log.d("data", " destination is lat: " + lat+" lon: "+lon);

                    //resultData.getString("geometry");



                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }



    private class userstatus extends AsyncTask<String,String,String>{
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://ehmad11.com/labs/cab/track.php");
                utility.addFormField("action","status");
                utility.addFormField("timestamp",GetTimeStamap());
                utility.addFormField("status",hiredStatus);
                utility.addFormField("imei",String.valueOf(DEVICE_IMEI));

                result = utility.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("result", result);
        }
    }

    //Things for map direction

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            googleMap.clear();

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
//
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            try {
                googleMap.addPolyline(lineOptions);
                AddDestinationMarker(destinationPosition);
            }catch (NullPointerException ne) {
                Toast.makeText(context,"Either Location Too far or Something went wrong.",Toast.LENGTH_LONG).show();
                Log.e("NullPointer","Message: "+ne.getMessage());
            }

            firstTime = true;
        }
    }


    private void playDefaultNotificationSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }





    private class RecievingUserMessages extends AsyncTask<String,String,String> {
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://ehmad11.com/labs/cab/index.php?r=message/check"); //chagne this link
                utility.addFormField("action","recieve");
                utility.addFormField("imei",DEVICE_IMEI);
                utility.addFormField("username", MyActivity.username);
                utility.addFormField("sender","mobile");

                result = utility.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s == null)
            {
                Toast.makeText(context,"Could not send message,Server Returns Null",Toast.LENGTH_LONG).show();
                return;
            }

          //  Toast.makeText(context,"Server Says "+s,Toast.LENGTH_LONG).show();

            try {
                JSONObject serverRes = new JSONObject(s);
                String message = serverRes.getString("message");
                String msgid = serverRes.getString("id");
                pageEvents.EventInPage(viewId,"servermessage",new CModelServerMessage(message, Integer.valueOf(msgid) ) );
              //  Toast.makeText(context,message,Toast.LENGTH_LONG).show();

            }
            catch (JSONException jex) {
                Log.e("JSON",jex.getMessage());
            }
        }
    }

}
