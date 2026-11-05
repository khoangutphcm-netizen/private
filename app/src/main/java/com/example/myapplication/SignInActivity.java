package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtEmail, txtPassword;
    private Button btnSignIn, btnSignUp;
    private TextView btnForgot, btnSignInFacebook, btnSignInGoogle;

    private CloudRepository dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        dbHelper = CloudRepository.getInstance(this);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnForgot = findViewById(R.id.btnForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInFacebook = findViewById(R.id.btnSignInFacebook);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);

        btnForgot.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnSignInFacebook.setOnClickListener(this);
        btnSignInGoogle.setOnClickListener(this);

        btnSignIn.setEnabled(true);
        btnSignUp.setEnabled(true);
        txtEmail.setHint("Địa chỉ email");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSignIn) {
            handleSignIn();
        } else if (id == R.id.btnForgot) {
            String email = txtEmail.getText().toString().trim();
            if (email.isEmpty()) { txtEmail.setError("Nhập email để đặt lại mật khẩu"); return; }
            com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(vv -> Toast.makeText(this, "Đã yêu cầu đặt lại mật khẩu. Kiểm tra email.", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Không thể gửi yêu cầu đặt lại mật khẩu.", Toast.LENGTH_LONG).show());
        } else if (id == R.id.btnSignUp) {
            showSignUpDialog();
        } else if (id == R.id.btnSignInFacebook) {
            Toast.makeText(this, "Đăng nhập bằng Facebook (Simulated)", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btnSignInGoogle) {
            Toast.makeText(this, "Đăng nhập bằng Google (Simulated)", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignIn() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString();

        if (email.isEmpty()) {
            txtEmail.setError("Vui lòng nhập Email");
            txtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Vui lòng nhập Mật khẩu");
            txtPassword.requestFocus();
            return;
        }

        btnSignIn.setEnabled(false);
        btnSignUp.setEnabled(false);
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    FirebaseHelper.getInstance(this).getDatabaseRef().child("users").child(uid).get()
                            .addOnSuccessListener(snapshot -> {
                                String role = snapshot.child("role").getValue(String.class);
                                String name = snapshot.child("fullName").getValue(String.class);
                                if (Boolean.TRUE.equals(snapshot.child("disabled").getValue(Boolean.class)) || (!"admin".equals(role) && !"user".equals(role) && !"shipper".equals(role))) {
                                    firebaseLoginFailed("Thiếu hồ sơ users/" + uid + " hoặc role không hợp lệ.");
                                    return;
                                }
                                dbHelper.start(uid, role).addOnSuccessListener(ignored -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    getSharedPreferences("UserSession", MODE_PRIVATE).edit().putString("firebase_uid", uid).apply();
                                    completeSignIn(dbHelper.getUserById(dbHelper.currentUserId()));
                                }).addOnFailureListener(e -> firebaseLoginFailed("Không tải được dữ liệu Firebase: " + e.getMessage()));
                            }).addOnFailureListener(e -> firebaseLoginFailed("Không đọc được hồ sơ. Kiểm tra kết nối và Database Rules."));
                }).addOnFailureListener(e -> firebaseLoginFailed("Đăng nhập Firebase thất bại. Kiểm tra email, mật khẩu và Internet."));
    }

    private void firebaseLoginFailed(String message) {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        btnSignIn.setEnabled(true);
        btnSignUp.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void completeSignIn(User user) {
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            prefs.edit()
                    .putInt("user_id", user.getId())
                    .putString("user_email", user.getEmail())
                    .putString("user_name", user.getFullName())
                    .putString("user_role", user.getRole())
                    .apply();

            Toast.makeText(this, "Xin chào, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();

            Intent intent;
            if (user.isAdmin()) {
                intent = new Intent(SignInActivity.this, AdminActivity.class);
            } else if ("shipper".equalsIgnoreCase(user.getRole())) {
                intent = new Intent(SignInActivity.this, ShipperActivity.class);
            } else {
                intent = new Intent(SignInActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sai Email hoặc Mật khẩu!", Toast.LENGTH_LONG).show();
        }
    }

    private void showSignUpDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signup, null);

        EditText etName = dialogView.findViewById(R.id.etSignUpName);
        EditText etEmail = dialogView.findViewById(R.id.etSignUpEmail);
        EditText etPass = dialogView.findViewById(R.id.etSignUpPassword);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Tạo Tài Khoản", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String pass = etPass.getText().toString();

                    if (email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(SignInActivity.this, "Email và Mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(result -> {
                                java.util.Map<String, Object> profile = new java.util.HashMap<>();
                                profile.put("email", email);
                                profile.put("fullName", name.isEmpty() ? "Khách hàng" : name);
                                profile.put("role", "user");
                                profile.put("avatar", "ic_user");
                                FirebaseHelper.getInstance(this).getDatabaseRef().child("users").child(result.getUser().getUid())
                                        .setValue(profile).addOnCompleteListener(saved -> {
                                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                                            txtEmail.setText(email);
                                            txtPassword.setText("");
                                            Toast.makeText(this, saved.isSuccessful() ? "Đăng ký thành công! Hãy đăng nhập."
                                                    : "Tài khoản đã tạo nhưng lưu hồ sơ thất bại. Cần bổ sung users/UID trên Firebase.", Toast.LENGTH_LONG).show();
                                        });
                            }).addOnFailureListener(e -> Toast.makeText(this, "Đăng ký thất bại. Kiểm tra email, mật khẩu (ít nhất 6 ký tự) và Internet.", Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
