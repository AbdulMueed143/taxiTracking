package team.inventeaze.taxitracking.Views;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.Models.CModelServerMessage;
import team.inventeaze.taxitracking.MyActivity;
import team.inventeaze.taxitracking.R;
import team.inventeaze.taxitracking.Utility.MultipartUtility;

/**
 * Created by AbdulMueed on 1/2/2015.
 */
public class CSendMessageScreen extends LinearLayout implements View.OnClickListener {

    Context context;
    int viewId;
    MultipartUtility utility;
    View view;
    String deviceID;

    /*here are our text boxes and buttons*/
    TextView itemOne, itemTwo, itemThree, itemFour, itemFive;
    EditText messageBox;
    Button btnBack, btnSendMessage;

    ListView serverMessages;


    private IViewPageInterface pageEvents;
    public void setViewPageInterface(IViewPageInterface ppageInterface) {
        pageEvents = ppageInterface;
    }


    public CSendMessageScreen(Context pContext,int pViewId) {
        super(pContext,null);
        viewId = pViewId;
        context = pContext; //save the context of the screen

        InitializeElements();
    }

    ArrayList<CModelServerMessage> messagesList;
    ArrayAdapter listAdapter;
    ArrayList<String> messagesAll;
    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.send_message_layout ,this,true);

        itemOne = (TextView) view.findViewById(R.id.txtvpointone);
        itemTwo = (TextView) view.findViewById(R.id.txtvpointtwo);
        itemThree = (TextView) view.findViewById(R.id.txtvpointthree);
        itemFour = (TextView) view.findViewById(R.id.txtvpointfour);
        itemFive = (TextView) view.findViewById(R.id.txtvpointfive);

        messageBox = (EditText) view.findViewById(R.id.messageBox);

        messagesList = new ArrayList<CModelServerMessage>(); //here we get the messages list

        serverMessages = (ListView) view.findViewById(R.id.lstserverMessages);

        messagesAll = new ArrayList<String>();
        for (int index = 0; index < messagesList.size(); index++) {
            messagesAll.add(messagesList.get(index).message);
        }

        listAdapter = new ArrayAdapter<String>(context, R.layout.simplerow, messagesAll);

        serverMessages.setAdapter(listAdapter);


        btnBack = (Button) view.findViewById(R.id.btnback);
        btnSendMessage = (Button) view.findViewById(R.id.btnsendmessage);


        itemOne.setOnClickListener(this);
        itemTwo.setOnClickListener(this);
        itemThree.setOnClickListener(this);
        itemFour.setOnClickListener(this);
        itemFive.setOnClickListener(this);

        btnBack.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);

    }

    public boolean isNewMessage(int id) {
        //tell me if this is new message
        int count = messagesList.size();
        for(int index = 0; index < count; index++)
           if( messagesList.get(index).id == id)
               return false;

        return true;
    }

    public void AddMessageToList(CModelServerMessage messageobj) {
       CModelServerMessage curr = messageobj;
       messagesList.add(curr);
        messagesAll.add(messageobj.message);
        listAdapter.notifyDataSetChanged();

        playDefaultNotificationSound();
    }



    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.txtvpointone:
                messageBox.setText("I have an emergency");
                break;

            case R.id.txtvpointtwo:
                messageBox.setText("Deviation is on clients discretion");
                break;

            case R.id.txtvpointthree:
                messageBox.setText("I am heading for food/ refueling");
                break;

            case R.id.txtvpointfour:
                messageBox.setText("I am heading to pick-up client ");
                break;

            case R.id.txtvpointfive:
                messageBox.setText("I am lost");
                break;

            case R.id.btnback:
                Finish();
                break;

            case R.id.btnsendmessage:
                Toast.makeText(context,"Message Sending in process please wait.",Toast.LENGTH_LONG).show();
                new SendingUserMessage().execute();
                break;
        }

    }


    private class SendingUserMessage extends AsyncTask<String,String,String> {
        String result;
        @Override
        protected String doInBackground(String... strings) {

            try {
                utility = new MultipartUtility("http://system.sudburycab.com/index.php?r=message/post"); //chagne this link
                utility.addFormField("action","usermessage");
                utility.addFormField("imei",deviceID);
                utility.addFormField("username", MyActivity.username);
                utility.addFormField("sender","mobile");
                utility.addFormField("message",messageBox.getText().toString());
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

            try {
                JSONObject serverRes = new JSONObject(s);
                String message = serverRes.getString("success");
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                messageBox.setText("");

            }
            catch (JSONException jex) {
                Log.e("JSON",jex.getMessage());
            }
        }
    }

    private void playDefaultNotificationSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }







    public void SetIMEI(String DeviceIMEI) {
        deviceID = DeviceIMEI;
    }

    public void Finish() {
        //this pages finish event is called
        if(pageEvents != null)
            pageEvents.ViewPageDone(viewId);
    }
}
