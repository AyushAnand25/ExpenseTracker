package com.expensetracker.expensetracker;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.expensetracker.expensetracker.MainActivity.profile_pic;


public class AccountFragment extends Fragment {

    private androidx.appcompat.widget.Toolbar Toolbar;
    private TextView ToolbarName;
    private ConstraintLayout CardBackground;
    private Button Back;
    private TextView Name, Email, Joined, Budget, Version;
    private ConstraintLayout BudgetLayout, CategoriesLayout, LogOutLayout;
    ImageView ProfilePic;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        Toolbar = view.findViewById(R.id.Toolbar);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        Back = view.findViewById(R.id.Back);
        Name = view.findViewById(R.id.Name);
        Email = view.findViewById(R.id.Email);
        Joined = view.findViewById(R.id.JoinedOn);
        ProfilePic = view.findViewById(R.id.ProfilePic);
        BudgetLayout= view.findViewById(R.id.BudgetLayout);
        CategoriesLayout = view.findViewById(R.id.CategoriesLayout);
        Budget = view.findViewById(R.id.Budget);
        LogOutLayout  = view.findViewById(R.id.LogoutLayout);
        Version = view.findViewById(R.id.Version);
        ToolbarName.setText("My Account");
        Version.setText("Version " + BuildConfig.VERSION_NAME);
        //MainActivity.BottomNavigationBar.setVisibility(View.GONE);



        ProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent , "Select Image") , 1);
            }
        });


        CategoriesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right).add(R.id.Frame_Container, new CategoriesFragment(), "CategoriesFragment").addToBackStack(null).commit();
            }
        });


        BudgetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BudgetBottomSheet budgetBottomSheet = new BudgetBottomSheet();
                budgetBottomSheet.setData(Budget);
                budgetBottomSheet.show(getActivity().getSupportFragmentManager(), "BudgetBottomSheet");
            }
        });


        //Log Out
        LogOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut(getContext());
            }
        });


        //Back
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        getUserData();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                updateProfilePic(data.getData());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }


    void getUserData()
    {
        firestore.collection("Users").document(MainActivity.userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String name = documentSnapshot.getString("Name");
                String profile_pic = documentSnapshot.getString("ProfilePic");
                String email = documentSnapshot.getString("EmailId");
                Long joinedAt  = documentSnapshot.getLong("CreatedAt");
                Double budget = documentSnapshot.getDouble("MonthlyBudget");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yy");

                Name.setText(name);
                Email.setText(email);
                Joined.setText("Joined on " + simpleDateFormat.format(new Date(joinedAt)));
                Budget.setText(String.format("₹ %.1f", budget));

                if (profile_pic != null && !profile_pic.equals(""))
                    Glide.with(getContext()).load(profile_pic).centerCrop().into(ProfilePic);
                else
                    Glide.with(getContext()).load(R.drawable.wallet_icon22).into(ProfilePic);
            }
        });
    }

    void updateProfilePic(Uri uri)
    {
        Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        TextView message = dialog.findViewById(R.id.Message);
        message.setText("Updating Profile Pic...");

        try {
            File imageFile = new File(getContext().getCacheDir(), "temp.jpg");
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(imageFile);
            IOUtils.copyStream(inputStream, fos);
            fos.flush();
            fos.close();

            Bitmap compressedImageBitmap = new Compressor(getContext()).setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP).compressToBitmap(imageFile);

            //Medium
            ByteArrayOutputStream baosMedium = new ByteArrayOutputStream();
            compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baosMedium);
            byte[] dataMedium = baosMedium.toByteArray();

            FirebaseStorage.getInstance().getReference().child("ProfilePics").child(MainActivity.userId).putBytes(dataMedium).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            firestore.collection("Users").document(MainActivity.userId).update("ProfilePic", task.getResult().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    dialog.dismiss();
                                    Glide.with(getContext()).load(task.getResult().toString()).centerCrop().into(ProfilePic);

                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    myEdit.putString("ProfilePic", task.getResult().toString());
                                    myEdit.apply();
                                    Glide.with(getContext()).load(task.getResult()).centerCrop().into(((HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag("HomeFragment")).AccountButton);
                                }
                            });
                        }
                    });
                }
            });


        } catch (IOException e) {
            dialog.dismiss();
        }
    }

    void logOut(Context context)
    {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        TextView Message;
        Button Ok, Cancel;
        Message = dialog.findViewById(R.id.Message);
        Message.setText("Log Out?");
        Ok = dialog.findViewById(R.id.Ok);
        Cancel = dialog.findViewById(R.id.Cancel);

        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
                auth.signOut();

                SharedPreferences sharedPreferences2 = context.getSharedPreferences("UserData", MODE_PRIVATE);
                SharedPreferences.Editor myEdit2 = sharedPreferences2.edit();
                myEdit2.clear();
                myEdit2.apply();

                Intent intent = new Intent(context, LogInActivity.class);
                context.startActivity(intent);
                ((AppCompatActivity) context).finish();

            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.BottomNavigationBar.setVisibility(View.VISIBLE);
    }
}
