package com.expensetracker.expensetracker;


import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter_Transactions extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Transaction> arrayList;
    private int current_index = -1;
    private TransactionHolder current_holder;
    static int ALL = 1, TODAY = 2, BOTTOMSHEET = 3;
    int type;

    private int TRANSACTION = 1;


    RecyclerAdapter_Transactions(Context context, ArrayList<Transaction> arrayList, int type) {
        this.context = context;
        this.arrayList = arrayList;
        this.type = type;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view;
        if(viewType==TRANSACTION) {
            view = inflater.inflate(R.layout.transaction_item, parent, false);
            return new  TransactionHolder(view);
        }
        else {
            view = inflater.inflate(R.layout.transaction_item, parent, false);
            return new TransactionHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        TransactionHolder holder = (TransactionHolder) viewHolder;

        for(String key : MainActivity.categoriesMap.keySet()) {

            if(MainActivity.categoriesMap.get(key).category.equals(arrayList.get(position).category))
            Glide.with(context).load(MainActivity.categoriesMap.get(key).iconUrl).centerCrop().into(holder.icon);
        }

        if(arrayList.get(position).details.equals(""))
            holder.category.setText(arrayList.get(position).category);
        else
            holder.category.setText(arrayList.get(position).category + " | " + arrayList.get(position).details);

        if (arrayList.get(position).type == Transaction.DEBIT)
            holder.amount.setText("- ₹ " + arrayList.get(position).amount);
        else
            holder.amount.setText("+ ₹ " + arrayList.get(position).amount);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
        holder.date.setText(simpleDateFormat.format(arrayList.get(position).time));

        if (type == TODAY)
            holder.background.setLayoutTransition(null);
        else if(type == BOTTOMSHEET)
            holder.itemView.setEnabled(false);

        if (arrayList.get(position).type == Transaction.DEBIT) {
            holder.type.setText("Debit");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            holder.ignoredIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue3)));
            holder.edit.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            holder.ignore.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            holder.delete.setTextColor(ContextCompat.getColor(context, R.color.blue3));
        } else {
            holder.type.setText("Credit");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            holder.ignoredIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple1)));
            holder.edit.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            holder.ignore.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            holder.delete.setTextColor(ContextCompat.getColor(context, R.color.purple1));
        }

        if (arrayList.get(position).ignore) {
            holder.ignore.setText("Add");
            holder.ignoredIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ignore.setText("Ignore");
            holder.ignoredIcon.setVisibility(View.GONE);
        }
        if (current_index == position) {
            current_holder = holder;
            holder.buttons.setVisibility(View.VISIBLE);
        } else
            holder.buttons.setVisibility(View.GONE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTransactionClick(position, holder);
            }
        });


        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEdit(position);
            }
        });


        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onIgnore(position);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete(position);
            }
        });
    }


    @Override
    public int getItemViewType(int position) {

            return TRANSACTION;
    }

    @Override
    public int getItemCount() {

            return arrayList.size();
    }


    public class TransactionHolder extends RecyclerView.ViewHolder {

        ConstraintLayout buttons, background;
        TextView date, type, category, amount;
        Button edit, ignore, delete;
        ImageView icon;
        ImageView ignoredIcon;

        TransactionHolder(@NonNull View itemView) {
            super(itemView);

            background = itemView.findViewById(R.id.constraintLayout);
            buttons = itemView.findViewById(R.id.ButtonsLayout);
            date = itemView.findViewById(R.id.Date);
            category = itemView.findViewById(R.id.Category);
            type = itemView.findViewById(R.id.Type);
            amount = itemView.findViewById(R.id.Amount);
            icon = itemView.findViewById(R.id.Icon);
            edit = itemView.findViewById(R.id.Edit);
            ignore = itemView.findViewById(R.id.Ignore);
            delete = itemView.findViewById(R.id.Delete);
            ignoredIcon = itemView.findViewById(R.id.Ignored);
        }
    }


    void onTransactionClick(int position, TransactionHolder holder)
    {
        if (current_index == position) {
            holder.buttons.setVisibility(View.GONE);
            current_holder = null;
            current_index = -1;
        } else {
            if (current_holder != null)
                current_holder.buttons.setVisibility(View.GONE);

            holder.buttons.setVisibility(View.VISIBLE);
            current_holder = holder;
            current_index = position;
        }
    }

    void onEdit(int position)
    {
        AddExpenseFragment addExpenseFragment = new AddExpenseFragment();
        addExpenseFragment.setData(arrayList.get(position), false, ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("TransactionsFragment"));
        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.Frame_Container, addExpenseFragment, "ADD").addToBackStack(null).commit();
    }


    void onIgnore(int position)
    {
        boolean ignored = arrayList.get(position).ignore;
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Button Ignore, Cancel;
        TextView Message = dialog.findViewById(R.id.Message);
        Ignore = dialog.findViewById(R.id.Ok);
        Cancel = dialog.findViewById(R.id.Cancel);
        Cancel.setText("Cancel");

        if(!ignored) {
            Message.setText("Ignore this Transaction?");
            Ignore.setText("Ignore");
        }
        else {
            Message.setText("Add this Transaction?");
            Ignore.setText("Add");
        }

        Ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
                FirebaseFirestore.getInstance().collection("Transactions").document(MainActivity.userId).collection("Months").document(simpleDateFormat.format(new Date(arrayList.get(position).time))).update("Transactions." + arrayList.get(position).id + ".Ignore", !ignored).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (!ignored)
                            UtilityFunctions.showSnackbar("This Transaction will be ignored.");
                        else
                            UtilityFunctions.showSnackbar("The Transaction has been added.");

                        arrayList.get(position).ignore = !ignored;
                        notifyDataSetChanged();
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

        Message.setText("Delete this Transaction?");
        Delete.setText("Delete");
        Cancel.setText("Cancel");

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SimpleDateFormat month_format = new SimpleDateFormat("MMMM yyyy");
                String month = month_format.format(new Date(arrayList.get(position).time));
                FirebaseFirestore.getInstance().collection("Transactions").document(MainActivity.userId).collection("Months").document(month).update("Transactions." + arrayList.get(position).id, FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        arrayList.remove(position);
                        RecyclerAdapter_Transactions.this.notifyItemRemoved(position);
                        RecyclerAdapter_Transactions.this.notifyItemRangeChanged(position,arrayList.size() + 1);
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
