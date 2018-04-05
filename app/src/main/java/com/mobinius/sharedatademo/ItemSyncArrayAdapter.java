package com.mobinius.sharedatademo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;

import java.util.List;

/**
 * Created by prajna on 23/8/17.
 */

public class ItemSyncArrayAdapter  extends ArrayAdapter<QueryRow> {

    private List<QueryRow> list;
    private final Context context;

    public ItemSyncArrayAdapter(Context context, int resource, int textViewResourceId,int oo, List<QueryRow> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    private static class ViewHolder {
//        ImageView icon;
        TextView name;
        TextView place;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {

        if (itemView == null) {
//            Toast.makeText(context, "Total number of Items are:" + position, Toast.LENGTH_LONG).show();

            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.item_list, null);
            ViewHolder vh = new ViewHolder();
            vh.name = (TextView) itemView.findViewById(R.id.item_name);
//            vh.icon = (ImageView) itemView.findViewById(R.id.icon);
            vh.place = (TextView) itemView.findViewById(R.id.item_place);
            itemView.setTag(vh);
        }

        try {
//            Toast.makeText(context, "Total number of Items are:" + position, Toast.LENGTH_LONG).show();

            TextView label = ((ViewHolder)itemView.getTag()).name;
            TextView rollno = ((ViewHolder)itemView.getTag()).place;
            QueryRow row = getItem(position);
            SavedRevision currentRevision = row.getDocument().getCurrentRevision();
//            Object check = (Object) currentRevision.getProperty("check");
//            boolean isGroceryItemChecked = false;
//            if (check != null && check instanceof Boolean) {
//                isGroceryItemChecked = ((Boolean)check).booleanValue();
//            }
            String groceryItemText = (String) currentRevision.getProperty("name");
            String groceryItemText2 = (String) currentRevision.getProperty("place");
            label.setText(groceryItemText);
            rollno.setText(groceryItemText2);

//            ImageView icon = ((ViewHolder)itemView.getTag()).icon;
//            if(isGroceryItemChecked) {
////                icon.setImageResource(R.drawable.list_area___checkbox___checked);
//            }
//            else {
////                icon.setImageResource(R.drawable.list_area___checkbox___unchecked);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemView;
    }
}
