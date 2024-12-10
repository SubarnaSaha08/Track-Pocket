package com.example.trackpocket;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenerateReportActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mAccountDatabase, mTransactionDatabase, mUserDatabase;
    private String accountId, startDate, endDate;
    private TextView dateRangeView, totalIncomeView, totalExpenseView, totalBalanceView;
    private String currencyType;
    private RecyclerView tRecyclerView;
    private List<Transaction> transactionsList;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_report);
        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        startDate = getIntent().getStringExtra("START_DATE");
        endDate = getIntent().getStringExtra("END_DATE");
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

        tRecyclerView = findViewById(R.id.transactions_recycler);
        dateRangeView = findViewById(R.id.report_date_range);
        dateRangeView.setText(startDate+" - "+endDate);
        totalIncomeView = findViewById(R.id.total_income);
        totalExpenseView = findViewById(R.id.total_expense);
        totalBalanceView = findViewById(R.id.total_balance);

        getTransactionsForReport(startDate, endDate, new TransactionCallback() {
            @Override
            public void onTransactionsFetched(List<Transaction> transactions) {
                double total_income = 0.0;
                double total_expense = 0.0;
                double total_balance = 0.0;
                for (Transaction transaction : transactions) {
                    if ("income".equalsIgnoreCase(transaction.getType())) {
                        total_income += transaction.getAmount();
                    } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                        total_expense += transaction.getAmount();
                    }
                }
                total_balance = total_income - total_expense;
                totalIncomeView.setText("+"+currencyType+" "+String.format(" %.2f", total_income));
                totalExpenseView.setText("-"+currencyType+" "+String.format(" %.2f", total_expense));
                if(total_balance > 0.00){
                    totalBalanceView.setText("+"+currencyType+" "+String.format(" %.2f", total_balance));
                    totalBalanceView.setTextColor(getResources().getColor(R.color.green));
                }
                else if(total_balance < 0.00){
                    totalBalanceView.setText("-"+currencyType+" "+String.format(" %.2f", total_balance));
                    totalBalanceView.setTextColor(getResources().getColor(R.color.red));
                }
                else{
                    totalBalanceView.setText(currencyType+" "+String.format(" %.2f", total_balance));
                    totalBalanceView.setTextColor(getResources().getColor(R.color.green));
                }

                TransactionAdapter adapter = new TransactionAdapter(transactions, currencyType);
                LinearLayoutManager layoutManagerTransaction = new LinearLayoutManager(GenerateReportActivity.this, LinearLayoutManager.VERTICAL, false);
                layoutManagerTransaction.setStackFromEnd(true);
                layoutManagerTransaction.setReverseLayout(true);
                tRecyclerView.setLayoutManager(layoutManagerTransaction);
                tRecyclerView.setAdapter(adapter);
            }
        });

    }

    public interface TransactionCallback {
        void onTransactionsFetched(List<Transaction> transactions);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void getTransactionsForReport(String startDate, String endDate, TransactionCallback callback) {
        mTransactionDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Transaction> transactionList = new ArrayList<>();

                for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        if (Objects.equals(transaction.getAccountId(), accountId) && isDateValid(convertDateString(endDate),convertDateString(startDate), convertDateString(transaction.getDate()))) {
                            transactionList.add(transaction);
                        }
                    }
                }
                callback.onTransactionsFetched(transactionList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "onCancelled: " + databaseError.getMessage());
                callback.onTransactionsFetched(new ArrayList<>()); // Return empty list on failure
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
}