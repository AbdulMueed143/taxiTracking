package team.inventeaze.taxitracking.Views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import team.inventeaze.taxitracking.Interfaces.IViewPageInterface;
import team.inventeaze.taxitracking.R;

/**
 * Created by AbdulMueed on 1/5/2015.
 */
public class CSearchScreen extends LinearLayout implements View.OnClickListener {

    int viewId;
    Context context;

    private IViewPageInterface pageEvents;
    public void setViewPageInterface(IViewPageInterface ppageInterface) {
        pageEvents = ppageInterface;
    }


    public CSearchScreen(Context pContext,int pViewId) {
        super(pContext,null);
        viewId = pViewId;
        context = pContext; //save the context of the screen

        InitializeElements();
    }

    WebView searchMap;
    Button btnback;

    public void InitializeElements() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_screen, this, true);

        searchMap = (WebView) view.findViewById(R.id.webviewgooglesearch);

        searchMap.setWebViewClient(new AppWebViewClients());
        searchMap.getSettings().setJavaScriptEnabled(true);
        searchMap.loadUrl("https://www.google.com/maps/@37.0625,-95.677068,4z");


//        btnback = (Button) view.findViewById(R.id.btnsearchback);
  //      btnback.setOnClickListener(this);

    }



    public class AppWebViewClients extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            Log.e("Error","Reading url: "+url);
            return true;
        }

    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.btnsearchback:
  //              if(pageEvents != null)
    //                pageEvents.EventInPage(viewId,"backtomapscreen",null);
      //          break;
        }
    }
}
