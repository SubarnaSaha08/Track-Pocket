package com.example.trackpocket;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackpocket.Model.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private String currencyType;

    public TransactionAdapter(List<Transaction> transactions, String currencyType) {
        this.transactions = transactions;
        this.currencyType = currencyType;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and return a new view holder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_transactions_layout, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.setDescription(transaction.getDescription());
        holder.setAmount(transaction.getAmount());
        holder.setDate(transaction.getDate());
        holder.setCurrencyType(currencyType);
        holder.setAccountTitle(transaction.getAccountTitle());
        holder.setType(transaction.getType(), holder);
        holder.setCategory(transaction.getCategory(), holder);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        View transactionView;
        protected View iconBackground;
        protected ImageView transactionTypeIcon;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            transactionView = itemView;
        }

        private void setCurrencyType(String cType) {
            TextView cTypeView = transactionView.findViewById(R.id.currency_type);
            cTypeView.setText(cType);
        }

        private void setDescription(String description) {
            TextView descriptionView = transactionView.findViewById(R.id.transaction_description);
            descriptionView.setText(description);
        }

        private void setAmount(Double amount) {
            TextView amountView = transactionView.findViewById(R.id.transaction_amount);
            amountView.setText(String.format("%.2f", amount));
        }

        private void setDate(String date) {
            TextView dateView = transactionView.findViewById(R.id.transaction_date);
            dateView.setText(date);
        }

        private void setAccountTitle(String accountTitle) {
            TextView accountTitleView = transactionView.findViewById(R.id.transaction_account);
            accountTitleView.setText(accountTitle);
        }

        private void setType(String type, TransactionViewHolder holder) {
            iconBackground = transactionView.findViewById(R.id.transaction_icon_bg);
            transactionTypeIcon = itemView.findViewById(R.id.transaction_icon);
            if ("income".equalsIgnoreCase(type)) {
                holder.iconBackground.setBackgroundTintList(
                        ColorStateList.valueOf(holder.iconBackground.getContext().getResources().getColor(R.color.green))
                );
            } else {
                holder.iconBackground.setBackgroundTintList(
                        ColorStateList.valueOf(holder.iconBackground.getContext().getResources().getColor(R.color.red))
                );
            }
        }
        private void setCategory(String category, TransactionViewHolder holder) {
            transactionTypeIcon = itemView.findViewById(R.id.transaction_icon);
            if ("Income".equalsIgnoreCase(category)) {
                holder.transactionTypeIcon.setImageResource(R.drawable.arrow_down);
            } else if("Gift".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_gift);
            } else if("Food".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_food);
            } else if("Study".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_education);
            } else if("Transport".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_transport);
            } else if("Utility".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_utility);
            } else if("Entertainment".equalsIgnoreCase(category)){
                holder.transactionTypeIcon.setImageResource(R.drawable.category_icon_entertainment);
            }
        }
    }
}
