package com.expensetracker.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class MonthSpinnerAdapter extends ArrayAdapter<String> {

    int type;
    static int NORMAL = 1, HEADER = 2;

    public MonthSpinnerAdapter(Context context, ArrayList<String> arrayList, int type) {
        super(context, 0, arrayList);
        this.type = type;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            if(type == NORMAL)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.month_spinner_item, parent, false);
            else
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.month_spinner_item2, parent, false);
        }
        TextView textViewName = convertView.findViewById(R.id.Text);
        String currentItem = getItem(position);

        if (currentItem != null) {
            textViewName.setText(currentItem);
        }

        return convertView;
    }
}
