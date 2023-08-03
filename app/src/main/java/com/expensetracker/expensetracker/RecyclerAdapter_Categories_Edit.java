package com.expensetracker.expensetracker;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter_Categories_Edit extends RecyclerView.Adapter<RecyclerAdapter_Categories_Edit.CategoryHolder> {

    private Context context;
    private ArrayList<Category> arrayList;

    RecyclerAdapter_Categories_Edit(Context context, ArrayList<Category> arrayList)
    {
        this.context=context;
        this.arrayList = arrayList;
    }


    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_item_expanded,parent,false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryHolder holder, final int position) {

        Glide.with(context).load(arrayList.get(position).iconUrl).centerCrop().into(holder.icon);
        holder.category.setText(arrayList.get(position).category);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CategoryBottomSheet categoryBottomSheet = new CategoryBottomSheet();
                if (position < arrayList.size())
                    categoryBottomSheet.category = arrayList.get(position);

//                if (arrayList.get(position).type.equals("Custom"))
                    categoryBottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "CategoryBottomSheet");
            }
        });


        //Long Press
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                onDelete(position);
                return true;
            }
        });
    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class CategoryHolder extends RecyclerView.ViewHolder {

        CircleImageView icon;
        TextView category;

        CategoryHolder(@NonNull View itemView) {
            super(itemView);

            category = itemView.findViewById(R.id.Category);
            icon = itemView.findViewById(R.id.Icon);
        }
    }

    void onDelete(int position)
    {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Button Delete, Cancel;
        TextView Message = dialog.findViewById(R.id.Message);
        Delete = dialog.findViewById(R.id.Ok);
        Cancel = dialog.findViewById(R.id.Cancel);

        Message.setText("Delete this Category?");
        Delete.setText("Delete");
        Cancel.setText("Cancel");

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseFirestore.getInstance().collection("Users").document(MainActivity.userId).update("Categories." + arrayList.get(position).category_id, FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        arrayList.remove(position);
                        MainActivity.categoriesMap.remove(arrayList.get(position).category_id);
                        RecyclerAdapter_Categories_Edit.this.notifyItemRemoved(position);
                        RecyclerAdapter_Categories_Edit.this.notifyItemRangeChanged(position,arrayList.size() + 1);
                    }
                });
                dialog.dismiss();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
