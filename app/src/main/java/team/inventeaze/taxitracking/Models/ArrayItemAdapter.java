package team.inventeaze.taxitracking.Models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import team.inventeaze.taxitracking.R;

/**
 * Created by AbdulMueed on 12/20/2014.
 */
public class ArrayItemAdapter extends ArrayAdapter<String> {

    Context mContext;
    int layoutResourceId;
    String data[] = null;

    public ArrayItemAdapter(Context mContext, int layoutResourceId, String[] data) {

        super(mContext, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*
         * The convertView argument is essentially a "ScrapView" as described is Lucas post
         * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
         * It will have a non-null value when ListView is asking you recycle the row layout.
         * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
         */
        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        // object item based on the position
        String tempString = data[position];

        Log.e("Taxi","Here: "+tempString);

        // get the TextView and then set the text (item name) and tag (item ID) values
        TextView textViewItem = (TextView) convertView.findViewById(R.id.textViewItem);
        textViewItem.setText(tempString);
        textViewItem.setTag(tempString);

        return convertView;

    }
}


