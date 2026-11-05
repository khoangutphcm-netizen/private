package com.example.myapplication;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

/** Re-enter login after process recreation, sign-out, disabled account or role change. */
public abstract class CloudActivity extends AppCompatActivity {
    private final FirebaseAuth.AuthStateListener session = auth -> checkSession();
    private void checkSession() {
        CloudRepository repo = CloudRepository.getInstance(this);
        boolean wrongRole = this instanceof AdminActivity && !"admin".equals(repo.getRole())
                || this instanceof ShipperActivity && !"shipper".equals(repo.getRole());
        if (!repo.isReady() || wrongRole) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent); finish();
        }
    }
    @Override protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(session);
        checkSession();
    }
    @Override protected void onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(session);
        super.onStop();
    }
}
