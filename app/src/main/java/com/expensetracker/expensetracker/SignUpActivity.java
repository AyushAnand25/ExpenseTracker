package com.expensetracker.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity {


    private EditText Name, Email, Password, ConfirmPassword;
    private Button SignUp;
    private TextView LogIn;
    private ProgressBar Loading;
    ConstraintLayout RootLayout;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Name = findViewById(R.id.Name);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        ConfirmPassword = findViewById(R.id.ConfirmPassword);
        Loading = findViewById(R.id.Loading);
        RootLayout = findViewById(R.id.RootLayout);
        SignUp = findViewById(R.id.SignUp);
        LogIn = findViewById(R.id.LogIn);


        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Name.getText().toString().trim().equals(""))
                    showSnackbar("Please Enter your Name.");
                else
                {
                    String name = Name.getText().toString().trim();
                    for(int i=0;i<name.length();i++)
                    {
                        if(!((name.charAt(i) >= 65 && name.charAt(i) <= 90) || (name.charAt(i) >= 97 && name.charAt(i) <= 122) || name.charAt(i) == ' '))
                        {
                            showSnackbar("Name can only contain alphabets.");
                            return;
                        }
                    }
                }

                if (Email.getText().toString().trim().equals(""))
                    showSnackbar("Please Enter your Email Id.");
                else if (Password.getText().toString().trim().length() < 6)
                    showSnackbar("Password must contain at least 6 characters.");
                else if (ConfirmPassword.getText().toString().trim().equals(""))
                    showSnackbar("Please confirm your Password.");
                else if (!ConfirmPassword.getText().toString().trim().equals(Password.getText().toString().trim()))
                    showSnackbar("Passwords do not match.");
                else {
                    Loading.setVisibility(View.VISIBLE);
                    SignUp.setText("");
                    String email = Email.getText().toString().trim();
                    String password = Password.getText().toString().trim();

                    auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            //Send Email Verification
                            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                        myEdit.putString("Name", Name.getText().toString().trim());
                                        myEdit.apply();

                                        Intent intent = new Intent(getApplicationContext(), VerifyEmailActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                            showSnackbar("User already exists, Please Log In.");
                                        else if (task.getException() instanceof FirebaseAuthWeakPasswordException)
                                            showSnackbar("Password must contain at least 6 characters.");
                                        else
                                            showSnackbar(task.getException().getMessage());

                                        Loading.setVisibility(View.GONE);
                                        SignUp.setText("Sign Up");
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            showSnackbar(e.getMessage());
                            Loading.setVisibility(View.GONE);
                            SignUp.setText("Sign Up");
                        }
                    });
                }

            }
        });


        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
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
