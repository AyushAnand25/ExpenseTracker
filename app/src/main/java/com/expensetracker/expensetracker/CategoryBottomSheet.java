package com.expensetracker.expensetracker;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class CategoryBottomSheet extends BottomSheetDialogFragment {

    EditText Category, IconUrl;
    Button Submit;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    Category category;
    Uri iconURI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_bottom_sheet, container, false);
        Category = view.findViewById(R.id.Category);
        IconUrl= view.findViewById(R.id.Icon);
        Submit = view.findViewById(R.id.Submit);

        if(category != null)
        {
            Category.append(category.category);
            IconUrl.setText(category.iconUrl);
        }
        else
            category = new Category(UtilityFunctions.getRandomKey(), "", "", "Custom", false);


//        Icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent , "Select Image") , 1);
//            }
//        });


        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String category_name = Category.getText().toString().trim();

                if(category_name.equals(""))
                    Toast.makeText(getContext(), "Please Enter the Category Name...", Toast.LENGTH_SHORT).show();
                else if(iconURI == null)
                    setCategory();
                else
                    setIcon();

            }
        });


        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                iconURI = data.getData();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }


    void setIcon()
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
            InputStream inputStream = getContext().getContentResolver().openInputStream(iconURI);
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

                                    category.iconUrl = task.getResult().toString();
                                    setCategory();
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


    void setCategory()
    {
        category.category = Category.getText().toString().trim();
        category.iconUrl = IconUrl.getText().toString().trim();
        HashMap<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("CategoryId", category.category_id);
        categoryMap.put("Name", category.category);
        categoryMap.put("Icon", category.iconUrl);

        firestore.collection("Users").document(MainActivity.userId).update("Categories." +  category.category_id, categoryMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                SQLiteDatabase database = new SqlHelper(getContext()).getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("CategoryId", category.category_id);
                contentValues.put("Category", category.category);
                contentValues.put("IconUrl", category.iconUrl);
                contentValues.put("Type", "Custom");
                database.insertWithOnConflict("CATEGORIES", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                database.close();

                MainActivity.categoriesMap.put(category.category_id, category);

                ((CategoriesFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CategoriesFragment")).refresh();
                CategoryBottomSheet.this.dismiss();
            }
        });
    }
}
