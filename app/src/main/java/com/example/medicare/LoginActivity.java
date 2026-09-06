package com.example.medicare;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

public class LoginActivity extends AppCompatActivity {

    public static final String ROLE_PATIENT = "patient";
    public static final String ROLE_DOCTOR = "doctor";
    public static final String ROLE_ADMIN = "admin";

    private String selectedRole = ROLE_PATIENT;

    // Role Selection Views
    private MaterialCardView cardRolePatient, cardRoleDoctor, cardRoleAdmin;
    private ImageView ivRolePatient, ivRoleDoctor, ivRoleAdmin;
    private TextView tvRolePatient, tvRoleDoctor, tvRoleAdmin;

    // Form Fields & Buttons
    private EditText etEmail, etPassword;
    private Button btnSubmitLogin, btnCreateAccount;
    private TextView tvGoToRegister, tvForgotPassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    // Registration & Notice Containers
    private LinearLayout layoutPatientRegister;
    private View layoutRoleNotice;
    private TextView tvRoleNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
        updateRoleUI(selectedRole);
    }

    private void initViews() {
        // Role selectors
        cardRolePatient = findViewById(R.id.cardRolePatient);
        cardRoleDoctor = findViewById(R.id.cardRoleDoctor);
        cardRoleAdmin = findViewById(R.id.cardRoleAdmin);

        ivRolePatient = findViewById(R.id.ivRolePatient);
        ivRoleDoctor = findViewById(R.id.ivRoleDoctor);
        ivRoleAdmin = findViewById(R.id.ivRoleAdmin);

        tvRolePatient = findViewById(R.id.tvRolePatient);
        tvRoleDoctor = findViewById(R.id.tvRoleDoctor);
        tvRoleAdmin = findViewById(R.id.tvRoleAdmin);

        // Inputs & Actions
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Role notices
        layoutPatientRegister = findViewById(R.id.layoutPatientRegister);
        layoutRoleNotice = findViewById(R.id.layoutRoleNotice);
        tvRoleNotice = findViewById(R.id.tvRoleNotice);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Role selector click listeners
        cardRolePatient.setOnClickListener(v -> selectRole(ROLE_PATIENT));
        cardRoleDoctor.setOnClickListener(v -> selectRole(ROLE_DOCTOR));
        cardRoleAdmin.setOnClickListener(v -> selectRole(ROLE_ADMIN));

        // Register navigation
        View.OnClickListener registerListener = v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        };
        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(registerListener);
        }
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(registerListener);
        }

        // Forgot password
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        }

        // Submit login
        btnSubmitLogin.setOnClickListener(v -> handleLogin());
    }

    private void selectRole(String role) {
        this.selectedRole = role;
        updateRoleUI(role);
    }

    private void updateRoleUI(String role) {
        boolean isPatient = ROLE_PATIENT.equalsIgnoreCase(role);
        boolean isDoctor = ROLE_DOCTOR.equalsIgnoreCase(role);
        boolean isAdmin = ROLE_ADMIN.equalsIgnoreCase(role);

        // Style Patient Card
        styleRoleCard(cardRolePatient, ivRolePatient, tvRolePatient, isPatient);
        // Style Doctor Card
        styleRoleCard(cardRoleDoctor, ivRoleDoctor, tvRoleDoctor, isDoctor);
        // Style Admin Card
        styleRoleCard(cardRoleAdmin, ivRoleAdmin, tvRoleAdmin, isAdmin);

        // Update Dynamic Registration / Notice section
        if (isPatient) {
            if (layoutPatientRegister != null) layoutPatientRegister.setVisibility(View.VISIBLE);
            if (layoutRoleNotice != null) layoutRoleNotice.setVisibility(View.GONE);
        } else if (isDoctor) {
            if (layoutPatientRegister != null) layoutPatientRegister.setVisibility(View.GONE);
            if (layoutRoleNotice != null) layoutRoleNotice.setVisibility(View.VISIBLE);
            if (tvRoleNotice != null) {
                tvRoleNotice.setText(R.string.doctor_account_notice);
            }
        } else {
            if (layoutPatientRegister != null) layoutPatientRegister.setVisibility(View.GONE);
            if (layoutRoleNotice != null) layoutRoleNotice.setVisibility(View.VISIBLE);
            if (tvRoleNotice != null) {
                tvRoleNotice.setText(R.string.admin_account_notice);
            }
        }
    }

    private void styleRoleCard(MaterialCardView card, ImageView icon, TextView text, boolean isSelected) {
        if (card == null || icon == null || text == null) return;

        if (isSelected) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_green_surface));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.primary_green));
            card.setStrokeWidth(dpToPx(2));
            card.setCardElevation(dpToPx(2));

            icon.setColorFilter(ContextCompat.getColor(this, R.color.primary_green));
            text.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
            text.setTypeface(null, Typeface.BOLD);
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.medicare_card));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.medicare_border));
            card.setStrokeWidth(dpToPx(1));
            card.setCardElevation(0);

            icon.setColorFilter(ContextCompat.getColor(this, R.color.medicare_text_secondary));
            text.setTextColor(ContextCompat.getColor(this, R.color.medicare_text_secondary));
            text.setTypeface(null, Typeface.NORMAL);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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
                String actualRole = user.getRole();
                if (actualRole == null) {
                    actualRole = ROLE_PATIENT;
                }
                actualRole = actualRole.toLowerCase().trim();

                // Validate selected role against user's actual database profile role
                if (!selectedRole.equalsIgnoreCase(actualRole)) {
                    setLoading(false);
                    // Do not retain session for mismatched role
                    SupabaseClient.getInstance().signOut(LoginActivity.this);
                    Toast.makeText(
                            LoginActivity.this,
                            getString(R.string.role_mismatch_error),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                setLoading(false);

                // Save session in SharedPreferences
                SupabaseClient.getInstance().saveSession(
                        LoginActivity.this,
                        user,
                        SupabaseClient.getInstance().getAuthToken()
                );

                String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                        ? user.getFullName()
                        : email;
                Toast.makeText(LoginActivity.this, "Welcome " + displayName, Toast.LENGTH_SHORT).show();

                // Role-based redirection:
                // patient -> DashboardActivity
                // doctor  -> DoctorDashboardActivity
                // admin   -> AdminDashboardActivity
                Intent intent;
                if (ROLE_DOCTOR.equalsIgnoreCase(actualRole)) {
                    intent = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                } else if (ROLE_ADMIN.equalsIgnoreCase(actualRole)) {
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
        if (tvGoToRegister != null) {
            tvGoToRegister.setEnabled(!loading);
        }
        cardRolePatient.setEnabled(!loading);
        cardRoleDoctor.setEnabled(!loading);
        cardRoleAdmin.setEnabled(!loading);
    }
}
