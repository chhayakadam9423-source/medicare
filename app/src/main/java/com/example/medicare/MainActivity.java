package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

/**
 * The initial landing screen of Medicare - Hospital Management System.
 * Displays the healthcare branding and direct access to [Login] and [Create Account].
 */
public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if an authenticated user session already exists
        if (SupabaseClient.getInstance().loadSession(this)) {
            User user = SupabaseClient.getInstance().getCurrentUser();
            if (user != null) {
                String role = user.getRole();
                if (role == null) role = "patient";
                role = role.toLowerCase().trim();

                Intent intent;
                if ("doctor".equals(role)) {
                    intent = new Intent(this, DoctorDashboardActivity.class);
                } else if ("admin".equals(role)) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, DashboardActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
