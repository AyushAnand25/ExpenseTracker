package com.expensetracker.expensetracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;


public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView ToolbarName;
    private Button Add, Back;

    private ArrayList<Category> arrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = view.findViewById(R.id.RecyclerView);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        Add = view.findViewById(R.id.Add);
        Back = view.findViewById(R.id.Back);
        ToolbarName.setText("Expense Categories");


        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CategoryBottomSheet categoryBottomSheet = new CategoryBottomSheet();
                categoryBottomSheet.show(getActivity().getSupportFragmentManager(), "CategoryBottomSheet");

            }
        });


        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


        refresh();
        return view;
    }

    void refresh()
    {
        arrayList.clear();
        arrayList.addAll(MainActivity.categoriesMap.values());
        RecyclerAdapter_Categories_Edit recyclerAdapter_categories_edit = new RecyclerAdapter_Categories_Edit(getContext(), arrayList);
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getContext());
        recyclerView.setLayoutManager(flexboxLayoutManager);
        recyclerView.setAdapter(recyclerAdapter_categories_edit);
    }
}
