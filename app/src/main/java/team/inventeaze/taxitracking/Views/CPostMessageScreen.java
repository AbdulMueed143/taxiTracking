package team.inventeaze.taxitracking.Views;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.MyActivity;
import team.inventeaze.taxitracking.R;
import team.inventeaze.taxitracking.Utility.MultipartUtility;

/**
 * Created by AbdulMueed on 1/7/2015.
 */
public class CPostMessageScreen extends LinearLayout implements View.OnClickListener  {

     int viewId;
     Context context;

     private IViewPageInterface pageEvents;
     String DEVICE_IMEI;
     MultipartUtility utility;

    //buttons
    Button btnpostmessage;


  //  EditText edtbookingid, edtbookingtype,  edtcompany, edtbookingdatetime,  edtflightinfo, edtservicetype, edtcomments;
    EditText edtdestination, edtcustomername,edtpickup, edtdriverid, edtcost, edtcontactnumber ;
   // EditText edtbookingstatus, edtpaymentstatus,edtdetails;

    public void SetIMEI(String pDeviceIMEI) {
        DEVICE_IMEI = pDeviceIMEI;
        edtdriverid.setText(DEVICE_IMEI);
    }


    public void setViewPageInterface(IViewPageInterface ppageInterface) {
            pageEvents = ppageInterface;
    }

        public CPostMessageScreen(Context pContext,int pViewId) {
            super(pContext,null);
            viewId = pViewId;
            context = pContext; //save the context of the screen

            InitializeElements();
        }

    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.post_message_layout ,this,true);

        btnpostmessage = (Button) view.findViewById(R.id.btnpostmessage);
        btnpostmessage.setOnClickListener(this);


       // edtbookingid = (EditText) view.findViewById(R.id.edtbookingid);
      //  edtbookingstatus = (EditText) view.findViewById(R.id.edtbookingstatus);
      //  edtbookingtype = (EditText) view.findViewById(R.id.edtbookingtype);
     //   edtbookingdatetime = (EditText) view.findViewById(R.id.edtbookingdatetime);
     //   edtcomments = (EditText) view.findViewById(R.id.edtcomments);
        edtdestination = (EditText) view.findViewById(R.id.edtdestination);
      //  edtcompany = (EditText) view.findViewById(R.id.edtcompany);
        edtcontactnumber = (EditText) view.findViewById(R.id.edtcontactnumber);
        edtcost = (EditText) view.findViewById(R.id.edtcost);
     //   edtservicetype = (EditText) view.findViewById(R.id.edtservicetype);
        edtpickup = (EditText) view.findViewById(R.id.edtpickup);
    //    edtflightinfo = (EditText) view.findViewById(R.id.edtflightinfo);
        edtdriverid =  (EditText) view.findViewById(R.id.edtdriverid);
     //   edtdetails = (EditText) view.findViewById(R.id.edtdetails);
        edtcustomername = (EditText) view.findViewById(R.id.edtcustomername);
      //  edtpaymentstatus = (EditText) view.findViewById(R.id.edtpaymentstatus);

        edtdriverid.setText(DEVICE_IMEI);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnpostmessage:

                Toast.makeText(context,"Please wait while posting booking.",Toast.LENGTH_LONG).show();

                new PostBooking().execute();

                break;

        }
    }


    private class PostBooking extends AsyncTask<String,String,String> {
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://system.sudburycab.com/index.php?r=message/post"); //chagne this link
        //        utility.addFormField("booking_id",edtbookingid.getText().toString());
          //      utility.addFormField("booking_type",edtbookingtype.getText().toString());
          //      utility.addFormField("booking_status", edtbookingstatus.getText().toString());
           //     utility.addFormField("booking_datetime", edtbookingdatetime.getText().toString());
                utility.addFormField("customer_name", edtcustomername.getText().toString());
             //   utility.addFormField("payment_status", edtpaymentstatus.getText().toString());
                utility.addFormField("from", edtpickup.getText().toString());
                utility.addFormField("to", edtdestination.getText().toString());
           //     utility.addFormField("details", edtdetails.getText().toString());
             //   utility.addFormField("flight_information", edtflightinfo.getText().toString());
                utility.addFormField("cost", edtcost.getText().toString());
                utility.addFormField("contact_number", edtcontactnumber.getText().toString());
           //     utility.addFormField("company", edtcompany.getText().toString());
            //    utility.addFormField("comments", edtcomments.getText().toString());
            //    utility.addFormField("service_type", edtservicetype.getText().toString());
           //     utility.addFormField(,.getText().toString());
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

                JSONObject bookingres = new JSONObject(s); //here we get all the bookings..
                Iterator<String> keysiterator = bookingres.keys();

                while(keysiterator.hasNext()) {
                    Toast.makeText(context, "Server Says: " + bookingres.getString(keysiterator.next()), Toast.LENGTH_LONG).show();
                    //here we will make destination as the path shown

                    if(pageEvents != null) {
                        String data = edtdestination.getText().toString();
                        Toast.makeText(context,"Destination: "+data,Toast.LENGTH_LONG).show();
                        pageEvents.EventInPage(viewId,"destinationpath",data);
                    }
                    else {
                        Toast.makeText(context,"PageEvents is null",Toast.LENGTH_LONG).show();
                    }

                }

            }
            catch (JSONException jex) {
                Log.e("JSON", jex.getMessage());
            }
        }
    }



}
