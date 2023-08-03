package com.expensetracker.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.expensetracker.expensetracker.MainActivity.profile_pic;

public class HomeFragment extends Fragment {

    private TextView MonthText, SpendsText, PendingText, DebitAmount, DailyExpense, CreditAmount, AccountBalance, SafeDailyExpense, TodayText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView ToolbarName;
    CircleImageView AccountButton;
    private Spinner MonthSpinner;
    private Button Statistics;
    private PieChart SpendsPieChart;
    private RecyclerView TransactionsRecycler;

    private String current_month = "";
    private ArrayList<String> months = new ArrayList<>(Arrays.asList("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"));
    private ArrayList<String> spinnerData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        swipeRefreshLayout = view.findViewById(R.id.SwipeRefreshLayout);
        AccountButton = view.findViewById(R.id.AccountButton);
        MonthText = view.findViewById(R.id.MonthText);
        SpendsText = view.findViewById(R.id.SpendsText);
        PendingText = view.findViewById(R.id.PendingText);
        Statistics = view.findViewById(R.id.Statistics);
        DebitAmount = view.findViewById(R.id.DebitAmount);
        CreditAmount = view.findViewById(R.id.CreditAmount);
        DailyExpense = view.findViewById(R.id.DailyAmount);
        AccountBalance = view.findViewById(R.id.BalanceAmount);
        SafeDailyExpense = view.findViewById(R.id.SafeDailyAmount);
        SafeDailyExpense = view.findViewById(R.id.SafeDailyAmount);
        MonthSpinner = view.findViewById(R.id.MonthSpinner);
        SpendsPieChart = view.findViewById(R.id.PieChart);
        TodayText = view.findViewById(R.id.TransactionsTodayText);
        TransactionsRecycler = view.findViewById(R.id.RecyclerView);
        ToolbarName.setText("Hi " + MainActivity.name.split(" ")[0]);

        if (profile_pic != null && !profile_pic.equals(""))
            Glide.with(getContext()).load(profile_pic).centerCrop().into(AccountButton);
        else
            Glide.with(getContext()).load(R.drawable.wallet_icon22).into(AccountButton);

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
        for (int i = 0; i < 12; i++) {
            if (current_month.equals(months.get(i))) {
                MonthSpinner.setSelection(i);
                break;
            }
        }

        SMSHelper smsHelper = new SMSHelper(getContext());
        smsHelper.readSMS();


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

        Statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right).add(R.id.Frame_Container, new StatisticsFragment(), "Statistics").addToBackStack("Statistics").commit();
            }
        });

        AccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.Frame_Container, new AccountFragment(), "ACCOUNT").addToBackStack("Account").commit();
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

        return view;
    }

    void refresh() {
        TransactionData transactionData = new TransactionData(getContext(), MonthText, SpendsText, PendingText, DebitAmount, DailyExpense, CreditAmount, AccountBalance, SafeDailyExpense, SpendsPieChart, TodayText, TransactionsRecycler);
        transactionData.setSpinnerSelection(spinnerData.get(MonthSpinner.getSelectedItemPosition()));
    }
}
