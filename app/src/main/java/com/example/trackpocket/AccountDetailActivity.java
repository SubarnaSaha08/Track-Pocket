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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AccountDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mAccountDatabase, mTransactionDatabase, mUserDatabase;
    private EditText editDate, editReportStartDate, editReportEndDate;
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
        editDate.setOnClickListener(v -> showDatePickerDialog1());
        editReportStartDate = findViewById(R.id.edit_report_start_date);
        editReportStartDate.setOnClickListener(v -> showDatePickerDialog2());
        editReportEndDate = findViewById(R.id.edit_report_end_date);
        editReportEndDate.setOnClickListener(v -> showDatePickerDialog3());

        transactionDataInsert();
        GenerateReport();
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
                        if (Objects.equals(transaction.getAccountId(), accountId) && isDateValid(today, oneWeekAgo, convertDateString(transaction.getDate()))) {
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

    public Calendar convertDateString(String dateString) {
        int i = 0, j = 0;
        for (int ind = 0; ind < dateString.length(); ind++) {
            char c = dateString.charAt(ind);
            j = ind;
            if(c < '0' || c > '9'){
                break;
            }
        }
        int day = Integer.parseInt(dateString.substring(i, j));
        i = j + 1;
        for (int ind = i; ind < dateString.length(); ind++) {
            char c = dateString.charAt(ind);
            j = ind;
            if(c < '0' || c > '9'){
                break;
            }
        }
        int month = Integer.parseInt(dateString.substring(i, j))- 1;
        i = j + 1;
        for (int ind = i; ind < dateString.length(); ind++) {
            char c = dateString.charAt(ind);
            j = ind;
        }
        int year = Integer.parseInt(dateString.substring(i, j+1));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    public boolean isDateValid(Calendar end, Calendar start, Calendar givenCalendar) {
        return (isDayAfter(start, givenCalendar) || isSameDay(givenCalendar, start)) &&
                (isDayAfter(givenCalendar, end) || isSameDay(givenCalendar, end));
    }

    public static boolean isDayAfter(Calendar calendar1, Calendar calendar2) {
        return (calendar1.get(Calendar.YEAR) < calendar2.get(Calendar.YEAR) ||
                (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) < calendar2.get(Calendar.MONTH)) ||
                (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                        calendar1.get(Calendar.DAY_OF_MONTH) < calendar2.get(Calendar.DAY_OF_MONTH)));
    }

    public static boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH));
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
        Spinner editCategory = findViewById(R.id.edit_category_spinner);
        final String[] selectedTransactionType = {""};

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
                String category = String.valueOf(editCategory.getSelectedItem());
                String tAccountId = accountId;
                String tAccountTitle = accountTitle;

                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AccountDetailActivity.this, "Description is required!!", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(category) || Objects.equals(category, "Select Category")) {
                    Toast.makeText(AccountDetailActivity.this, "Category is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(amount)){
                    Toast.makeText(AccountDetailActivity.this, "Amount is required!!", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(date)){
                    Toast.makeText(AccountDetailActivity.this, "Date is required!!", Toast.LENGTH_SHORT).show();
                }
                else if (selectedId == -1)  {
                    Toast.makeText(AccountDetailActivity.this, "Type is required!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    double dAmount = Double.parseDouble(amount);
                    String id = mTransactionDatabase.push().getKey();
                    Transaction tData = new Transaction(dAmount, description, id,date, type, tAccountId, tAccountTitle, category);
                    mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateAccountBalance(dAmount, type);
                                Intent intent = new Intent(v.getContext(), AccountDetailActivity.class);
                                intent.putExtra("ACCOUNT_ID", accountId);
                                intent.putExtra("CURRENCY_TYPE", currencyType);
                                v.getContext().startActivity(intent);
                            } else {
                                Toast.makeText(AccountDetailActivity.this, "Failed to save transaction data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void GenerateReport() {
        Button btnGenerate = findViewById(R.id.btn_generate_report);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDate = editReportStartDate.getText().toString().trim();
                String endDate = editReportEndDate.getText().toString().trim();

                if(TextUtils.isEmpty(startDate)){
                    Toast.makeText(AccountDetailActivity.this, "Start date is required!!", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(endDate)){
                    Toast.makeText(AccountDetailActivity.this, "End date is required!!", Toast.LENGTH_SHORT).show();
                }
                else if(convertDateString(startDate).after(convertDateString(endDate)) || isSameDay(convertDateString(startDate), convertDateString(endDate))){
                    Toast.makeText(AccountDetailActivity.this, "Invalid Date range!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(v.getContext(), GenerateReportActivity.class);
                    intent.putExtra("ACCOUNT_ID", accountId);
                    intent.putExtra("START_DATE", startDate);
                    intent.putExtra("END_DATE", endDate);
                    intent.putExtra("CURRENCY_TYPE", currencyType);
                    v.getContext().startActivity(intent);
                }
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

    private void showDatePickerDialog1() {
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

    private void showDatePickerDialog2() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (editReportStartDate != null) {
                            String dateText = dayOfMonth + "/" + (month + 1) + "/" + year;
                            editReportStartDate.setText(dateText);
                        }
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void showDatePickerDialog3() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (editReportEndDate != null) {
                            String dateText = dayOfMonth + "/" + (month + 1) + "/" + year;
                            editReportEndDate.setText(dateText);
                        }
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }
}