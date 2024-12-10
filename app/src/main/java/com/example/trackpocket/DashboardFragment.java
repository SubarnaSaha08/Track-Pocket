package com.example.trackpocket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.Transaction;
import com.example.trackpocket.Model.UserInformation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private FloatingActionButton floatingActionButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mTransactionDatabase, mAccountDatabase, mUserDatabase;
    private EditText editDate;
    private RecyclerView tRecyclerView,accountRecyclerView;
    private View addAccount;
    private boolean isDataLoaded = false;
    private String currencyType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mTransactionDatabase = database.getReference().child("TransactionData").child(uid);
        mUserDatabase = database.getReference().child("UserData").child(uid);
        mAccountDatabase = database.getReference().child("AccountData").child(uid);

//      Transaction RECYCLER PART
        tRecyclerView = myView.findViewById(R.id.transactions_recycler);
        LinearLayoutManager layoutManagerTransaction = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManagerTransaction.setStackFromEnd(true);
        layoutManagerTransaction.setReverseLayout(true);
        tRecyclerView.setHasFixedSize(true);
        tRecyclerView.setLayoutManager(layoutManagerTransaction);

//      Account RECYCLER PART
        accountRecyclerView = myView.findViewById(R.id.accounts_recycler);
        LinearLayoutManager layoutManagerAccount = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerAccount.setStackFromEnd(true);
        layoutManagerAccount.setReverseLayout(true);
        accountRecyclerView.setHasFixedSize(true);
        accountRecyclerView.setLayoutManager(layoutManagerAccount);

        addAccount = myView.findViewById((R.id.add_account));
        addAccountView();
        getCurrencyTypeFromDb();
        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Account> options1 =
                new FirebaseRecyclerOptions.Builder<Account>()
                        .setQuery(mAccountDatabase, Account.class)
                        .build();

        FirebaseRecyclerAdapter<Account, AccountViewHolder> accountAdapter =
                new FirebaseRecyclerAdapter<Account, AccountViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull AccountViewHolder holder, int position, @NonNull Account model) {
                        getCurrencyTypeFromDb();
                        holder.setTitle(model.getTitle());
                        holder.setBalance(model.getBalance());
                        getCurrencyTypeFromDb();
                        holder.setCurrencyType(currencyType);
                        String accountId = getSnapshots().getSnapshot(position).getKey();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), AccountDetailActivity.class);
                                intent.putExtra("ACCOUNT_ID", accountId);
                                intent.putExtra("CURRENCY_TYPE", currencyType);
                                v.getContext().startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Inflate the item layout and create an instance of AccountViewHolder
                        getCurrencyTypeFromDb();
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.recycler_account_layout, parent, false);
                        return new AccountViewHolder(view);
                    }
                };

        FirebaseRecyclerOptions<Transaction> options2 =
                new FirebaseRecyclerOptions.Builder<Transaction>()
                        .setQuery(mTransactionDatabase, Transaction.class)
                        .build();

        FirebaseRecyclerAdapter<Transaction, TransactionViewHolder> tAdapter =
                new FirebaseRecyclerAdapter<Transaction, TransactionViewHolder>(options2) {

                    @Override
                    protected void onBindViewHolder(@NonNull TransactionViewHolder holder, int position, @NonNull Transaction model) {
                        holder.setDescription(model.getDescription());
                        holder.setAmount(model.getAmount());
                        holder.setDate(model.getDate());
                        holder.setCurrencyType(currencyType);
                        holder.setAccountTitle(model.getAccountTitle());
                        holder.setType(model.getType(), holder);
                        holder.setCategory(model.getCategory(), holder);
                        String transactionId = getSnapshots().getSnapshot(position).getKey();
                    }

                    @NonNull
                    @Override
                    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Inflate the item layout and create an instance of AccountViewHolder
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.recycler_transactions_layout, parent, false);
                        return new TransactionViewHolder(view);
                    }
                };

        accountRecyclerView.setAdapter(accountAdapter);
        accountAdapter.startListening();
        tRecyclerView.setAdapter(tAdapter);
        tAdapter.startListening();
    }

    public void getCurrencyTypeFromDb(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserInformation userInfo = dataSnapshot.getValue(UserInformation.class);
                    if (userInfo != null) {
                        currencyType = userInfo.getCurrencyType();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        // Stop listening when the fragment is stopped
        if (accountRecyclerView.getAdapter() != null) {
            ((FirebaseRecyclerAdapter) accountRecyclerView.getAdapter()).stopListening();
        }

        if (tRecyclerView.getAdapter() != null) {
            ((FirebaseRecyclerAdapter) tRecyclerView.getAdapter()).stopListening();
        }
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder{
        View accountView;
        public AccountViewHolder(View itemView){
            super(itemView);
            accountView = itemView;
        }

        private void setCurrencyType(String cType){
            TextView cTypeView = accountView.findViewById(R.id.currency_type);
            cTypeView.setText(cType);
        }

        private void setTitle(String title){
            TextView accountTitle = accountView.findViewById(R.id.account_title);
            accountTitle.setText(title);
        }
        private void setBalance(Double balance){
            TextView accountBalance = accountView.findViewById(R.id.total_account_balance);
            String  balanceString =  String.valueOf( balance);
            accountBalance.setText(balanceString);
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder{
        View transactionView;
        private View iconBackground;
        private ImageView transactionTypeIcon;

        public TransactionViewHolder(View itemView){
            super(itemView);
            transactionView = itemView;
        }

        private void setCurrencyType(String cType){
            TextView cTypeView = transactionView.findViewById(R.id.currency_type);
            cTypeView.setText(cType);
        }

        private void setDescription(String description){
            TextView descriptionView = transactionView.findViewById(R.id.transaction_description);
            descriptionView.setText(description);
        }

        private void setAmount(Double amount){
            TextView amountView = transactionView.findViewById(R.id.transaction_amount);
            String  amountString =  String.valueOf( amount);
            amountView.setText(amountString);
        }
        private void setDate(String date){
            TextView dateView = transactionView.findViewById(R.id.transaction_date);
            dateView.setText(date);
        }

        private void setAccountTitle(String accountTitle){
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


    private void addAccountView(){
        addAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountDataInsert();
            }
        });
    }
    public void insertInitialTransaction(double dAmount, String description, String date, String type, String tAccountId, String tAccountTitle, String tCategory){
        String id = mTransactionDatabase.push().getKey();
        Transaction tData = new Transaction(dAmount, description, id,date, type, tAccountId, tAccountTitle, tCategory);
        mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getContext(), "Account saved successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void accountDataInsert(){
        AlertDialog.Builder accountDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater accountInflater = LayoutInflater.from(getActivity());
        View tView = accountInflater.inflate(R.layout.account_addition_layout, null);
        accountDialog.setView(tView);
        AlertDialog dialog = accountDialog.create();

        EditText editTitle = tView.findViewById(R.id.edit_account_title);
        EditText editInitialBalance = tView.findViewById(R.id.edit_initial_balance);

        Button btnSave = tView.findViewById(R.id.btn_account_save);
        Button btnCancel = tView.findViewById(R.id.btn_account_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                String initialBalance = editInitialBalance.getText().toString().trim();


                if(TextUtils.isEmpty(title)){
                    editTitle.setError("Title is required!!");
                    return;
                }
                if(TextUtils.isEmpty(initialBalance)){
                    editInitialBalance.setError("Initial balance is required!!");
                    return;
                }
                double dInitialBalance = Double.parseDouble(initialBalance);

                String accountId = mAccountDatabase.push().getKey();
                Account aData = new Account(title, dInitialBalance);
                mAccountDatabase.child(accountId).setValue(aData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            LocalDate currentDate = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            insertInitialTransaction(dInitialBalance, "Initial Deposit", currentDate.format(formatter), "Income", accountId, title, "Income");
                        } else {
                            Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}