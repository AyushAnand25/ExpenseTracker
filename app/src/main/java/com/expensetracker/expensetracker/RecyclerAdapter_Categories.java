package com.expensetracker.expensetracker;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter_Categories extends RecyclerView.Adapter<RecyclerAdapter_Categories.CategoryHolder> {

    private Context context;
    private ArrayList<Category> arrayList;
    private CategoryClick categoryClick;

    RecyclerAdapter_Categories(Context context, ArrayList<Category> arrayList, CategoryClick categoryClick)
    {
        this.context=context;
        this.arrayList = arrayList;
        this.categoryClick = categoryClick;
    }


    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_item,parent,false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryHolder holder, final int position) {

        Glide.with(context).load(arrayList.get(position).iconUrl).centerCrop().into(holder.icon);
        if(arrayList.get(position).isSelected) {
            holder.icon.setBorderWidth(10);
            holder.icon.setBorderColor(context.getResources().getColor(R.color.blue2));
        }else {
            holder.icon.setBorderWidth(0);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryClick.onCategoryClick(position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }



    public class CategoryHolder extends RecyclerView.ViewHolder {

        CircleImageView icon;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.Icon);
        }
    }

    interface CategoryClick
    {
        public void onCategoryClick(int index);
    }
}
