package com.expensetracker.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

public class TransactionData {

    private Context context;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<Transaction> transactionsList = new ArrayList<>();
    private ArrayList<Transaction> ignoredTransactions = new ArrayList<>();
    private ArrayList<Transaction> today_transactions = new ArrayList<>();
    private ArrayList<String> current_months = new ArrayList<>();
    private HashMap<String, Double> categories = new HashMap<>();
    private Double debit = 0.0, credit = 0.0, spends = 0.0, daily_expenses = 0.0, safe_limit = 0.0;
    private Float budget = 0.0f;
    Long start, end;
    private String month_name = "";
    private PieChart SpendsPieChart;
    private BarChart SpendsBarChart;
    private RecyclerView TransactionsRecycler, IgnoredRecycler;
    private TextView MonthText, SpendsText, PendingText, DebitAmount, DailyExpenses, CreditAmount, AccountBalance, SafeLimit;
    private TextView TodayText, IgnoredText;
    private ProgressBar Loading;

    private int TYPE, HOME = 1, STATS = 2, TRANSACTIONS = 3;
    private int safe_limit_called = 0;
    private boolean pending_included = false;

    TransactionData(Context context) {
        this.context = context;
    }

    TransactionData(Context context, RecyclerView transactionsRecycler, ProgressBar loading) {
        this.context = context;
        TransactionsRecycler = transactionsRecycler;
        Loading = loading;
        TYPE = TRANSACTIONS;
    }

    TransactionData(Context context, TextView monthText, TextView spendsText, TextView pendingText, TextView debitAmount, TextView dailyExpenses, TextView creditAmount, TextView accountBalance, TextView safeLimit, PieChart spendsPieChart, TextView todayText, RecyclerView transactionsRecycler) {
        this.context = context;
        SpendsPieChart = spendsPieChart;
        TodayText = todayText;
        TransactionsRecycler = transactionsRecycler;
        MonthText = monthText;
        SpendsText = spendsText;
        PendingText = pendingText;
        DebitAmount = debitAmount;
        DailyExpenses = dailyExpenses;
        CreditAmount = creditAmount;
        AccountBalance = accountBalance;
        SafeLimit = safeLimit;
        TYPE = HOME;
    }

    TransactionData(Context context, TextView monthText, TextView spendsText, TextView debitAmount, TextView dailyExpenses, TextView creditAmount, PieChart spendsPieChart, BarChart spendsBarChart, TextView ignoredText, RecyclerView ignoredRecycler) {
        this.context = context;
        IgnoredText = ignoredText;
        IgnoredRecycler = ignoredRecycler;
        SpendsPieChart = spendsPieChart;
        SpendsBarChart = spendsBarChart;
        MonthText = monthText;
        SpendsText = spendsText;
        DebitAmount = debitAmount;
        DailyExpenses = dailyExpenses;
        CreditAmount = creditAmount;
        TYPE = STATS;
    }

    void setSpinnerSelection(String selection) {

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
        String year = yearFormat.format(new Date());
        Long today;

        try {
            today = dateFormat.parse(dateFormat.format(new Date(new Date().getTime() + 60 * 60 * 24 * 1000L))).getTime();

            if (selection.equals("Custom"))
                showCalendar();
            else if (selection.contains("Last")) {

                int months_count;
                if (selection.equals("Last 1 month"))
                    months_count = 1;
                else if (selection.equals("Last 3 months"))
                    months_count = 3;
                else
                    months_count = 6;

                try {
                    start = today - 60 * 60 * 24 * 1000 * 30L * months_count;
                    end = today;
                    getTransactions(getMonths(start, today), start, end);
                } catch (ParseException ignored) {
                }
            } else {
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");
                Date month_start = monthFormat.parse(selection + " " + year);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(month_start.getTime());
                int days = calendar.getActualMaximum(Calendar.DATE);
                Date month_end = new Date(month_start.getTime() + 60 * 60 * 24 * 1000L * days);
                getTransactions(new ArrayList<>(Arrays.asList(selection + " " + year)), month_start.getTime(), month_end.getTime());
            }
        } catch (ParseException ignored) {
        }
    }


