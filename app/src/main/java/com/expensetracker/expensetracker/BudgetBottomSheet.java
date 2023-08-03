package com.expensetracker.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;


public class BudgetBottomSheet extends BottomSheetDialogFragment {

    EditText Budget;
    Button Submit;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    TextView BudgetText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_budget_bottom_sheet, container, false);
        Budget = view.findViewById(R.id.Budget);
        Submit = view.findViewById(R.id.Submit);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String budget_string = Budget.getText().toString().trim();

                if(budget_string.equals(""))
                    Toast.makeText(getContext(), "Please Enter your Budget...", Toast.LENGTH_SHORT).show();
                else
                {
                    Double budget = Double.parseDouble(budget_string);
                    firestore.collection("Users").document(MainActivity.userId).update("MonthlyBudget", budget).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.putFloat("MonthlyBudget", budget.floatValue());
                            myEdit.apply();

                            ((HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag("HomeFragment")).refresh();
                            UtilityFunctions.showSnackbar("Budget updated successfully.");
                            BudgetText.setText(String.format("₹ %.1f", budget));
                            BudgetBottomSheet.this.dismiss();
                        }
                    });
                }

            }
        });


        return view;
    }

    void setData(TextView BudgetText)
    {
        this.BudgetText = BudgetText;
    }
}
