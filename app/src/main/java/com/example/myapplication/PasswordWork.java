package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Password derivation and migration must not block the UI. */
final class PasswordWork {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    interface Result<T> { void complete(T value); }
    static <T> void run(Callable<T> work, Result<T> success, Runnable failure) {
        WORKER.execute(() -> {
            Handler main = new Handler(Looper.getMainLooper());
            try {
                T result = work.call();
                main.post(() -> success.complete(result));
            } catch (Exception e) {
                Log.e("PasswordWork", "Credential operation failed", e);
                main.post(failure);
            }
        });
    }
}