    private void getTransactions(ArrayList<String> months, Long start, Long end) {

        this.start = start;
        this.end = end;
        current_months.clear();
        current_months.addAll(months);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Log.i("Test", dateFormat.format(start)+ " " + dateFormat.format(end));

        firestore.collection("Transactions").document(MainActivity.userId).collection("Months").whereIn("Month", months).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                transactionsList.clear();
                debit = credit = 0.0;

                SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", MODE_PRIVATE);
                budget = sharedPreferences.getFloat("MonthlyBudget", 0f);

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    HashMap<String, Object> transactions = (HashMap<String, Object>) documentSnapshot.get("Transactions");
                    HashMap<String, Object> doc = new HashMap<>();

                    if (transactions != null) {
                        for (String key : transactions.keySet()) {
                            HashMap<String, Object> transaction = (HashMap<String, Object>) transactions.get(key);
                            String type = (String) transaction.get("Type");
                            String category = (String) transaction.get("Category");
                            Long time = (Long) transaction.get("Time");
                            int transaction_type;
                            Double amount, signed_amount;
                            Boolean ignore = false;

                            if (time >= start && time <= end) {

                                try {
                                    amount = (Double) transaction.get("Amount");
                                } catch (Exception e) {
                                    amount = ((Long) transaction.get("Amount")).doubleValue();
                                }

                                if (transaction.containsKey("Ignore"))
                                    ignore = (Boolean) transaction.get("Ignore");

                                if (type.equals("Debit")) {

                                    if (!ignore)
                                        debit += amount;
                                    signed_amount = amount;
                                    transaction_type = Transaction.DEBIT;
                                } else {

                                    if (!ignore)
                                        credit += amount;
                                    signed_amount = -amount;
                                    transaction_type = Transaction.CREDIT;
                                }

                                if (!ignore) {
                                    if (categories.containsKey(category))
                                        categories.put(category, new Double(categories.get(category) + signed_amount).doubleValue());
                                    else
                                        categories.put(category, new Double(signed_amount).doubleValue());
                                }

                                transactionsList.add(new Transaction((String) transaction.get("Id"), (String) transaction.get("SMS"),
                                        transaction_type, (String) transaction.get("Category"), (String) transaction.get("Details"), (String) transaction.get("AccountNo"),
                                        (String) transaction.get("PaymentMethod"), (Long) transaction.get("Time"), amount, ignore));
                            }

                            doc.put(key, transaction);
                        }
                    }
                }

                transactionsList.sort(new transactionComparator());
                spends = debit - credit;
                month_name = months.get(0).split(" ")[0];

                Long current_end = end;
                if (current_end > new Date().getTime())
                    current_end = new Date().getTime() + 60 * 60 * 24 * 1000L;

                daily_expenses = spends / ((current_end - start) / (60 * 60 * 24 * 1000L));

