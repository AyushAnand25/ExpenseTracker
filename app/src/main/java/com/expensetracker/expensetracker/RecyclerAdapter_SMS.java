package com.expensetracker.expensetracker;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter_SMS extends RecyclerView.Adapter<RecyclerAdapter_SMS.SMSHolder> {

    private Context context;
    private ArrayList<SMS> arrayList;
    private SMSClick smsClick;
    int current_sms_index = 0;

    RecyclerAdapter_SMS(Context context, ArrayList<SMS> arrayList, SMSClick smsClick)
    {
        this.context=context;
        this.arrayList = arrayList;
        this.smsClick = smsClick;
    }


    @NonNull
    @Override
    public SMSHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.sms_item,parent,false);
        return new SMSHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SMSHolder holder, final int position) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
        holder.amount.setText(String.format("₹ %.1f", arrayList.get(position).amount));
        holder.payment_method.setText(arrayList.get(position).payment_method);
        holder.date.setText(simpleDateFormat.format(new Date(arrayList.get(position).date)));
        Glide.with(context).load(MainActivity.banksMap.get(arrayList.get(position).bank)).centerCrop().into(holder.icon);


        if (arrayList.get(position).type == Transaction.DEBIT) {
            holder.details.setText("Debit");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            holder.payment_method.setTextColor(ContextCompat.getColor(context, R.color.blue3));
        } else {
            holder.details.setText("Credit");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            holder.payment_method.setTextColor(ContextCompat.getColor(context, R.color.purple1));
        }

        //Set Background on selected
        if (current_sms_index == position)
            holder.background.setBackgroundResource(R.drawable.pending_selected_drawable);
        else
            holder.background.setBackgroundResource(R.drawable.round_drawable1);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsClick.onSMSClick(position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }



    public class SMSHolder extends RecyclerView.ViewHolder {

        ConstraintLayout background;
        TextView date, details, amount, payment_method;
        CircleImageView icon;

        public SMSHolder(@NonNull View itemView) {
            super(itemView);

            background = itemView.findViewById(R.id.Background);
            date = itemView.findViewById(R.id.Date);
            details = itemView.findViewById(R.id.Details);
            amount = itemView.findViewById(R.id.Amount);
            payment_method = itemView.findViewById(R.id.PaymentMethod);
            icon = itemView.findViewById(R.id.Icon);
        }
    }

    interface SMSClick
    {
        public void onSMSClick(int index);
    }
}
