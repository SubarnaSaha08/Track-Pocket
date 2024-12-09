package com.example.trackpocket;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.core.content.ContextCompat;

import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.Transaction;
import com.example.trackpocket.Model.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AccountDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mAccountDatabase, mTransactionDatabase, mUserDatabase;
    private EditText editDate;
    private TextView accountTitleTextView, accountBalanceTextView, incomeCurrencyTypeView, expenseCurrencyTypeView;
    private String accountId, accountTitle;
    double accountBalance;
    private String currencyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_detail);
        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        currencyType = getIntent().getStringExtra("CURRENCY_TYPE");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        assert accountId != null;
        mAccountDatabase = database.getReference().child("AccountData").child(uid).child(accountId);
        mUserDatabase = database.getReference().child("UserData").child(uid);
        mTransactionDatabase = database.getReference().child("TransactionData").child(uid);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        fetchAccountData(accountId);

        editDate = findViewById(R.id.edit_date);
        editDate.setOnClickListener(v -> showDatePickerDialog());

        transactionDataInsert();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTransactionDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIncome = 0;
                double totalExpense = 0;

                Calendar today = Calendar.getInstance();
                Calendar oneWeekAgo = Calendar.getInstance();
                oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7);

                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        if (isDateValid(today, oneWeekAgo,transaction.getDate())) {
                            if ("income".equalsIgnoreCase(transaction.getType())) {
                                totalIncome += transaction.getAmount();
                            } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                                totalExpense += transaction.getAmount();
                            }
                        }
                    }
                }
                TextView incomeTextView = findViewById(R.id.last_week_income);
                TextView expenseTextView = findViewById(R.id.last_week_expense);

                incomeTextView.setText(String.format(" %.2f", totalIncome));
                expenseTextView.setText(String.format(" %.2f", totalExpense));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountDetailActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isDateValid(Calendar end, Calendar start, String dateString) {


        int day = Integer.parseInt(dateString.substring(0, 2));
        int month = Integer.parseInt(dateString.substring(3, 5)) - 1;
        int year = Integer.parseInt(dateString.substring(6, 10));

        Calendar givenCalendar = Calendar.getInstance();
        givenCalendar.set(Calendar.YEAR, year);
        givenCalendar.set(Calendar.MONTH, month);
        givenCalendar.set(Calendar.DAY_OF_MONTH, day);

        return (givenCalendar.after(start) || isSameDay(givenCalendar, start)) &&
                (givenCalendar.before(end) || isSameDay(givenCalendar, end));


    }

    public static boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void fetchAccountData(String accountId) {

        accountTitleTextView = findViewById(R.id.account_title);
        accountBalanceTextView = findViewById(R.id.total_account_balance);
        incomeCurrencyTypeView = findViewById(R.id.last_week_income_ctype);
        expenseCurrencyTypeView = findViewById(R.id.last_week_expense_ctype);
        if (accountId != null) {
            mAccountDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Account account = dataSnapshot.getValue(Account.class);
                        if (account != null) {
                            accountBalance = account.getBalance();
                            accountTitle = account.getTitle();
                            accountTitleTextView.setText(account.getTitle());
                            accountBalanceTextView.setText(String.format(currencyType+" %s", account.getBalance()));
                            expenseCurrencyTypeView.setText(currencyType);
                            incomeCurrencyTypeView.setText(currencyType);
                        }
                    } else {
                        Toast.makeText(AccountDetailActivity.this, "Account not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AccountDetailActivity.this, "Failed to load account data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void transactionDataInsert(){
        EditText editDescription = findViewById(R.id.edit_transaction_description);
        EditText editAmount = findViewById(R.id.edit_transaction_amount);
        RadioGroup radioGroupType = findViewById(R.id.transaction_type_radio_group);
        final String[] selectedTransactionType = {""};
        editDate = findViewById(R.id.edit_date);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button btnSave = findViewById(R.id.btn_transaction_save);
        Button btnCancel = findViewById(R.id.btn_transaction_cancel);

        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                selectedTransactionType[0] = selectedRadioButton.getText().toString();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = editAmount.getText().toString().trim();
                String type = selectedTransactionType[0].trim();
                String date = editDate.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                String tAccountId = accountId;
                String tAccountTitle = accountTitle;

                if(TextUtils.isEmpty(description)){
                    editDate.setError("Description is required!!");
                }
                if(TextUtils.isEmpty(amount)){
                    editAmount.setError("Amount is required!!");
                    return;
                }
                if(TextUtils.isEmpty(date)){
                    editDate.setError("Date is required!!");
                }
                double dAmount = Double.parseDouble(amount);
                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(AccountDetailActivity.this, "Type is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }


                String id = mTransactionDatabase.push().getKey();
                Transaction tData = new Transaction(dAmount, description, id,date, type, tAccountId, tAccountTitle);
                mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updateAccountBalance(dAmount, type);
                        } else {
                            Toast.makeText(AccountDetailActivity.this, "Failed to save transaction data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void updateAccountBalance(double transactionAmount, String transactionType) {
        double newAmount = accountBalance;
        if(Objects.equals(transactionType, "Income"))
        {
            newAmount += transactionAmount;
        }
        else if(Objects.equals(transactionType, "Expense"))
        {
            newAmount -= transactionAmount;
        }
        mAccountDatabase.child("balance").setValue(newAmount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AccountDetailActivity.this, "Transaction data saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AccountDetailActivity.this, "Failed to save transaction data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
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