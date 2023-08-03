package com.expensetracker.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    static ConstraintLayout RootLayout, BottomNavigationBar;
    private ConstraintLayout HomeButton, TransactionsButton, PendingButton;
    private ImageView HomeIcon, TransactionsIcon, PendingIcon;
    private TextView HomeText, TransactionsText, PendingText;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    static HashMap<String, Category> categoriesMap = new HashMap<>();
    static HashMap<String, String> banksMap = new HashMap<>();

    Fragment activeFragment, homeFragment;
    int fragmentCounter = 1;
    ArrayList<Integer> fragment_breaks = new ArrayList<>();
    private HashMap<String, Integer> backStackMap = new HashMap<>();

    static String userId = "", name = "", profile_pic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RootLayout = findViewById(R.id.RootLayout);
        HomeButton = findViewById(R.id.HomeButton);
        TransactionsButton = findViewById(R.id.TransactionsButton);
        PendingButton = findViewById(R.id.PendingButton);
        HomeText = findViewById(R.id.HomeText);
        TransactionsText = findViewById(R.id.TransactionsText);
        PendingText = findViewById(R.id.PendingText);
        HomeIcon = findViewById(R.id.HomeIcon);
        TransactionsIcon = findViewById(R.id.TransactionsIcon);
        PendingIcon = findViewById(R.id.PendingIcon);
        BottomNavigationBar = findViewById(R.id.BottomNavigation);


        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                for(Fragment fragment : fragmentManager.getFragments())
                    fragmentManager.beginTransaction().hide(fragment).commit();

                if(fragmentManager.getBackStackEntryCount() != 0) {
                    int total = 0;
                    for(Integer c : fragment_breaks)
                        total+= c;
                    fragment_breaks.add(fragmentManager.getBackStackEntryCount() - total);
                }

                Fragment homeFragment = getSupportFragmentManager().findFragmentByTag("HomeFragment");

                selectedIcon(homeFragment);
                backStackMap.put("HomeFragment", fragmentCounter++);
                getSupportFragmentManager().beginTransaction().show(homeFragment).commit();
                activeFragment = homeFragment;

            }
        });


        TransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, new TimetableFragment(), "Timetable").commit();

                FragmentManager fragmentManager = getSupportFragmentManager();
                for (Fragment fragment : fragmentManager.getFragments())
                    fragmentManager.beginTransaction().hide(fragment).commit();

                if (fragmentManager.getBackStackEntryCount() != 0) {
                    int total = 0;
                    for (Integer c : fragment_breaks)
                        total += c;
                    fragment_breaks.add(fragmentManager.getBackStackEntryCount() - total);
                }

                TransactionsFragment timetableFragment = (TransactionsFragment) getSupportFragmentManager().findFragmentByTag("TransactionsFragment");

                if (timetableFragment == null) {
                    timetableFragment = new TransactionsFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, timetableFragment, "TransactionsFragment").commit();
                } else
                    getSupportFragmentManager().beginTransaction().show(timetableFragment).commit();

                backStackMap.put("TransactionsFragment", fragmentCounter++);
                selectedIcon(timetableFragment);
                activeFragment = timetableFragment;
            }
        });


        PendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, new PollsFragment(), "Timetable").commit();

                FragmentManager fragmentManager = getSupportFragmentManager();
                for (Fragment fragment : fragmentManager.getFragments())
                    fragmentManager.beginTransaction().hide(fragment).commit();

                if (fragmentManager.getBackStackEntryCount() != 0) {
                    int total = 0;
                    for (Integer c : fragment_breaks)
                        total += c;
                    fragment_breaks.add(fragmentManager.getBackStackEntryCount() - total);
                }

                PendingPaymentsFragment pollsFragment = (PendingPaymentsFragment) getSupportFragmentManager().findFragmentByTag("PendingFragment");

                if (pollsFragment == null) {
                    pollsFragment = new PendingPaymentsFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, pollsFragment, "PendingFragment").commit();
                } else
                    getSupportFragmentManager().beginTransaction().show(pollsFragment).commit();

                backStackMap.put("PendingFragment", fragmentCounter++);
                selectedIcon(pollsFragment);
                activeFragment = pollsFragment;
            }
        });


        getPermissions();
        loadUserData();
        loadCategories();
        loadBanks();
        getUserData();
        getServerData();

        backStackMap.put("HomeFragment", fragmentCounter++);
        homeFragment = new HomeFragment();
        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.Frame_Container, homeFragment, "HomeFragment").commit();
    }

    private void getPermissions()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS},
                    1);
        }
    }

    void loadCategories()
    {
        SQLiteDatabase database = new SqlHelper(MainActivity.this).getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM CATEGORIES", new String[]{});
        cursor.moveToFirst();

        if(cursor.getCount() != 0)
        {
            do {
                categoriesMap.put(cursor.getString(0), new Category(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), false));
                Log.i(("Test"), cursor.getString(0) + "");
            }while (cursor.moveToNext());
        }

        //Clear Database
        SQLiteDatabase database2 = new SqlHelper(MainActivity.this).getWritableDatabase();
        database2.delete("CATEGORIES", null, null);
        database2.close();
    }

    void loadBanks()
    {
        SQLiteDatabase database = new SqlHelper(MainActivity.this).getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM BANKS", new String[]{});
        cursor.moveToFirst();

        if(cursor.getCount() != 0)
        {
            do {
                banksMap.put(cursor.getString(0), cursor.getString(1));
                Log.i(("Test"), cursor.getString(0) + "");
            }while (cursor.moveToNext());
        }
    }


    void loadUserData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        userId = sharedPreferences.getString("UserId", "");
        name = sharedPreferences.getString("Name", "");
        profile_pic = sharedPreferences.getString("ProfilePic", "");
    }

    void getUserData()
    {
        firestore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                cacheUserData(MainActivity.this, documentSnapshot);
            }
        });
    }

    static void cacheUserData(Context context, DocumentSnapshot documentSnapshot)
    {
        String userId = documentSnapshot.getString("UserId");
        String name = documentSnapshot.getString("Name");
        String profile_pic = documentSnapshot.getString("ProfilePic");
        Long lastSMSTime = documentSnapshot.getLong("LastSMSTime");
        Double budget = documentSnapshot.getDouble("MonthlyBudget");
        HashMap<String, Object> categories = (HashMap<String, Object>) documentSnapshot.get("Categories");

        if (budget == null)
            budget = 0.0;

        if (lastSMSTime == null)
            lastSMSTime = -1L;

        SharedPreferences sharedPreferences = ((AppCompatActivity) context).getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("UserId", userId);
        myEdit.putString("Name", name);
        myEdit.putString("ProfilePic", profile_pic);
        myEdit.putLong("LastSMSTime", lastSMSTime);
        myEdit.putFloat("MonthlyBudget", budget.floatValue());
        myEdit.apply();


        if(categories != null) {
            for (String key : categories.keySet()) {
                HashMap<String, String> category = (HashMap<String, String>) categories.get(key);
                SQLiteDatabase database = new SqlHelper(context).getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("CategoryId", key);
                contentValues.put("Category", category.get("Name"));
                contentValues.put("IconUrl", category.get("Icon"));
                contentValues.put("Type", "Custom");
                database.insertWithOnConflict("CATEGORIES", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                database.close();
            }
        }
    }

    void getServerData()
    {
        SQLiteDatabase database1 = new SqlHelper(MainActivity.this).getWritableDatabase();
        database1.delete("BANKS", null, null);
        database1.close();

        firestore.collection("ServerData").document("ServerData").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                HashMap<String, Object> banks = (HashMap<String, Object>) documentSnapshot.get("Banks");
                HashMap<String, Object> categories = (HashMap<String, Object>) documentSnapshot.get("Categories");

                if(banks != null) {
                    for (String key : banks.keySet()) {
                        HashMap<String, String> category = (HashMap<String, String>) banks.get(key);
                        SQLiteDatabase database = new SqlHelper(getApplicationContext()).getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("Name", category.get("Name"));
                        contentValues.put("IconUrl", category.get("Icon"));
                        database.insertWithOnConflict("BANKS", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                        database.close();
                    }
                }

                if(categories != null) {
                    for (String key : categories.keySet()) {
                        HashMap<String, String> category = (HashMap<String, String>) categories.get(key);
                        SQLiteDatabase database = new SqlHelper(getApplicationContext()).getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("CategoryId", key);
                        contentValues.put("Category", category.get("Name"));
                        contentValues.put("IconUrl", category.get("Icon"));
                        contentValues.put("Type", "Default");
                        database.insertWithOnConflict("CATEGORIES", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                        database.close();
                    }
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            SMSHelper smsHelper = new SMSHelper(MainActivity.this);
            smsHelper.readSMS();
        }
    }

    @Override
    public void onBackPressed() {

        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fragmentManager = getSupportFragmentManager();

        //For Nested Fragments
        //Checking if Account was opened from Comments

        for (Fragment frag : fragmentManager.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }

        int total_breaks = 0;
        for(Integer count : fragment_breaks)
            total_breaks+=count;

        if(getSupportFragmentManager().getBackStackEntryCount() == 0 || total_breaks == getSupportFragmentManager().getBackStackEntryCount())
        {

            for(Fragment fragment : fragmentManager.getFragments()) {
                if(!(fragment instanceof HomeFragment) && !(fragment instanceof TransactionsFragment) && !(fragment instanceof PendingPaymentsFragment))
                    fragmentManager.beginTransaction().show(fragment).commit();
            }

            if(fragment_breaks.size() > 0)
                fragment_breaks.remove(fragment_breaks.size() - 1);

            backStackMap.remove(activeFragment.getTag());
            int max = -1;
            String max_tag = "";

            for(String tag : backStackMap.keySet())
            {
                if(backStackMap.get(tag) >= max)
                {
                    max = backStackMap.get(tag);
                    max_tag = tag;
                }
            }

            if(max != -1) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(max_tag);
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(fragment).commit();
                selectedIcon(fragment);
                activeFragment = fragment;
                return;
            }
        }
        super.onBackPressed();
    }

    private void selectedIcon(Fragment newFragment)
    {
        if(activeFragment instanceof HomeFragment) {
            HomeIcon.setImageResource(R.drawable.icon_statistics);
            HomeText.setTextColor(getResources().getColor(R.color.grey));
        }
        else if(activeFragment instanceof TransactionsFragment) {
            TransactionsIcon.setImageResource(R.drawable.icon_transactions);
            TransactionsText.setTextColor(getResources().getColor(R.color.grey));
        }
        else if(activeFragment instanceof PendingPaymentsFragment) {
            PendingIcon.setImageResource(R.drawable.icon_pending);
            PendingText.setTextColor(getResources().getColor(R.color.grey));
        }

        //New
        if(newFragment instanceof HomeFragment) {
            HomeIcon.setImageResource(R.drawable.icon_statistics_selected);
            HomeText.setTextColor(getResources().getColor(R.color.blue3));
        }
        else if(newFragment instanceof TransactionsFragment) {
            TransactionsIcon.setImageResource(R.drawable.icon_transactions_selcted);
            TransactionsText.setTextColor(getResources().getColor(R.color.blue3));
        }
        else if(newFragment instanceof PendingPaymentsFragment) {
            PendingIcon.setImageResource(R.drawable.icon_pending_selected);
            PendingText.setTextColor(getResources().getColor(R.color.blue3));
        }
    }
}
