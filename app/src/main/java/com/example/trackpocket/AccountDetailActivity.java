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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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


import java.util.Objects;

public class AccountDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mAccountDatabase, mTransactionDatabase;
    private EditText editDate;
    private TextView accountTitleTextView, accountBalanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_detail);
        String accountId = getIntent().getStringExtra("ACCOUNT_ID");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        assert accountId != null;
        mAccountDatabase = database.getReference().child("AccountData").child(uid).child(accountId);
        mTransactionDatabase = database.getReference().child("AccountData").child(uid).child(accountId).child("TransactionData");

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void fetchAccountData(String accountId) {

        accountTitleTextView = findViewById(R.id.account_title);
        accountBalanceTextView = findViewById(R.id.total_account_balance);
        if (accountId != null) {
            mAccountDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Account account = dataSnapshot.getValue(Account.class);
                        if (account != null) {
                            // Display account data
                            accountTitleTextView.setText(account.getTitle());
                            accountBalanceTextView.setText(String.format("BDT %s", account.getBalance()));
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


                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(AccountDetailActivity.this, "Type is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(amount)){
                    editAmount.setError("Amount is required!!");
                    return;
                }
                double dAmount = Double.parseDouble(amount);
                if(TextUtils.isEmpty(description)){
                    editDate.setError("Description is required!!");
                }
                if(TextUtils.isEmpty(date)){
                    editDate.setError("Date is required!!");
                }

                String id = mTransactionDatabase.push().getKey();
                Transaction tData = new Transaction(dAmount, description, id,date, type);
                mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, // Use 'this' to refer to the current Activity's context
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