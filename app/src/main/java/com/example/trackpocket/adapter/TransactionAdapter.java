package com.example.trackpocket.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackpocket.Model.Transaction;
import com.example.trackpocket.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_transactions_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.amountTextView.setText(String.format("%.2f", transaction.getAmount())); // Formatting the amount
        holder.dateTextView.setText(transaction.getDate());

        // Change the background color based on the transaction type
        if ("Income".equals(transaction.getType())) {
            holder.iconBackground.setBackgroundTintList(
                    ColorStateList.valueOf(holder.iconBackground.getContext().getResources().getColor(R.color.green))
            );
            holder.transactionTypeIcon.setImageResource(R.drawable.arrow_down);
        } else {
            holder.iconBackground.setBackgroundTintList(
                    ColorStateList.valueOf(holder.iconBackground.getContext().getResources().getColor(R.color.red))
            );
            holder.transactionTypeIcon.setImageResource(R.drawable.arrow_up);
        }

    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView amountTextView;
        public TextView categoryTextView;
        public TextView dateTextView;
        public View iconBackground;
        public ImageView transactionTypeIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.transaction_amount);
            categoryTextView = itemView.findViewById(R.id.transaction_category);
            dateTextView = itemView.findViewById(R.id.transaction_date);
            iconBackground = itemView.findViewById(R.id.transaction_icon_bg);
            transactionTypeIcon = itemView.findViewById(R.id.transaction_icon);
        }
    }
}
