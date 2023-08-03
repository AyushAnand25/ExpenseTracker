package com.expensetracker.expensetracker;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;


public class SMSBottomSheet extends BottomSheetDialogFragment implements RecyclerAdapter_SMS.SMSClick, RecyclerAdapter_Categories.CategoryClick {

    private RecyclerView recyclerView, CategoriesRecycler;

    private ArrayList<SMS> smsArrayList;
    private ArrayList<Category> categoryArrayList = new ArrayList<>();
    private RecyclerAdapter_SMS recyclerAdapter_sms;
    private RecyclerAdapter_Categories recyclerAdapter_categories;
    EditText Details;
    private Button Done;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sms_bottom_sheet, container, false);
        recyclerView = view.findViewById(R.id.RecyclerView);
        CategoriesRecycler = view.findViewById(R.id.CategoriesRecycler);
        Details = view.findViewById(R.id.Details);
        Done = view.findViewById(R.id.Done);


        recyclerAdapter_sms = new RecyclerAdapter_SMS(getContext(), smsArrayList, SMSBottomSheet.this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerAdapter_sms);


        for(String key : MainActivity.categoriesMap.keySet())
            categoryArrayList.add(new Category(key, MainActivity.categoriesMap.get(key).category, MainActivity.categoriesMap.get(key).iconUrl, MainActivity.categoriesMap.get(key).type, false));

        recyclerAdapter_categories = new RecyclerAdapter_Categories(getContext(), categoryArrayList, SMSBottomSheet.this);
        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        CategoriesRecycler.setLayoutManager(linearLayoutManager2);
        CategoriesRecycler.setAdapter(recyclerAdapter_categories);


        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransactionData transactionData = new TransactionData(getContext());
                transactionData.addSMSTransactions(smsArrayList, null);

                //Refresh
                ((HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag("HomeFragment")).refresh();
                SMSBottomSheet.this.dismiss();
            }
        });

        Details.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                smsArrayList.get(recyclerAdapter_sms.current_sms_index).details = Details.getText().toString().trim();
            }
        });

        return view;
    }

    @Override
    public void onSMSClick(int index) {

        int prev = recyclerAdapter_sms.current_sms_index;
        recyclerAdapter_sms.current_sms_index = index;

        for (int i = 0; i < categoryArrayList.size(); i++) {
            categoryArrayList.get(i).isSelected = categoryArrayList.get(i).category.equals(smsArrayList.get(recyclerAdapter_sms.current_sms_index).category);
        }

        Details.setText(smsArrayList.get(index).details);
        recyclerAdapter_sms.notifyItemChanged(prev);
        recyclerAdapter_sms.notifyItemChanged(index);
        recyclerAdapter_categories.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(int index) {

        Log.i("Test", "Category Clicked");
        if (smsArrayList.get(recyclerAdapter_sms.current_sms_index).category.equals(categoryArrayList.get(index).category))
            smsArrayList.get(recyclerAdapter_sms.current_sms_index).category = "General";
        else
            smsArrayList.get(recyclerAdapter_sms.current_sms_index).category = categoryArrayList.get(index).category;

        for (int i = 0; i < categoryArrayList.size(); i++) {
            categoryArrayList.get(i).isSelected = categoryArrayList.get(i).category.equals(smsArrayList.get(recyclerAdapter_sms.current_sms_index).category);
        }
        recyclerAdapter_categories.notifyDataSetChanged();
    }

    SMSBottomSheet(ArrayList<SMS> smsArrayList)
    {
        this.smsArrayList = smsArrayList;
    }
}
