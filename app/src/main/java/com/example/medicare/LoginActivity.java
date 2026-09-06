package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSubmitLogin, btnCreateAccount;
    private TextView tvGoToRegister, tvForgotPassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener registerListener = v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        };
        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(registerListener);
        }
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(registerListener);
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        }

        btnSubmitLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        // Authenticate with Supabase Auth and fetch the user's role from public.profiles
        SupabaseClient.getInstance().signIn(email, password, new SupabaseClient.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);

                // Save session in SharedPreferences
                SupabaseClient.getInstance().saveSession(
                        LoginActivity.this,
                        user,
                        SupabaseClient.getInstance().getAuthToken()
                );

                String role = user.getRole();
                if (role == null) {
                    role = "patient";
                }
                role = role.toLowerCase().trim();

                String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                        ? user.getFullName()
                        : email;
                Toast.makeText(LoginActivity.this, "Welcome " + displayName, Toast.LENGTH_SHORT).show();

                // Role-based redirection:
                // patient -> DashboardActivity
                // doctor  -> DoctorDashboardActivity
                // admin   -> AdminDashboardActivity
                Intent intent;
                if ("doctor".equals(role)) {
                    intent = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                } else if ("admin".equals(role)) {
                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, DashboardActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showResetPasswordDialog();
            return;
        }

        sendPasswordReset(email);
    }

    private void showResetPasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter registered email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your registered email address to receive password reset instructions.")
                .setView(input)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String enteredEmail = input.getText().toString().trim();
                    if (TextUtils.isEmpty(enteredEmail) || !Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                        Toast.makeText(LoginActivity.this, "Please provide a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendPasswordReset(enteredEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordReset(String email) {
        setLoading(true);
        SupabaseClient.getInstance().resetPassword(email, new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmitLogin.setEnabled(!loading);
        if (btnCreateAccount != null) {
            btnCreateAccount.setEnabled(!loading);
        }
        if (tvForgotPassword != null) {
            tvForgotPassword.setEnabled(!loading);
        }
    }
}

