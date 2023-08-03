package com.expensetracker.expensetracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ReadSMSFragment extends Fragment {

    TextView SMSCount, Text;
    ProgressBar ProgressBar;
    int count = 1, total = 1, progress = 0, delay = 10;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read_sm, container, false);
        SMSCount = view.findViewById(R.id.SMSCount);
        Text = view.findViewById(R.id.text2);
        ProgressBar = view.findViewById(R.id.ProgressBar);
        MainActivity.BottomNavigationBar.setVisibility(View.GONE);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                SMSCount.setText(count + " / " + total);
                ProgressBar.setProgress((int) (count * 100.0) / total);

                if(total != count)
                    handler.postDelayed(this::run, 10);
                else
                    Text.setText("Saving...");
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.BottomNavigationBar.setVisibility(View.VISIBLE);
    }
}
