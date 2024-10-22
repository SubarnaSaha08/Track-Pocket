package com.example.trackpocket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.Transaction;
import com.example.trackpocket.adapter.AccountAdapter;
import com.example.trackpocket.adapter.TransactionAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private FloatingActionButton floatingActionButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mTransactionDatabase, mAccountDatabase;
    private EditText editDate;
    private RecyclerView tRecyclerView,accountRecyclerView;
    private AccountAdapter adapter;
    private TransactionAdapter tAdapter;
    private View addAccount;
    private boolean isDataLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mTransactionDatabase = database.getReference().child("TransactionData").child(uid);
        mAccountDatabase = database.getReference().child("AccountData").child(uid);

        addAccount = myView.findViewById((R.id.add_account));
//        floatingActionButton = myView.findViewById(R.id.floating_button);
        List<Transaction> tList = new ArrayList<>();
        tList.add(new Transaction(150.75, "Food", "txn_001", "Dinner at restaurant", "2024-10-07", "Expense"));
        tList.add(new Transaction(3000.45, "Salary", "txn_002", "Got salary from job", "2024-10-01", "Income"));

        tRecyclerView = myView.findViewById(R.id.recycler_id_transaction);
        tRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        tAdapter = new TransactionAdapter(tList);
        tRecyclerView.setAdapter(tAdapter);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//        tRecyclerView.setHasFixedSize(true);
//        tRecyclerView.setLayoutManager(layoutManager);
//        addTransactionView();

//        Account RECYCLER PART
        accountRecyclerView = myView.findViewById(R.id.accounts_recycler);
        LinearLayoutManager layoutManagerAccount = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerAccount.setStackFromEnd(true);
        layoutManagerAccount.setReverseLayout(true);
        accountRecyclerView.setHasFixedSize(true);
        accountRecyclerView.setLayoutManager(layoutManagerAccount);


        addAccountView();
        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Account> options =
                new FirebaseRecyclerOptions.Builder<Account>()
                        .setQuery(mAccountDatabase, Account.class)
                        .build();

        FirebaseRecyclerAdapter<Account, AccountViewHolder> accountAdapter =
                new FirebaseRecyclerAdapter<Account, AccountViewHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull AccountViewHolder holder, int position, @NonNull Account model) {
                        holder.setTitle(model.getTitle());
                        holder.setBalance(model.getBalance());
                    }

                    @NonNull
                    @Override
                    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Inflate the item layout and create an instance of AccountViewHolder
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.recycler_account_layout, parent, false);
                        return new AccountViewHolder(view);
                    }
                };
        accountRecyclerView.setAdapter(accountAdapter);
        accountAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening when the fragment is stopped
        if (accountRecyclerView.getAdapter() != null) {
            ((FirebaseRecyclerAdapter) accountRecyclerView.getAdapter()).stopListening();
        }
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder{
        View accountView;
        public AccountViewHolder(View itemView){
            super(itemView);
            accountView = itemView;
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

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View tView;

        public MyViewHolder(View itemView){
            super(itemView);
            tView = itemView;
        }

        private void setCategory(String category){
            TextView tCategory = tView.findViewById(R.id.transaction_category);
            tCategory.setText(category);
        }

        private void setDate(String date){
            TextView tDate = tView.findViewById(R.id.transaction_date);
            tDate.setText(date);
        }

        private void setAmount(Double amount, String type){
            TextView tAmount = tView.findViewById(R.id.transaction_amount);
            String amountString =  String.valueOf(amount);
            if (type.equals("Income")) {
                amountString = "+" + amountString + "tk";
                tAmount.setTextColor(ContextCompat.getColor(tAmount.getContext(), R.color.green));
            } else {
                amountString = "-" + amountString + "tk";
                tAmount.setTextColor(ContextCompat.getColor(tAmount.getContext(), R.color.red));
            }
            tAmount.setText(amountString);
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
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Account saved successfully", Toast.LENGTH_SHORT).show();
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

    private void addTransactionView(){
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionDataInsert();
            }
        });
    }

    public void transactionDataInsert(){
        AlertDialog.Builder transactionDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View tView = inflater.inflate(R.layout.insertion_layout, null);
        transactionDialog.setView(tView);
        AlertDialog dialog = transactionDialog.create();

        EditText editAmount = tView.findViewById(R.id.edit_amount);
        RadioGroup radioGroupType = tView.findViewById(R.id.radio_group_transaction_type);
        final String[] selectedTransactionType = {""};
        AutoCompleteTextView editTransactionCat = tView.findViewById(R.id.edit_transaction_category);
        EditText editNote = tView.findViewById(R.id.edit_note);
        editDate = tView.findViewById(R.id.edit_date);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button btnSave = tView.findViewById(R.id.btn_transaction_save);
        Button btnCancel = tView.findViewById(R.id.btn_transaction_cancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.transaction_categories, android.R.layout.simple_dropdown_item_1line);
        editTransactionCat.setAdapter(adapter);

        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = tView.findViewById(checkedId);
                selectedTransactionType[0] = selectedRadioButton.getText().toString();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = editTransactionCat.getText().toString().trim();
                String amount = editAmount.getText().toString().trim();
                String type = selectedTransactionType[0].trim();
                String note = editNote.getText().toString().trim();
                String date = editDate.getText().toString().trim();

                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(getContext(), "Type is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(amount)){
                    editAmount.setError("Amount is required!!");
                    return;
                }
                double dAmount = Double.parseDouble(amount);
                if(TextUtils.isEmpty(category)){
                    editTransactionCat.setError("Category is required!!");
                }
                if(TextUtils.isEmpty(category)){
                    editDate.setError("Date is required!!");
                }

                String id = mTransactionDatabase.push().getKey();
                Transaction tData = new Transaction(dAmount,category, id, note, date, type);
                mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          if (task.isSuccessful()) {
                              Toast.makeText(getContext(), "Transaction data saved successfully", Toast.LENGTH_SHORT).show();
                          } else {
                              Toast.makeText(getContext(), "Failed to save transaction data", Toast.LENGTH_SHORT).show();
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

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (editDate != null) {
                            String dateText = dayOfMonth + "/" + (month + 1) + "/" + year;
                            editDate.setText(dateText);
                        }
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }
}