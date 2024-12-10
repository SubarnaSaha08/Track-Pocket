package com.example.trackpocket;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.Transaction;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveScannedBillActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mTransactionDatabase, mAccountDatabase;
    private String scannedResult, selectedAccount, selectedAccountId, selectedAccountBalance;
    private EditText editDescription, editDate, editAmount;
    private List<String> accountTitles;
    private Map<String, String> accountIdMap, accountBalanceMap, extractedValues;
    private RadioGroup radioGroupType;
    private Spinner accountSpinner;
    private final String[] selectedTransactionType = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_save_scanned_bill);

        if (getIntent() != null && getIntent().hasExtra("RESULT_KEY")) {
            scannedResult = getIntent().getStringExtra("RESULT_KEY");
        }
        assert scannedResult != null;
        extractedValues = extractValues(scannedResult);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mTransactionDatabase = database.getReference().child("TransactionData").child(uid);
        mAccountDatabase = database.getReference().child("AccountData").child(uid);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        radioGroupType = findViewById(R.id.transaction_type_radio_group);
        RadioButton defaultRadioButton = findViewById(R.id.radio_expense);
        selectedTransactionType[0] = defaultRadioButton.getText().toString();
        radioGroupType.check(defaultRadioButton.getId());
        editDescription = findViewById(R.id.edit_transaction_description);
        editDescription.setText(extractedValues.get("description"));
        editAmount = findViewById(R.id.edit_transaction_amount);
        editAmount.setText(extractedValues.get("amount"));
        editDate = findViewById(R.id.edit_date);
        editDate.setText(extractedValues.get("date"));
        editDate.setOnClickListener(v -> showDatePickerDialog(extractedValues.get("date")));

        transactionDataInsert();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        accountTitles = new ArrayList<>();
        accountIdMap = new HashMap<>();
        accountBalanceMap = new HashMap<>();
        accountSpinner = findViewById(R.id.account_spinner);
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedAccount = parentView.getItemAtPosition(position).toString();
                selectedAccountId = accountIdMap.get(selectedAccount);
                selectedAccountBalance = accountBalanceMap.get(selectedAccount);
                if(!Objects.equals(selectedAccount, "Select Account")){
                    Toast.makeText(SaveScannedBillActivity.this, selectedAccount+" account is selected.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });


        mAccountDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accountTitles.clear();
                accountIdMap.clear();
                accountBalanceMap.clear();

                accountTitles.add("Select Account");
                accountIdMap.put("Select Account", "-");
                accountBalanceMap.put("Select Account", "-1");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String accountTitle = snapshot.child("title").getValue(String.class);
                    Long accountBalanceLong = snapshot.child("balance").getValue(Long.class);
                    String accountBalance = accountBalanceLong != null ? accountBalanceLong.toString() : "0";
                    String accountId = snapshot.getKey();

                    if (accountTitle != null) {
                        accountTitles.add(accountTitle);
                        accountIdMap.put(accountTitle, accountId);
                        accountBalanceMap.put(accountTitle, accountBalance);
                    }
                }

                ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(SaveScannedBillActivity.this, R.layout.account_spinner_item, accountTitles);
                accountAdapter.setDropDownViewResource(R.layout.account_spinner_item);
                accountSpinner.setAdapter(accountAdapter);

                accountAdapter.notifyDataSetChanged();
                accountSpinner.setSelection(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }


    public Map<String, String> extractValues(String input) {
        Map<String, String> result = new HashMap<>();
        result.put("description", "");
        result.put("amount", "");
        result.put("date", "");
        result.put("currency", "");
        String[] lines = input.split("\n");

        for (String line : lines) {
            if (line.startsWith("Description:")) {
                result.put("description", line.substring("Description:".length()).trim());
            } else if (line.startsWith("Bill:")) {
                result.put("amount", line.substring("Bill:".length()).trim());
            } else if (line.startsWith("Date:")) {
                result.put("date", line.substring("Date:".length()).trim());
            } else if (line.startsWith("Currency:")) {
                result.put("currency", line.substring("Currency:".length()).trim());
            }
        }

        return result;
    }

    public void transactionDataInsert(){
        editDescription = findViewById(R.id.edit_transaction_description);
        editAmount = findViewById(R.id.edit_transaction_amount);
        radioGroupType = findViewById(R.id.transaction_type_radio_group);
        editDate = findViewById(R.id.edit_date);
        Spinner editCategory = findViewById(R.id.edit_category_spinner);

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
                    Toast.makeText(SaveScannedBillActivity.this, "Type is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(selectedAccount) || Objects.equals(selectedAccount, "Select Account")) {
                    Toast.makeText(SaveScannedBillActivity.this, "Target account is required!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = mTransactionDatabase.push().getKey();
                Transaction tData = new Transaction(dAmount, description, id,date, type, selectedAccountId, selectedAccount, category);
                mTransactionDatabase.child(id).setValue(tData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updateAccountBalance(dAmount, type);
                        } else {
                            Toast.makeText(SaveScannedBillActivity.this, "Failed to save transaction data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void updateAccountBalance(double transactionAmount, String transactionType) {
        double newAmount =  Double.parseDouble(selectedAccountBalance);
        if(Objects.equals(transactionType, "Income"))
        {
            newAmount += transactionAmount;
        }
        else if(Objects.equals(transactionType, "Expense"))
        {
            newAmount -= transactionAmount;
        }
        mAccountDatabase.child(selectedAccountId).child("balance").setValue(newAmount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SaveScannedBillActivity.this, "Transaction data saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SaveScannedBillActivity.this, "Failed to save transaction data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog(String defaultDate) {
        final Calendar calendar = Calendar.getInstance();

        if (defaultDate != null && !defaultDate.isEmpty()) {
            String[] dateParts = defaultDate.split("/");
            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);
                calendar.set(year, month, day);
            }
        }
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