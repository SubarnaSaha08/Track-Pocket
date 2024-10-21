package com.example.trackpocket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.Model.Account;
import com.example.trackpocket.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private List<Account> accountList;

    public AccountAdapter(List<Account> accountList) {
        this.accountList = accountList;
    }

    // Create a new ViewHolder for each item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_layout, parent, false); // Change item_layout to your item layout file
        return new ViewHolder(view);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.titleTextView.setText(account.getTitle());
        holder.balanceTextView.setText(String.valueOf(account.getBalance()));

        // Get the LayoutParams for the item view
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

        int marginInPixels = (int) (5 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        if (position % 2 == 0) {
            // Set marginStart to 20dp (convert dp to pixels)
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        } else {
            layoutParams.setMargins(marginInPixels, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        }

        // Apply the modified LayoutParams to the item view
        holder.itemView.setLayoutParams(layoutParams);
    }



    @Override
    public int getItemCount() {
        return accountList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView balanceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.account_title);
            balanceTextView = itemView.findViewById(R.id.total_account_balance);
        }
    }
}
