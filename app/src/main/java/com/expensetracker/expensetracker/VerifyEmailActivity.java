package com.expensetracker.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;

public class VerifyEmailActivity extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    boolean check = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(user != null)
        {
            user.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {

                    if (user.isEmailVerified() && check) {

                        HashMap<String, Object> document = new HashMap<>();
                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                        String name = sharedPreferences.getString("Name", "User");
                        String userId = name.toLowerCase().replaceAll(" ", "_");
                        userId = userId + "_" + user.getUid().substring(0, Math.min(10, user.getUid().length()));


                        document.put("EmailId", user.getEmail());
                        document.put("UserId", userId);
                        document.put("Name", name);
                        document.put("CreatedAt", new Date().getTime());
                        document.put("MonthlyBudget", 0.0);
                        document.put("AccountBalance", 0.0);

                        String finalUserId = userId;
                        FirebaseFirestore.getInstance().collection("Users").document(userId).set(document, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putString("UserId", finalUserId);
                                myEdit.putString("Name", name);
                                myEdit.apply();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        check = false;
                    }
                }
            });
        }
    }
}
