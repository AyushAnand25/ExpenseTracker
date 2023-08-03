package com.expensetracker.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class StatisticsFragment extends Fragment {

    private TextView MonthText, SpendsText, DebitAmount, DailyExpense, CreditAmount, IgnoredText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView ToolbarName;
    private Button Back;
    private Spinner MonthSpinner;
    private PieChart SpendsPieChart;
    private BarChart SpendsBarChart;
    private TextView Daily, Monthly;
    private RecyclerView IgnoredRecycler;


    private String current_month = "";
    private ArrayList<String> months = new ArrayList<>(Arrays.asList("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"));
    private ArrayList<String> spinnerData = new ArrayList<>();
    TransactionData transactionData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        Back = view.findViewById(R.id.Back);
        swipeRefreshLayout= view.findViewById(R.id.SwipeRefreshLayout);
        MonthText = view.findViewById(R.id.MonthText);
        SpendsText = view.findViewById(R.id.SpendsText);
        DebitAmount = view.findViewById(R.id.DebitAmount);
        DailyExpense = view.findViewById(R.id.DailyAmount);
        CreditAmount = view.findViewById(R.id.CreditAmount);
        MonthSpinner = view.findViewById(R.id.MonthSpinner);
        SpendsPieChart = view.findViewById(R.id.PieChart);
        SpendsBarChart = view.findViewById(R.id.BarChart);
        Daily = view.findViewById(R.id.Daily);
        Monthly = view.findViewById(R.id.Monthly);
        IgnoredText = view.findViewById(R.id.IgnoredText);
        IgnoredRecycler = view.findViewById(R.id.IgnoredRecyclerView);
        ToolbarName.setText("Summary");
        MainActivity.BottomNavigationBar.setVisibility(View.GONE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 250);

        return view;
    }


    void loadData()
    {
        current_month = new SimpleDateFormat("MMMM").format(new Date());

        for (String month : months) {
            spinnerData.add(month);
            if (month.equals(current_month)) {
                spinnerData.add("Last 1 month");
                spinnerData.add("Last 3 months");
                spinnerData.add("Last 6 months");
                spinnerData.add("Custom");
                break;
            }
        }
        MonthSpinnerAdapter spinnerAdapter = new MonthSpinnerAdapter(getContext(), spinnerData, MonthSpinnerAdapter.NORMAL);
        MonthSpinner.setAdapter(spinnerAdapter);
        for(int i=0;i<12;i++)
        {
            if(current_month.equals(months.get(i))) {
                MonthSpinner.setSelection(i);
                break;
            }
        }


        //Fill Subjects in Spinner
        MonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Daily.setBackgroundResource(R.drawable.border_drawable3);
                Daily.setTextColor(ContextCompat.getColor(getContext(), R.color.blue3));
                Daily.setTypeface(ResourcesCompat.getFont(getContext(), R.font.proxima_nova_medium));
                Monthly.setBackgroundColor(Color.parseColor("#00000000"));
                Monthly.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                Monthly.setTypeface(ResourcesCompat.getFont(getContext(), R.font.proxima_nova));
                transactionData.setBarChart("Daily");
            }
        });

        Monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Monthly.setBackgroundResource(R.drawable.border_drawable3);
                Monthly.setTextColor(ContextCompat.getColor(getContext(), R.color.blue3));
                Monthly.setTypeface(ResourcesCompat.getFont(getContext(), R.font.proxima_nova_medium));
                Daily.setBackgroundColor(Color.parseColor("#00000000"));
                Daily.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                Daily.setTypeface(ResourcesCompat.getFont(getContext(), R.font.proxima_nova));
                transactionData.setBarChart("Monthly");
            }
        });


        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                refresh();
            }
        });
    }


    void refresh()
    {
        transactionData = new TransactionData(getContext(), MonthText, SpendsText, DebitAmount, DailyExpense, CreditAmount, SpendsPieChart, SpendsBarChart, IgnoredText, IgnoredRecycler);
        transactionData.setSpinnerSelection(spinnerData.get(MonthSpinner.getSelectedItemPosition()));
        Daily.setBackgroundResource(R.drawable.border_drawable3);
        Daily.setTextColor(ContextCompat.getColor(getContext(), R.color.blue3));
        Daily.setTypeface(ResourcesCompat.getFont(getContext(), R.font.proxima_nova_medium));
        Monthly.setBackgroundColor(Color.parseColor("#00000000"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.BottomNavigationBar.setVisibility(View.VISIBLE);
    }
}
