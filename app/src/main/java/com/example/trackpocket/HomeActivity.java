package com.example.trackpocket;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.net.Uri;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.example.trackpocket.Model.UserInformation;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private DashboardFragment dashboardFragment;
    private ScanBillFragment scanBillFragment;
    private NavigationView navigationView;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;
    private TextView userNameTextView, userPhoneTextView;
    private ImageView profileImageView;
    private View headerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        database = FirebaseDatabase.getInstance("https://expensemanager-cc64b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mUserDatabase = database.getReference().child("UserData");
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("Track Pocket");
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.user);
        userNameTextView = headerView.findViewById(R.id.user_name);
        userPhoneTextView = headerView.findViewById(R.id.user_phone_number);
        // Fetch user information to display in header
        fetchUserInfo(uid, userNameTextView, userPhoneTextView, profileImageView);

        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        frameLayout = findViewById(R.id.main_frame);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        Drawable customIcon = ContextCompat.getDrawable(this, R.drawable.drawer_icon);
        toggle.setHomeAsUpIndicator(customIcon);
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dashboardFragment = new DashboardFragment();
        scanBillFragment = new ScanBillFragment();
        setFragment(dashboardFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    int itemId = item.getItemId();
                    if(itemId == R.id.dashboard_icon){
                        setFragment(dashboardFragment);
                        return true;
                    } else if(itemId == R.id.scanner_icon){
                        setFragment(scanBillFragment);
                        return true;
                    } else {
                        return false;
                    }
                }
        );
    }

    private void fetchUserInfo(String uid, TextView userNameTextView, TextView userPhoneTextView, ImageView profileImageView) {
        mUserDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserInformation userInfo = dataSnapshot.getValue(UserInformation.class);
                    if (userInfo != null) {
                        userNameTextView.setText(userInfo.getName());
                        userPhoneTextView.setText(userInfo.getPhoneNumber());
                        StorageReference imageRef = mStorageRef.child(uid + ".jpg");

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(HomeActivity.this)
                                        .load(uri)
                                        .into(profileImageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e("FirebaseStorage", "Error getting download URL", exception);
                            }
                        });
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "No user data found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame, fragment);
        ft.commit();
    }

    public void displaySelectedListener(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.dashboard_icon) {
            fragment = new DashboardFragment();
            return;
        }
        if (itemId == R.id.logout_icon) {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return;
        }
        if (itemId == R.id.edit_profile_icon) {
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
            return;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, fragment);
            ft.commit();
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedListener(item.getItemId());
        return true;
    }
}