                if (TYPE == HOME || TYPE == STATS) {
                    //MonthText.setText("Spends in " + months.get(0).split(" ")[0]);
                    SpendsText.setText(String.format("₹ %.1f", spends));
                    DebitAmount.setText(String.format("₹ %.1f", debit));
                    CreditAmount.setText(String.format("₹ %.1f", credit));
                    DailyExpenses.setText(String.format("₹ %.1f", daily_expenses));
                    setPieChart();


                    if (TYPE == STATS) {
                        setBarChart("Daily");
                        setIgnoredTransactions();
                    }

                    if (TYPE == HOME) {
                        try {
                            setTodayTransactions();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (months.size() == 1 && months.get(0).equals(new SimpleDateFormat("MMMM yyyy").format(new Date())))
                            setSafeLimit();
                    }
                } else
                    setTransactions();

                if (TYPE == TRANSACTIONS)
                    Loading.setVisibility(View.GONE);
            }
        });

        //Get Pending Transactions
        if (TYPE == HOME)
            getPendingTransactions(start, end);


        //Call only on current Month
        if (TYPE == HOME && months.size() == 1 && months.get(0).equals(new SimpleDateFormat("MMMM yyyy").format(new Date()))) {
            firestore.collection("Users").document(MainActivity.userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Double balance = documentSnapshot.getDouble("AccountBalance");
                    AccountBalance.setText(String.format("₹ %.1f", balance));
                }
            });
        }
    }

    void setTexts() {

        Long current_end = end;
        if (current_end > new Date().getTime())
            current_end = new Date().getTime() + 60 * 60 * 24 * 1000L;

        daily_expenses = spends / ((current_end - start) / (60 * 60 * 24 * 1000L));

        //MonthText.setText("Spends in " + months.get(0).split(" ")[0]);
        SpendsText.setText(String.format("₹ %.1f", spends));
        DebitAmount.setText(String.format("₹ %.1f", debit));
        CreditAmount.setText(String.format("₹ %.1f", credit));
        DailyExpenses.setText(String.format("₹ %.1f", daily_expenses));
        setPieChart();

        if (TYPE == HOME) {
            if (current_months.size() == 1 && current_months.get(0).equals(new SimpleDateFormat("MMMM yyyy").format(new Date())))
                setSafeLimit();
        }
    }


    private void getPendingTransactions(Long start, Long end) {
        firestore.collection("PendingPayments").document(MainActivity.userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                HashMap<String, Object> transactions = (HashMap<String, Object>) documentSnapshot.get("PendingPayments");
                double pending = 0.0;
                Double pending_credit = 0.0, pending_debit = 0.0;

                if (transactions != null) {

                    for (String key : transactions.keySet()) {
                        HashMap<String, Object> transaction = (HashMap<String, Object>) transactions.get(key);
                        String type = (String) transaction.get("Type");
                        Double amount;
                        Long time = (Long) transaction.get("Time");

                        if (time >= start && time <= end) {
                            try {
                                amount = (Double) transaction.get("Amount");
                            } catch (Exception e) {
                                amount = ((Long) transaction.get("Amount")).doubleValue();
                            }

                            if (type.equals("Debit"))
                                pending_debit += amount;
                            else
                                pending_credit += amount;
                        }
                    }
                    pending = pending_debit - pending_credit;
                }

                if (pending > 0) {
                    PendingText.setText(String.format("+ ₹ %.0f", pending));
                    PendingText.setTextColor(context.getResources().getColor(R.color.blue3));
                } else if (pending < 0) {
                    PendingText.setText(String.format("- ₹ %.0f", Math.abs(pending)));
                    PendingText.setTextColor(context.getResources().getColor(R.color.purple1));
                } else
                    PendingText.setText("");


                //Add Pending to Spends
                double finalPending = pending;
                Double finalPending_credit = pending_credit;
                Double finalPending_debit = pending_debit;
                PendingText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(pending_included) {

                            spends -= finalPending;
                            credit -= finalPending_credit;
                            debit -= finalPending_debit;

                            SpendsText.setText(String.format("₹ %.1f", spends));
                            if (finalPending > 0)
                                PendingText.setText(String.format("+ ₹ %.0f", finalPending));
                            else if (finalPending < 0)
                                PendingText.setText(String.format("- ₹ %.0f", Math.abs(finalPending)));
                        }
                        else {
                            spends += finalPending;
                            credit += finalPending_credit;
                            debit += finalPending_debit;

                            SpendsText.setText(String.format("₹ %.1f", spends));
                            PendingText.setText("+ ₹ 0");
                        }

                        setTexts();
                        pending_included = !pending_included;
                    }
                });

            }
        });

    }


    private void setPieChart() {

        ArrayList<PieEntry> pieEntries = new ArrayList();

        for (String key : categories.keySet())
            pieEntries.add(new PieEntry(categories.get(key).intValue(), key));

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#1294f6"));
        colors.add(Color.parseColor("#3a76d6"));
        colors.add(Color.parseColor("#0070df"));
        colors.add(Color.parseColor("#8245e6"));
        colors.add(Color.parseColor("#6d1bf1"));
        colors.add(Color.parseColor("#f28e2c"));
        colors.add(Color.parseColor("#f78414"));
        colors.add(Color.parseColor("#e04857"));


        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setValueTypeface(ResourcesCompat.getFont(context, R.font.proxima_nova_medium));
        pieDataSet.setValueTextColors(colors);
        pieDataSet.setColors(colors);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        PieData pieData = new PieData(pieDataSet);
        SpendsPieChart.setData(pieData);

        //Set Description
        Description description = new Description();
        description.setEnabled(false);
        SpendsPieChart.setDescription(description);
        SpendsPieChart.setTransparentCircleRadius(0);
        SpendsPieChart.setDrawEntryLabels(false);

        //Customize Legends
        Legend l = SpendsPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);
        l.setTypeface(ResourcesCompat.getFont(context, R.font.proxima_nova));
        l.setEnabled(true);

        //Format Values
        HashMap<String, Double> categories2 = new HashMap<>(categories);
        pieDataSet.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {

                String category = "";
                for(String key : categories2.keySet())
                {
                    if(categories2.get(key) == value)
                    {
                        category = key;
                        break;
                    }
                }
                //categories2.remove(category);
                return String.format("₹%.0f (" + category + ")", value);
            }
        });


        //Show Transactions on Click
        SpendsPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                String category = "";
                for(String key : categories.keySet())
                {
                    if(categories.get(key) == e.getY())
                        category = key;
                }
                TransactionsBottomSheet transactionsBottomSheet = new TransactionsBottomSheet();
                transactionsBottomSheet.setCategoryData(category, transactionsList);
                transactionsBottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "Transactions");
            }

            @Override
            public void onNothingSelected() {

            }
        });

        SpendsPieChart.notifyDataSetChanged();
        SpendsPieChart.invalidate();
    }


    private void setSafeLimit() {

        Calendar calendar = Calendar.getInstance();
        int days_left = calendar.getActualMaximum(Calendar.DATE) - calendar.get(Calendar.DAY_OF_MONTH) + 1;

        if (budget != 0f)
            safe_limit = (budget - spends) / (days_left);

        SafeLimit.setText(String.format("₹ %.1f", safe_limit));
    }


    void setBarChart(String interval) {

        HashMap<String, Integer> expenses = getDailyExpenses(transactionsList, interval);
        ArrayList<String> expensesTexts = new ArrayList<>(expenses.keySet());
        expensesTexts.sort(new dayComparator(interval));

        Calendar calendar = Calendar.getInstance();
        int days = calendar.getActualMaximum(Calendar.DATE);
        int today_date = calendar.get(Calendar.DAY_OF_MONTH);

        if (interval.equals("Monthly"))
            days = 1;

        Double daily_budget = budget.doubleValue() / days;

        //Clear the previously drawn chart
        if (SpendsBarChart.getData() != null)
            SpendsBarChart.clearValues();
        SpendsBarChart.fitScreen();
        SpendsBarChart.notifyDataSetChanged();
        SpendsBarChart.clear();
        SpendsBarChart.invalidate();

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < expensesTexts.size(); i++)
            barEntries.add(new BarEntry(i, expenses.get(expensesTexts.get(i))));


        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        //Changing the color of the bar

        ArrayList<Integer> colors = new ArrayList<>();
        for (String text : expensesTexts) {
            if (expenses.get(text) > daily_budget)
                colors.add(ContextCompat.getColor(context, R.color.zomatoRed));
            else
                colors.add(ContextCompat.getColor(context, R.color.blue3));
        }
        barDataSet.setColors(colors);
        barDataSet.setValueTextColors(colors);


        //Setting the size of the form in the legend
        barDataSet.setFormSize(15f);
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(true);
        //setting the text size of the value of the bar
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTypeface(ResourcesCompat.getFont(context, R.font.proxima_nova_medium));

        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹ " + value;
            }
        });

        BarData data = new BarData(barDataSet);
        SpendsBarChart.setData(data);

        Description description = new Description();
        description.setEnabled(false);
        SpendsBarChart.setDescription(description);


        XAxis xAxis = SpendsBarChart.getXAxis();
        //change the position of x-axis to the bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
        xAxis.setGranularity(1f);
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false);
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false);

        xAxis.setTypeface(ResourcesCompat.getFont(context, R.font.proxima_nova_medium));
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.black));
        xAxis.setYOffset(10);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float index) {
                // return the string value
                try {
                    return expensesTexts.get((int) index);
                } catch (Exception e) {
                }
                return "";
            }
        });

        YAxis leftAxis = SpendsBarChart.getAxisLeft();
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);

        YAxis rightAxis = SpendsBarChart.getAxisRight();
        //hiding the right y-axis line, default true if not set
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);


        //Open Transactions on that Day.
        SpendsBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                if (interval.equals("Daily")) {
                    TransactionsBottomSheet transactionsBottomSheet = new TransactionsBottomSheet();
                    transactionsBottomSheet.setDayData(expensesTexts.get((int) e.getX()), transactionsList);
                    transactionsBottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "Transactions");
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });


        int span = 6;
        SpendsBarChart.setDrawGridBackground(false);
        SpendsBarChart.getLegend().setEnabled(false);
        SpendsBarChart.setVisibleXRangeMaximum(span);
        SpendsBarChart.animateXY(0, 500);

        if(interval.equals("Daily"))
        SpendsBarChart.moveViewToX(today_date - span + 1);
        SpendsBarChart.invalidate();

    }


    private HashMap<String, Integer> getDailyExpenses(ArrayList<Transaction> transactions, String interval) {

        HashMap<String, Integer> dailyExpenses = new HashMap<>();
        SimpleDateFormat simpleDateFormat;
        if (interval.equals("Daily")) {

            simpleDateFormat = new SimpleDateFormat("dd MMM");
            Long date = start;

            while(date < end)
            {
                dailyExpenses.put(simpleDateFormat.format(date), 0);
                date += 60*60*24*1000L;
            }
        }
        else
            simpleDateFormat = new SimpleDateFormat("MMM");

        for (Transaction transaction : transactions) {

            if (!transaction.ignore) {
                Double amount = transaction.amount;
                String day = simpleDateFormat.format(transaction.time);
                if (transaction.type == Transaction.CREDIT)
                    amount = -amount;

                if (dailyExpenses.containsKey(day))
                    dailyExpenses.put(day, dailyExpenses.get(day) + amount.intValue());
                else
                    dailyExpenses.put(day, amount.intValue());
            }
        }
        return dailyExpenses;
    }


    private void setTodayTransactions() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        Long today = simpleDateFormat.parse(simpleDateFormat.format(new Date())).getTime();

        for (Transaction transaction : transactionsList) {
            if (transaction.time >= today && transaction.time <= today + 60 * 60 * 24 * 1000L)
                today_transactions.add(transaction);
        }

        //Today
        RecyclerAdapter_Transactions recyclerAdapter_transactions = new RecyclerAdapter_Transactions(context, today_transactions, RecyclerAdapter_Transactions.TODAY);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        TransactionsRecycler.setLayoutManager(linearLayoutManager);
        TransactionsRecycler.setAdapter(recyclerAdapter_transactions);


        if(today_transactions.isEmpty())
            TodayText.setVisibility(View.GONE);
        else
            TodayText.setVisibility(View.VISIBLE);

    }


    private void setIgnoredTransactions()
    {
        for (Transaction transaction : transactionsList) {
            if (transaction.time >= start && transaction.time <= end && transaction.ignore)
                ignoredTransactions.add(transaction);
        }

        //Ignored
        RecyclerAdapter_Transactions recyclerAdapter_transactions2 = new RecyclerAdapter_Transactions(context, ignoredTransactions, RecyclerAdapter_Transactions.TODAY);
        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        IgnoredRecycler.setLayoutManager(linearLayoutManager2);
        IgnoredRecycler.setAdapter(recyclerAdapter_transactions2);


        if(ignoredTransactions.isEmpty())
            IgnoredText.setVisibility(View.GONE);
        else
            IgnoredText.setVisibility(View.VISIBLE);
    }


    private void setTransactions() {
        RecyclerAdapter_Transactions recyclerAdapter_transactions = new RecyclerAdapter_Transactions(context, transactionsList, RecyclerAdapter_Transactions.ALL);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        TransactionsRecycler.setLayoutManager(linearLayoutManager);
        TransactionsRecycler.setAdapter(recyclerAdapter_transactions);
    }


    void addTransaction(Transaction transaction, boolean isPending, Fragment fragment, boolean back) {

        HashMap<String, Object> document = new HashMap<>();
        HashMap<String, Object> transactions = new HashMap<>();
        HashMap<String, Object> transactionMap = new HashMap<>();
        DocumentReference reference;
        String collection_name = "Transactions";

        if (isPending)
            collection_name = "PendingPayments";

        transactionMap.put("Amount", transaction.amount);
        transactionMap.put("AccountNo", transaction.account_no);
        transactionMap.put("Category", transaction.category);
        transactionMap.put("Details", transaction.details);
        transactionMap.put("Id", transaction.id);
        transactionMap.put("PaymentMethod", transaction.payment_method);
        transactionMap.put("SMSId", "");
        transactionMap.put("Time", transaction.time);

        if (transaction.type == Transaction.DEBIT)
            transactionMap.put("Type", "Debit");
        else
            transactionMap.put("Type", "Credit");

        transactions.put(transactionMap.get("Id").toString(), transactionMap);
        document.put(collection_name, transactions);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        if (!isPending)
            reference = firestore.collection(collection_name).document(MainActivity.userId).collection("Months").document(dateFormat.format(new Date(transaction.time)));
        else
            reference = firestore.collection(collection_name).document(MainActivity.userId);


        reference.set(document, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (fragment instanceof TransactionsFragment)
                    ((TransactionsFragment) fragment).refresh();
                else if(fragment instanceof PendingPaymentsFragment)
                    ((PendingPaymentsFragment) fragment).getTransactions();

                if(back)
                ((AppCompatActivity) context).onBackPressed();
            }
        });
    }


    void addSMSTransactions(ArrayList<SMS> smsArrayList, Fragment fragment) {

        HashMap<String, HashMap<String, Object>> documents = new HashMap<>();
        String collection_name = "Transactions";
        Long lastSMSTime = -1L;
        Double balance = 0.0;

        if (!smsArrayList.isEmpty())
            balance = smsArrayList.get(0).balance;

        for (SMS sms : smsArrayList) {
            HashMap<String, Object> transactions = new HashMap<>();
            HashMap<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("Amount", sms.amount);
            transactionMap.put("AccountNo", sms.account_no);
            transactionMap.put("Category", sms.category);
            transactionMap.put("Details", sms.details);
            transactionMap.put("Id", UtilityFunctions.getRandomKey());
            transactionMap.put("PaymentMethod", sms.payment_method);
            transactionMap.put("SMSId", sms.sms_id);
            transactionMap.put("SMS", sms.body);
            transactionMap.put("Bank", sms.bank);
            transactionMap.put("Time", sms.date);

            if (lastSMSTime == -1L || lastSMSTime < sms.date)
                lastSMSTime = sms.date;

            if (sms.type == Transaction.DEBIT)
                transactionMap.put("Type", "Debit");
            else
                transactionMap.put("Type", "Credit");
            transactions.put(transactionMap.get("Id").toString(), transactionMap);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
            String month = simpleDateFormat.format(new Date(sms.date));

            if (!documents.containsKey(month))
                documents.put(month, transactions);
            else
                documents.get(month).put(transactionMap.get("Id").toString(), transactionMap);
        }


        WriteBatch batch = firestore.batch();
        for (String month : documents.keySet()) {

            HashMap<String, Object> document = new HashMap<>();
            document.put("Transactions", documents.get(month));
            document.put("Month", month);
            batch.set(firestore.collection(collection_name).document(MainActivity.userId).collection("Months").document(month), document, SetOptions.merge());
        }

        Long finalLastSMSTime = lastSMSTime;
        Double finalBalance = balance;
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if(!smsArrayList.isEmpty())
                firestore.collection("Users").document(MainActivity.userId).update("LastSMSTime", finalLastSMSTime, "AccountBalance", finalBalance);

                SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putLong("LastSMSTime", finalLastSMSTime);
                myEdit.apply();

                if(fragment instanceof ReadSMSFragment) {

                    Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((AppCompatActivity) context).onBackPressed();
                        }
                    }, 2000);
                }

                ((HomeFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("HomeFragment")).refresh();
            }
        });
    }


    private void showCalendar() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Toast.makeText(context, "Please select Start Date", Toast.LENGTH_SHORT).show();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                //Start Date
                calendar.set(i, i1, i2);
                Long start = new Date(calendar.getTimeInMillis()).getTime();
                Log.i("Test", dateFormat.format(start));
                Long today = new Date().getTime();

                try {

                    if (start <= today) {
                        //End Date
                        Toast.makeText(context, "Please select End Date", Toast.LENGTH_SHORT).show();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                calendar.set(i, i1, i2);
                                Long end = new Date(calendar.getTimeInMillis() + 60 * 60 * 24 * 1000L).getTime();

                                if (end < start)
                                    UtilityFunctions.showSnackbar("End Date can't be less than Start Date.");
                                else {
                                    Log.i("Test", dateFormat.format(end));
                                    try {
                                        getTransactions(getMonths(start, end), start, end);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    } else
                        UtilityFunctions.showSnackbar("Please select a valid Date.");
                } catch (Exception e) {
                    e.printStackTrace();
                    UtilityFunctions.showSnackbar("Please select a valid Date.");
                }

            }
        }, year, month, day);
        datePickerDialog.show();
    }


    private ArrayList<String> getMonths(Long start, Long end) throws ParseException {

        HashSet<String> monthsSet = new HashSet<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
        Long date = simpleDateFormat.parse(simpleDateFormat.format(new Date(start))).getTime();
        Long end_of_month = simpleDateFormat.parse(simpleDateFormat.format(new Date(end + 60 * 60 * 24 * 30 * 1000L))).getTime();

        while (date < end_of_month) {
            String month = simpleDateFormat.format(date);
            Log.i("Test", simpleDateFormat.format(new Date(date)));
            monthsSet.add(month);
            date += 60 * 60 * 24 * 25 * 1000L;
        }

        Log.i("Test", monthsSet + " in Custom");
        return new ArrayList<>(monthsSet);
    }


    class dayComparator implements Comparator<String> {

        SimpleDateFormat simpleDateFormat;

        dayComparator(String interval) {
            if (interval.equals("Daily"))
                simpleDateFormat = new SimpleDateFormat("dd MMM");
            else
                simpleDateFormat = new SimpleDateFormat("MMM");
        }

        public int compare(String a, String b) {
            try {
                if (simpleDateFormat.parse(a).getTime() < simpleDateFormat.parse(b).getTime())
                    return -1;
                else if (simpleDateFormat.parse(a).getTime() > simpleDateFormat.parse(b).getTime())
                    return 1;
                return 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }


    class transactionComparator implements Comparator<Transaction> {

        public int compare(Transaction a, Transaction b) {

            if (a.time < b.time)
                return 1;
            else if (a.time > b.time)
                return -1;

            return 0;
        }
    }

}
