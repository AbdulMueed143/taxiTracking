package team.inventeaze.taxitracking.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import team.inventeaze.taxitracking.Models.CBookingModel;
import team.inventeaze.taxitracking.R;

/**
 * Created by AbdulMueed on 1/5/2015.
 */
public class CBookingsAdapter extends BaseAdapter {

    Context context;
    ArrayList items;
    Resources res;
    LayoutInflater inflater;
    /*************  CustomAdapter Constructor *****************/
    public CBookingsAdapter(Context pcontext, ArrayList listOfItems) {

        /********** Take passed values **********/
        context = pcontext;
        items=listOfItems;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {

        if(items == null)
            return 0;

        return items.size();
    }

    @Override
    public Object getItem(int position) {

        if(items == null)
            return null;

        return items.get(position);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View convertView = view;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            convertView = inflater.inflate(R.layout.booking_item_layout, null);

            TextView txtvbooking = (TextView) convertView.findViewById(R.id.txtvbookingitem);
            CBookingModel current = (CBookingModel)items.get(i);
            txtvbooking.setText("ID: "+current.bookingid+" - Cost "+current.cost+" -  Customer Name: "+current.customername
                    +System.getProperty("line.separator")+"Pickup: "+current.pickup
                    +System.getProperty("line.separator")+"Destination: "+current.destination
                    +System.getProperty("line.separator")+"Time: "+current.bookingdatetime+" - Contact Number: "+current.contactnumber);

        }

        return convertView;
    }
}
