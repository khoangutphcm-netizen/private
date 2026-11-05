package com.example.myapplication;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.*;

public final class OrderDetails {
    public static void show(Context context, Order selected) {
        CloudRepository repo=CloudRepository.getInstance(context);
        Dialog dialog=new Dialog(context);
        ScrollView scroll=new ScrollView(context);
        LinearLayout body=new LinearLayout(context);body.setOrientation(LinearLayout.VERTICAL);
        int pad=(int)(24*context.getResources().getDisplayMetrics().density);body.setPadding(pad,pad,pad,pad);
        scroll.setBackgroundColor(Color.parseColor("#F5FAF6"));scroll.addView(body);dialog.setContentView(scroll);
        Runnable render=()->{
            Order current=null;for(Order item:repo.getAllOrders())if(item.getId()==selected.getId())current=item;
            body.removeAllViews();
            Button back=new Button(context);back.setText("← Đơn hàng của bạn");body.addView(back);back.setOnClickListener(v->dialog.dismiss());
            if(current==null){text(body,"Đơn hàng không còn tồn tại",20);return;}
            final Order order=current;
            text(body,order.getStatusVietnamese(),26);
            text(body,"Mã đơn: "+order.getDisplayId()+"\n"+order.getDate(),13);
            text(body,"Đã đặt  →  Đang giao  →  Hoàn tất",16);
            if("Đang giao".equals(order.getStatus())){
                ProgressBar progress=new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);progress.setProgress(order.getDeliveryProgress());body.addView(progress);
                text(body,"Tiến độ giao hàng: "+order.getDeliveryProgress()+"%\nNgười giao: "+order.getShipperName(),15);
            }
            text(body,"Chi tiết món",20);
            for(OrderItem item:order.getItems())text(body,item.getFoodName()+" × "+item.getQuantity()+"\n"+String.format(java.util.Locale.getDefault(),"%,d đ",(long)item.getPrice()*item.getQuantity()),16);
            text(body,String.format(java.util.Locale.getDefault(),"Tổng cộng  %,d đ",order.getTotalPrice()),22);
            text(body,"Thanh toán thử nghiệm đã xác nhận. Không phát sinh khoản thu thực tế.",14);
            String status=order.getStatus();
            if("Chờ xác nhận".equals(status)||"Đang giao".equals(status)){
                boolean cancel="Chờ xác nhận".equals(status);
                Button action=new Button(context);action.setText(cancel?"Hủy đơn hàng":"Tôi đã nhận hàng");body.addView(action);
                action.setOnClickListener(v->new AlertDialog.Builder(context).setTitle(cancel?"Hủy đơn này?":"Bạn đã nhận đủ món?")
                    .setNegativeButton("Quay lại",null).setPositiveButton("Xác nhận",(d,w)->{
                        action.setEnabled(false);
                        repo.updateOrderStatus(order.getId(),cancel?"Đã hủy":"Đã hoàn thành").addOnFailureListener(e->{action.setEnabled(true);Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();});
                    }).show());
            }
        };
        repo.observe(render);dialog.setOnDismissListener(d->repo.unobserve(render));render.run();dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    }
    private static void text(LinearLayout parent,String value,int size){TextView view=new TextView(parent.getContext());view.setText(value);view.setTextSize(size);view.setTextColor(Color.parseColor("#203B2D"));view.setPadding(0,16,0,16);parent.addView(view);}
}
