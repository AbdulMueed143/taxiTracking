package team.inventeaze.taxitracking.Views;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import team.inventeaze.taxitracking.CAppManager;
import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.MyActivity;
import team.inventeaze.taxitracking.R;
import team.inventeaze.taxitracking.Utility.MultipartUtility;

/**
 * Created by DELL on 12/6/2014.
 */
public class CloginScreen extends LinearLayout implements View.OnClickListener {

    Context context;
    int viewId;
    Button btn_proceed;

    private String username;
    private EditText edtusername;
    private EditText edtpassword; //user password
    MultipartUtility utility;
    ProgressDialog progressDialog;
    CAppManager managerInstance;


    private IViewPageInterface pageEvents;
    String DEVICE_IMEI;

    public void SetIMEI(String pDeviceIMEI) {
        DEVICE_IMEI = pDeviceIMEI;
    }


    public void setViewPageInterface(IViewPageInterface ppageInterface) {
        pageEvents = ppageInterface;
    }

    public CloginScreen(Context pContext,int pViewId) {
        super(pContext,null);
        viewId = pViewId;
        context = pContext; //save the context of the screen

        InitializeElements();

    }

    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.login_screen ,this,true);

        btn_proceed = (Button) view.findViewById(R.id.btn_proceed);
        edtusername = (EditText) view.findViewById(R.id.edt_username);
        edtpassword = (EditText) view.findViewById(R.id.edt_password);
        //here we get reference of username and password box

        btn_proceed.setOnClickListener(this);
        progressDialog = new ProgressDialog(context);

    }



    private class userlogin extends AsyncTask<String,Integer,String> {
        String result;

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]); //update the progress
        }

        @Override
        protected String doInBackground(String... strings) {

            try {


                utility = new MultipartUtility("http://ehmad11.com/labs/cab/login.php");
                utility.addFormField("action","login");
                username = edtusername.getText().toString();
                Log.d("data","username: "+username);
                utility.addFormField("username",username);
                utility.addFormField("password",edtpassword.getText().toString());
                utility.addFormField("imei",String.valueOf(DEVICE_IMEI));
                utility.addFormField("sender","mobile");

                result = utility.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result == null) {
                Toast.makeText(context, "No internet connection available, please connect to internet first. ", Toast.LENGTH_LONG).show();
                progressDialog.cancel();
                return;
            }

            Log.i("result", result);

            if(result.contains("1")) {
                pageEvents.EventInPage(viewId, "setusername", username);
                if (pageEvents != null) {
                    pageEvents.ViewPageDone(viewId);
                }
            }
            else if (result.contains("0")) {
                Toast.makeText(context,"Wrong Username or Password.", Toast.LENGTH_LONG).show();
            }

            progressDialog.cancel();
        }
    }

    public void setAppManagerInstance(CAppManager pmanagerInstance) {
        managerInstance = pmanagerInstance;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_proceed: //that is actually login.. when users says i want to login..

                //here we will first write login credentials...
                //its simply saving driver name in database nothing else..
                String username = edtusername.getText().toString();

                if(!managerInstance.isNetworkAvailable()) {
                    Toast.makeText(context,"No internet connection available, please connect to internet first. ", Toast.LENGTH_LONG).show();
                    return;
                }

                if(username.length() < 3)
                {
                    Toast.makeText(context,"Please Enter Full Name atleast 3 charachter long. ",Toast.LENGTH_LONG).show();
                    return;
                }

                //here we will start logging in.. let us show some loading thing..
                //if we get success only then

                MyActivity.username = username;
                progressDialog.setMessage("Login Progress");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                new userlogin().execute();

                break;
        }
    }
}
