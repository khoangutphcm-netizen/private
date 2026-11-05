package com.example.myapplication;

public class User {
    private int id;
    private String email;
    private String password;
    private String fullName;
    private String role; // "admin" or "user"
    private int avatar; // Resource ID for user profile photo
    private boolean disabled;
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public User(int id, String email, String password, String fullName, String role, int avatar) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.avatar = avatar;
    }

    public User(int id, String email, String password, String fullName, String role) {
        this(id, email, password, fullName, role, R.drawable.ic_user);
    }

    public User(String email, String password, String fullName, String role) {
        this(0, email, password, fullName, role, R.drawable.ic_user);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @com.google.firebase.database.Exclude
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getAvatar() { return avatar != 0 ? avatar : R.drawable.ic_user; }
    public void setAvatar(int avatar) { this.avatar = avatar; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
