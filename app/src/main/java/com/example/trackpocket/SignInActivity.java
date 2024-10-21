package com.example.trackpocket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private Button btnLogin;
    private TextView mForgetPassword;
    private TextView mSignUpHere;
    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
        mDialog=new ProgressDialog(this);
        loginDetails();
    }

    private void loginDetails() {
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);
        btnLogin = findViewById(R.id.btn_login);
        mSignUpHere = findViewById(R.id.sign_up_here);
        mForgetPassword = findViewById(R.id.forget_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String pass = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required!!");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    mPassword.setError("Password is required!!");
                    return;
                }

                mDialog.setMessage("Logging in....");
                mDialog.show();

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login successful..", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }else{
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login failed!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mSignUpHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });

        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ResetPassActivity.class));
            }
        });
    }
}