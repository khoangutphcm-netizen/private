package com.example.myapplication;

import android.content.Context;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;

final class CloudUi {
    static int number(Context context, String value) {
        try { int number=Integer.parseInt(value); if(number>=0&&number<=100000000) return number; }
        catch(NumberFormatException ignored) {}
        Toast.makeText(context,"Nhập số hợp lệ từ 0 đến 100.000.000.",Toast.LENGTH_LONG).show();
        return -1;
    }
    static void write(Context context, Task<Void> task, Runnable success) {
        Toast.makeText(context, "Đang gửi thay đổi…", Toast.LENGTH_SHORT).show();
        Context app=context.getApplicationContext();
        task.addOnSuccessListener(ignored -> {
            if(context instanceof android.app.Activity) {
                android.app.Activity activity=(android.app.Activity)context;
                if(activity.isFinishing()||activity.isDestroyed()) return;
            }
            success.run();
        }).addOnFailureListener(error -> Toast.makeText(app, "Chưa lưu được: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }
    static void write(androidx.fragment.app.Fragment fragment, Task<Void> task, Runnable success) {
        Context context=fragment.getContext();
        if(context==null) return;
        write(context,task,()->{if(fragment.isAdded()&&fragment.getView()!=null) success.run();});
    }
}
