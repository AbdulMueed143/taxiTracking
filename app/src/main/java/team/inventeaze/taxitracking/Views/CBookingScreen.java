package team.inventeaze.taxitracking.Views;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import team.inventeaze.taxitracking.Adapters.CBookingsAdapter;
import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.Models.CBookingModel;
import team.inventeaze.taxitracking.MyActivity;
import team.inventeaze.taxitracking.R;
import team.inventeaze.taxitracking.Utility.MultipartUtility;

/**
 * Created by AbdulMueed on 1/5/2015.
 */
public class CBookingScreen extends LinearLayout {

    int viewId;
    Context context;


    private IViewPageInterface pageEvents;
    String DEVICE_IMEI;

    ListView lstbookings;
    MultipartUtility utility;

    public void SetIMEI(String pDeviceIMEI) {
        DEVICE_IMEI = pDeviceIMEI;
    }


    public void setViewPageInterface(IViewPageInterface ppageInterface) {
        pageEvents = ppageInterface;
    }

    public CBookingScreen(Context pContext,int pViewId) {
        super(pContext,null);
        viewId = pViewId;
        context = pContext; //save the context of the screen

        InitializeElements();
    }

    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.booking_screen_layout ,this,true);

        lstbookings = (ListView) view.findViewById(R.id.lstbookings);

    }

    CBookingsAdapter listAdapter;
    ArrayList<CBookingModel> bookingModels;
    public void InitializeList() {
        new RecieveAllBookings().execute(); //go to the server and get these things
        //until then make a list
        // Create and populate a List of planet names.
        bookingModels = new ArrayList<CBookingModel>(); //here is our embty list

        // Create ArrayAdapter using the planet list.
        listAdapter = new CBookingsAdapter(context , bookingModels);

        lstbookings.setAdapter(listAdapter);
    }

    public void AddModelToList(CBookingModel addingModel) {
        bookingModels.add(addingModel);
       listAdapter.notifyDataSetChanged();
    }


    private class RecieveAllBookings extends AsyncTask<String,String,String> {
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://ehmad11.com/labs/cab/index.php?r=booking/checkall"); //chagne this link
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
                Toast.makeText(context, "Could not send message,Server Returns Null", Toast.LENGTH_LONG).show();
                return;
            }

            //  Toast.makeText(context,"Server Says "+s,Toast.LENGTH_LONG).show();

            try {
                JSONArray allbookings = new JSONArray(s); //here we get all the bookings..
                int len =  allbookings.length();
                for(int index = 0; index < len; index++) {
                    JSONObject object = allbookings.getJSONObject(index);
                    CBookingModel tempModel = new CBookingModel();

                    tempModel.bookingid =  object.getString("booking_id");
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

                    //now add it to the list :p
                    AddModelToList(tempModel);
                }
            }
            catch (JSONException jex) {
                Log.e("JSON", jex.getMessage());
            }
        }
    }

}
