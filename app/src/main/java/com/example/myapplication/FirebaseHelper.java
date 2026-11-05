package com.example.myapplication;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/** Shared endpoint. All data operations live in CloudRepository. */
public final class FirebaseHelper {
    private static FirebaseHelper instance;
    private final DatabaseReference database;
    private FirebaseHelper() {
        database = FirebaseDatabase.getInstance(
                "https://foodnow-app-22dff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();
    }
    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }
    public DatabaseReference getDatabaseRef() { return database; }
}
