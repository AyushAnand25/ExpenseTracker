package com.expensetracker.expensetracker;


import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter_PendingPayments extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Transaction> arrayList;
    private PendingListener pendingListener;
    private HashSet<Integer> selectedPayments;
    private double credit, debit;
    boolean selectionEnabled = false;
    private int current_index = -1;
    private PaymentHolder current_holder = null;

    private int HEADER = 0;
    private int PAYMENT = 1;


    RecyclerAdapter_PendingPayments(Context context, ArrayList<Transaction> arrayList, HashSet<Integer> selectedPayments, double credit, double debit, PendingListener pendingListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.selectedPayments = selectedPayments;
        this.credit = credit;
        this.debit = debit;
        this.pendingListener = pendingListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view;
        if(viewType == HEADER) {
            view = inflater.inflate(R.layout.pending_header_item, parent, false);
            return new HeaderHolder(view);
        }
        else {
            view = inflater.inflate(R.layout.pending_payment_item, parent, false);
            return new PaymentHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {

        if(viewHolder.getItemViewType() == PAYMENT) {
            Transaction transaction = arrayList.get(position - 1);
            PaymentHolder holder = (PaymentHolder) viewHolder;
            holder.amount.setText("₹ " + transaction.amount);
            holder.details.setText(transaction.details);
            holder.category.setText(transaction.category);

            for(String key : MainActivity.categoriesMap.keySet()) {
                if(MainActivity.categoriesMap.get(key).category.equals(arrayList.get(position - 1).category))
                    Glide.with(context).load(MainActivity.categoriesMap.get(key).iconUrl).centerCrop().into(holder.icon);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
            holder.date.setText(simpleDateFormat.format(transaction.time));

            if (current_index == position - 1)
                current_holder = holder;

            if (transaction.type == Transaction.DEBIT) {
                holder.type.setText("Debit");
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.blue3));
                holder.type.setTextColor(ContextCompat.getColor(context, R.color.blue3));
                holder.edit.setTextColor(ContextCompat.getColor(context, R.color.blue3));
                holder.settle.setTextColor(ContextCompat.getColor(context, R.color.blue3));
                holder.delete.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            } else {
                holder.type.setText("Credit");
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.purple1));
                holder.type.setTextColor(ContextCompat.getColor(context, R.color.purple1));
                holder.edit.setTextColor(ContextCompat.getColor(context, R.color.purple1));
                holder.settle.setTextColor(ContextCompat.getColor(context, R.color.purple1));
                holder.delete.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            }

            //Set Background on selected
            if (selectedPayments.contains(position - 1))
                holder.background.setBackgroundResource(R.drawable.pending_selected_drawable);
            else
                holder.background.setBackgroundResource(R.drawable.round_drawable1);


            //Click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (selectionEnabled)
                        pendingListener.onClick(position - 1);
                    else {
                        if (current_index == position - 1) {
                            holder.buttons.setVisibility(View.GONE);
                            current_holder = null;
                            current_index = -1;
                        } else {
                            if (current_holder != null)
                                current_holder.buttons.setVisibility(View.GONE);

                            holder.buttons.setVisibility(View.VISIBLE);
                            current_holder = holder;
                            current_index = position - 1;
                        }
                    }
                }
            });


            //Long Press
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    pendingListener.onLongPress(position - 1);
                    return true;
                }
            });

            //Edit
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pendingListener.onEdit(position - 1);
                }
            });


            //Settle
            holder.settle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pendingListener.onSettle(position - 1);
                }
            });


            //Delete
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pendingListener.onDelete(position - 1);
                }
            });

        }
        else {
            HeaderHolder holder = (HeaderHolder) viewHolder;
            double total = credit - debit;
            holder.credit.setText(String.format("To Get  ₹ %.1f", credit));
            holder.debit.setText(String.format("To Pay  ₹ %.1f", debit));

            if (total >= 0) {
                holder.total.setText(String.format("+ ₹ %.1f", total));
                holder.total.setTextColor(ContextCompat.getColor(context, R.color.purple1));
            } else {
                holder.total.setText(String.format("- ₹ %.1f", Math.abs(total)));
                holder.total.setTextColor(ContextCompat.getColor(context, R.color.blue3));
            }

            if(arrayList.size() == 0)
                holder.pending_text.setVisibility(View.INVISIBLE);
            else
                holder.pending_text.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemViewType(int position) {

        if(position == 0)
            return HEADER;
        return PAYMENT;
    }

    @Override
    public int getItemCount() {
        return arrayList.size()+1;
    }


    public class HeaderHolder extends RecyclerView.ViewHolder {

        TextView credit, debit, total, pending_text;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);

            credit = itemView.findViewById(R.id.CreditAmount);
            debit = itemView.findViewById(R.id.DebitAmount);
            total = itemView.findViewById(R.id.PendingAmount);
            pending_text = itemView.findViewById(R.id.PendingText);
        }
    }

    public class PaymentHolder extends RecyclerView.ViewHolder {

        ConstraintLayout background, buttons;
        TextView date, type, category, amount, details;
        Button edit, delete, settle;
        ImageView icon;

        PaymentHolder(@NonNull View itemView) {
            super(itemView);

            background = itemView.findViewById(R.id.Background);
            buttons = itemView.findViewById(R.id.ButtonsLayout);
            date = itemView.findViewById(R.id.Date);
            type = itemView.findViewById(R.id.Type);
            category = itemView.findViewById(R.id.Category);
            amount = itemView.findViewById(R.id.Amount);
            details = itemView.findViewById(R.id.Details);
            icon = itemView.findViewById(R.id.Icon);
            edit = itemView.findViewById(R.id.Edit);
            settle = itemView.findViewById(R.id.Settle);
            delete = itemView.findViewById(R.id.Delete);
        }
    }

    interface PendingListener
    {
        public void onLongPress(int index);
        public void onClick(int index);
        public void onEdit(int index);
        public void onDelete(int index);
        public void onSettle(int index);
    }

}
