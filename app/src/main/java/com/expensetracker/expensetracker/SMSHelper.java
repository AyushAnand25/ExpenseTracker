package com.expensetracker.expensetracker;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.expensetracker.expensetracker.MainActivity.userId;

public class SMSHelper {

    private Context context;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<SMS> smsArrayList = new ArrayList<>();
    Cursor cursor;
    ReadSMSFragment readSMSFragment;
    boolean first_time = false;

    SMSHelper(Context context) {
        this.context = context;
    }

    void readSMS() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", MODE_PRIVATE);
        final Long[] lastSMSTime = {sharedPreferences.getLong("LastSMSTime", -1L)};
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        Long month_start = 0L, year_start = 0L;
        readSMSFragment = new ReadSMSFragment();

        try {
            month_start = monthFormat.parse(monthFormat.format(new Date())).getTime();
            year_start = yearFormat.parse(yearFormat.format(new Date())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (lastSMSTime[0] == null || lastSMSTime[0] == -1L) {

            firestore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Long lastSMSTime = documentSnapshot.getLong("LastSMSTime");
                    if(lastSMSTime == null || lastSMSTime == -1L)
                        lastSMSTime = -1L;

                    //cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, "date > ?", new String[]{String.valueOf(month_start)}, null);
                    first_time = true;

                    try {
                        cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, "date > ?", new String[]{String.valueOf(lastSMSTime)}, null);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, readSMSFragment, "").addToBackStack(null).commit();
                        getSMS();
                    }catch (Exception ignored){ /* No Permission */

                        Log.i("Test", ignored.getMessage());
                    }
                }
            });
        } else {

            try {
                cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, "date > ?", new String[]{String.valueOf(lastSMSTime[0])}, null);
                getSMS();
            } catch (Exception ignored) { /* No Permission */}
        }

    }


    void getSMS()
    {
        int delay = 1;
        Thread thread = new Thread() {
            @Override
            public void run() {

                int read_total = 1, count = 0;
                if (cursor != null && cursor.moveToFirst()) {

                    readSMSFragment.delay = delay;
                    readSMSFragment.total = cursor.getCount();

                    do {
                        String id = cursor.getString(0);
                        String address = cursor.getString(2);
                        Long time = cursor.getLong(4);
                        String body = cursor.getString(12);


                        if (body.toLowerCase().contains("credit") || body.toLowerCase().contains("debit")) {
                            count++;
                            getSMSDetails(id, address, time, body);
                        }

                        if (first_time) {
                            try {
                                readSMSFragment.count = read_total;
                            } catch (Exception ignored) {
                            }
                        }

                        //Read messages slowly...
                        if (first_time) {
                            try {
                                sleep(delay);
                            } catch (InterruptedException ignored) {
                            }
                        }


//                if (count == 100)
//                    break;

                        read_total++;

                    } while (cursor.moveToNext());
                } else {
                    // empty box, no SMS
                    Log.i("Test", "No");
                }

                if (first_time) {
                    TransactionData transactionData = new TransactionData(context);
                    transactionData.addSMSTransactions(smsArrayList, readSMSFragment);
                } else if (smsArrayList.size() > 0) {
                    SMSBottomSheet smsBottomSheet = new SMSBottomSheet(smsArrayList);
                    smsBottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "SMSBottomSheet");
                }
            }
        };
        thread.start();
    }

    private void getSMSDetails(String id, String address, Long time, String body) {

        String body_full = body;
        body = body.toLowerCase();
        String account_no = "", payment_method = "";
        double amount = 0.0, balance = 0.0;
        int type = -1;

        String amount_regex = "[rR][sS]\\.?\\s*[,\\d]+\\.?\\d{0,2}|[iI][nN][rR]\\.?\\s*[,\\d]+\\.?\\d{0,2}";
        String payment_method_regex = "(through|thru)\\s*\\w+";
        String balance_regex = "bal(alance)?\\W*([rR][sS]\\.?\\s*[,\\d]+\\.?\\d{0,2}|[iI][nN][rR]\\.?\\s*[,\\d]+\\.?\\d{0,2})";
        String account_no_regex = "[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}";

        String mydata = "bal Rs. 3,785.30";
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(account_no_regex);
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            account_no = matcher.group();

            body = body.substring(matcher.end() + 1);
        }


        //Check Debit or Credit
        if (body.contains("debited")) {
            type = Transaction.DEBIT;
            body = body.substring(body.indexOf("debited") + "debited".length());
        } else if (body.contains("credited")) {
            type = Transaction.CREDIT;
            body = body.substring(body.indexOf("credited") + "credited".length());
        }


        pattern = Pattern.compile(amount_regex);
        matcher = pattern.matcher(body);
        if (matcher.find()) {

            String amount_string = matcher.group();
            amount_string = amount_string.replaceAll("inr.", "");
            amount_string = amount_string.replaceAll("inr", "");
            amount_string = amount_string.replaceAll("rs.", "");
            amount_string = amount_string.replaceAll("rs", "");
            amount_string = amount_string.replaceAll(",", "");
            amount_string = amount_string.replaceAll(" ", "");

            try {
                amount = Double.parseDouble(amount_string);
            } catch (Exception e) {
            }

            body = body.substring(matcher.end() + 1);
        }


        pattern = Pattern.compile(payment_method_regex);
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            payment_method = matcher.group();
            payment_method = payment_method.replaceAll("through", "");
            payment_method = payment_method.replaceAll("thru", "");
            payment_method = payment_method.replaceAll(" ", "");
            payment_method = payment_method.toUpperCase();

            body = body.substring(matcher.end() + 1);
        }

        pattern = Pattern.compile(balance_regex);
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            String balance_string = matcher.group();
            balance_string = balance_string.replaceAll(",", "");
            for (int i = 0; i < matcher.group().length(); i++) {
                if (balance_string.charAt(i) >= '0' && balance_string.charAt(i) <= '9') {
                    balance_string = balance_string.substring(i);
                    break;
                }
            }
            try {
                balance = Double.parseDouble(balance_string);
            } catch (Exception e) {
            }
            body = body.substring(matcher.end() + 1);
        }

        Log.i("Test", id + " " + account_no + " " + type + " " + amount + " " + payment_method + " " + balance + " ");
        if (account_no.equals("") || type == -1 || amount == 0) {
            //Log.i("Test", account_no + " " +type + " " +amount + " " +payment_method + " " +balance + " ");
            Log.i("Test", body_full);
        } else
            smsArrayList.add(new SMS(id, type, "General", payment_method, "PNB", address, body_full, account_no, time, amount, balance, false, ""));
    }
}
