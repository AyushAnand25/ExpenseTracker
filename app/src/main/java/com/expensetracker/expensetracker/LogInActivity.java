package com.expensetracker.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LogInActivity extends AppCompatActivity {


    private EditText Email, Password;
    private Button LogIn;
    private ProgressBar Loading;
    private TextView SignUp, ForgotPassword;
    ConstraintLayout RootLayout;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        RootLayout = findViewById(R.id.RootLayout);
        LogIn = findViewById(R.id.LogIn);
        Loading = findViewById(R.id.Loading);
        SignUp = findViewById(R.id.SignUp);
        ForgotPassword = findViewById(R.id.ForgotPassword);

        //Log In
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Email.getText().toString().trim().equals(""))
                    showSnackbar("Please Enter your Email Id.");
                else if(Password.getText().toString().trim().length() <6)
                    showSnackbar("Please Enter your Password.");
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                    LogIn.setText("");
                    String email = Email.getText().toString().trim();
                    String password = Password.getText().toString().trim();

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                if(auth.getCurrentUser().isEmailVerified()) {
                                    firestore.collection("Users").whereEqualTo("EmailId", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                MainActivity.cacheUserData(LogInActivity.this, documentSnapshot);
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                break;
                                            }

                                            if (queryDocumentSnapshots.size() == 0) {
                                                showSnackbar("User does not exist.");
                                                Loading.setVisibility(View.GONE);
                                                LogIn.setText("Log In");
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    Intent intent = new Intent(getApplicationContext(), VerifyEmailActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException)
                                    showSnackbar("User does Not exist. Please Sign Up!");
                                else
                                    showSnackbar(task.getException().getMessage());
                                Loading.setVisibility(View.GONE);
                                LogIn.setText("Log In");
                            }
                        }
                    });
                }

            }
        });


        //Forgot Password
        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(LogInActivity.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.forgot_password_layout);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                Button Ok, Cancel;
                Ok = dialog.findViewById(R.id.Ok);
                Cancel = dialog.findViewById(R.id.Cancel);
                EditText Email = dialog.findViewById(R.id.Email);


                Ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(Email.getText().toString().trim().equals(""))
                            showSnackbar("Please Enter your Email to get Password reset link.");
                        else
                        {
                            auth.sendPasswordResetEmail(Email.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    showSnackbar("A Password reset link has been sent to you Email.");
                                }
                            });
                        }

                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(RootLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundResource(R.color.white);
        TextView tv = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.BLACK);
        snackbar.show();
    }
}
