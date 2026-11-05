package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) { loadUserProfile(); }
    }
    @Override public void onStart() {
        super.onStart();
        dbHelper.observe(cloudRefresh);
        cloudRefresh.run();
    }
    @Override public void onStop() {
        dbHelper.unobserve(cloudRefresh);
        super.onStop();
    }

    private ImageView ivAvatar;
    private TextView tvUserName, tvUserEmail, tvUserRole;
    private Button btnEditProfile, btnChangePassword, btnOpenAdmin, btnSignOut;
    private CloudRepository dbHelper;

    private int currentUserId = -1;
    private int selectedAvatarResId = R.drawable.ic_user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = CloudRepository.getInstance(getContext());

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnOpenAdmin = view.findViewById(R.id.btnOpenAdmin);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnSignOut.setOnClickListener(v -> {
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                prefs.edit().clear().apply();

                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadUserProfile() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = dbHelper.currentUserId();

        User user = dbHelper.getUserById(currentUserId);
        if (user != null) {
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());
            String roleText = "KHÁCH HÀNG";
            if (user.isAdmin()) roleText = "QUẢN TRỊ VIÊN";
            else if ("shipper".equalsIgnoreCase(user.getRole())) roleText = "SHIPPER / TÀI XẾ";

            tvUserRole.setText("Vai trò: " + roleText);
            selectedAvatarResId = user.getAvatar();
            ivAvatar.setImageResource(selectedAvatarResId);

            if (user.isAdmin()) {
                btnOpenAdmin.setVisibility(View.VISIBLE);
                btnOpenAdmin.setText("MỞ TRANG QUẢN TRỊ ADMIN");
                btnOpenAdmin.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), AdminActivity.class);
                    startActivity(intent);
                });
            } else if ("shipper".equalsIgnoreCase(user.getRole())) {
                btnOpenAdmin.setVisibility(View.VISIBLE);
                btnOpenAdmin.setText("MỞ TRANG GIAO HÀNG SHIPPER");
                btnOpenAdmin.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ShipperActivity.class);
                    startActivity(intent);
                });
            } else {
                btnOpenAdmin.setVisibility(View.GONE);
            }
        } else {
            String name = prefs.getString("user_name", "Nguyễn Văn A");
            String email = prefs.getString("user_email", "user@app.com");
            String role = prefs.getString("user_role", "user");

            tvUserName.setText(name);
            tvUserEmail.setText(email);
            tvUserRole.setText("Vai trò: " + ("admin".equalsIgnoreCase(role) ? "QUẢN TRỊ VIÊN" : "KHÁCH HÀNG"));
            ivAvatar.setImageResource(R.drawable.ic_user);
        }
    }

    private void showEditProfileDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText etEditFullName = dialogView.findViewById(R.id.etEditFullName);
        ImageView ivOpt1 = dialogView.findViewById(R.id.ivAvatarOpt1);
        ImageView ivOpt2 = dialogView.findViewById(R.id.ivAvatarOpt2);
        ImageView ivOpt3 = dialogView.findViewById(R.id.ivAvatarOpt3);
        ImageView ivOpt4 = dialogView.findViewById(R.id.ivAvatarOpt4);

        User user = dbHelper.getUserById(currentUserId);
        if (user != null) {
            etEditFullName.setText(user.getFullName());
            selectedAvatarResId = user.getAvatar();
        } else {
            etEditFullName.setText(tvUserName.getText().toString());
        }

        final int[] chosenAvatar = {selectedAvatarResId};

        View.OnClickListener avatarClickListener = v -> {
            ivOpt1.setBackgroundResource(android.R.color.transparent);
            ivOpt2.setBackgroundResource(android.R.color.transparent);
            ivOpt3.setBackgroundResource(android.R.color.transparent);
            ivOpt4.setBackgroundResource(android.R.color.transparent);

            v.setBackgroundResource(R.drawable.bg_green_border_button);

            int id = v.getId();
            if (id == R.id.ivAvatarOpt1) chosenAvatar[0] = R.drawable.ic_user;
            else if (id == R.id.ivAvatarOpt2) chosenAvatar[0] = R.drawable.ic_mascot;
            else if (id == R.id.ivAvatarOpt3) chosenAvatar[0] = R.drawable.ic_driver;
            else if (id == R.id.ivAvatarOpt4) chosenAvatar[0] = R.drawable.ic_food;
        };

        ivOpt1.setOnClickListener(avatarClickListener);
        ivOpt2.setOnClickListener(avatarClickListener);
        ivOpt3.setOnClickListener(avatarClickListener);
        ivOpt4.setOnClickListener(avatarClickListener);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Lưu Thay Đổi", (dialog, which) -> {
                    String newName = etEditFullName.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(getContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CloudUi.write(ProfileFragment.this, dbHelper.updateUserProfile(currentUserId, newName, chosenAvatar[0]), () -> {
                        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                        prefs.edit().putString("user_name", newName).apply();

                        Toast.makeText(getContext(), "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Đổi Mật Khẩu", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (newPass.isEmpty()) {
                        Toast.makeText(getContext(), "Mật khẩu mới không được để trống!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    com.google.firebase.auth.FirebaseUser account = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    if (account == null || account.getEmail() == null) return;
                    account.reauthenticate(com.google.firebase.auth.EmailAuthProvider.getCredential(account.getEmail(), oldPass))
                            .continueWithTask(task -> {
                                if (!task.isSuccessful()) throw task.getException();
                                return account.updatePassword(newPass);
                            }).addOnCompleteListener(task -> {
                                if (isAdded()) Toast.makeText(getContext(), task.isSuccessful()
                                        ? "Đã đổi mật khẩu Firebase thành công!" : "Không thể đổi mật khẩu. Kiểm tra mật khẩu cũ và kết nối.", Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
