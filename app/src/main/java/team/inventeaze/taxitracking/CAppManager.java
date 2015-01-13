package team.inventeaze.taxitracking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by AbdulMueed on 12/17/2014.
 */
public class CAppManager {

    Context context;
    static CAppManager appManager;

    public CAppManager() {

    }

    public void setContext(Context pContext) {
        context = pContext;
    }

    public static CAppManager getInstance() {
        if(appManager == null)
            appManager = new CAppManager();

        return appManager;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
