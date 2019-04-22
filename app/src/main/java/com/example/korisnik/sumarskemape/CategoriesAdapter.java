package com.example.korisnik.sumarskemape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Miljan on 7/5/2016.
 */
public class CategoriesAdapter extends ArrayAdapter<String> {

    private List<String> categories;
    private int resource;
    private Context context;

    public CategoriesAdapter(Context context, int resource, List<String> categories) {
        super(context, resource, categories);

        this.categories = categories;
        this.resource = resource;
        this.context = context;
    }

    private class ViewHolder {
        private ImageView image;
        private TextView txt_category;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.category_list_row, parent, false);

            viewHolder.txt_category = (TextView) convertView.findViewById(R.id.txt_cat);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //char veliko = categories.get(position).toCharArray()[0];
        viewHolder.txt_category.setText(categories.get(position));

        return convertView;
    }

}
