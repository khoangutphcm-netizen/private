package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onToggleRole(User user);
        void onDeleteUser(User user);
    }

    private List<User> userList;
    private OnUserActionListener listener;

    public AdminUserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.tvFullName.setText(user.getFullName() != null ? user.getFullName() : "No Name");
        holder.tvRole.setText("ROLE: " + user.getRole().toUpperCase() + (user.isDisabled() ? " · ĐÃ KHÓA" : ""));

        if (user.isAdmin()) {
            holder.btnToggleRole.setText("Đổi quyền");
            holder.btnToggleRole.setBackgroundColor(0xFF757575);
        } else {
            holder.btnToggleRole.setText("Đổi quyền");
            holder.btnToggleRole.setBackgroundColor(0xFFF57C00);
        }

        holder.btnToggleRole.setOnClickListener(v -> {
            if (listener != null) listener.onToggleRole(user);
        });

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteUser(user);
        });
        holder.btnDeleteUser.setContentDescription(user.isDisabled() ? "Mở khóa tài khoản" : "Khóa tài khoản");
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvFullName, tvRole;
        Button btnToggleRole;
        ImageButton btnDeleteUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvFullName = itemView.findViewById(R.id.tvUserFullName);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnToggleRole = itemView.findViewById(R.id.btnToggleRole);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
