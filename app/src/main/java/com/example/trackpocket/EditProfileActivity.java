package com.example.trackpocket;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.trackpocket.Model.Account;
import com.example.trackpocket.Model.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;
    private static final int PICK_IMAGE = 1;
    private ImageView userProfilePicture;
    private EditText userNameInput;
    private EditText userPhoneInput;
    private Uri imageUri;
    private Button btnSkip;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();

        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mUserDatabase = database.getReference().child("UserData").child(uid);
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        userProfilePicture = findViewById(R.id.user_profile_picture);
        Button btnUploadImage = findViewById(R.id.btn_upload_image);
        userNameInput = findViewById(R.id.user_name);
        userPhoneInput = findViewById(R.id.user_phone_number);
        btnSkip = findViewById(R.id.btn_skip);
        btnSave = findViewById(R.id.btn_save);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSkip.setOnClickListener(view -> {
            Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        btnSave.setOnClickListener(view -> saveUserInfo(uid));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            userProfilePicture.setImageURI(imageUri);
        }
    }

    private void saveUserInfo(String uid) {
        String name = userNameInput.getText().toString().trim();
        String phoneNumber = userPhoneInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        String createdAt = String.valueOf(System.currentTimeMillis());
        String updatedAt = createdAt;
        if (imageUri != null) {
            final StorageReference fileReference = mStorageRef.child(uid + ".jpg");
            String imageUrl = (imageUri != null) ? imageUri.toString() : null;

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserInformation userInformation = new UserInformation(uid, name, phoneNumber, createdAt, updatedAt);

                        mUserDatabase.setValue(userInformation).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileActivity.this, "User information saved successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to save user information. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // If no image was selected, still save user info without image URL
            UserInformation userInformation = new UserInformation(uid, name, phoneNumber, createdAt, updatedAt);
            mUserDatabase.setValue(userInformation).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "User information saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to save user information. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


