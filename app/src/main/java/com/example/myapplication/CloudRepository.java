package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

/** Firebase is the source of truth. Integer handles are only for existing UI adapters. */
public final class CloudRepository {
    private static CloudRepository instance;
    private final Context context;
    private final DatabaseReference root;
    private final Map<String, DataSnapshot> snapshots = new HashMap<>();
    private final Map<String, Integer> handles = new HashMap<>();
    private final Map<Integer, String> keys = new HashMap<>();
    private final Map<Query, ValueEventListener> subscriptions = new HashMap<>();
    private final Set<Runnable> observers = new HashSet<>();
    private final List<TaskCompletionSource<Void>> pendingInitial = new ArrayList<>();
    private int nextHandle = 1;
    private String uid;
    private String role;
    private boolean ready;
    private int generation;
    private final android.os.Handler simulationHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Set<Integer> simulations = new HashSet<>();
    private CloudRepository(Context context) {
        this.context = context.getApplicationContext();
        root = FirebaseHelper.getInstance(context).getDatabaseRef();
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            if (auth.getCurrentUser() == null || (uid != null && !uid.equals(auth.getCurrentUser().getUid()))) stop();
        });
    }
    public static synchronized CloudRepository getInstance(Context context) {
        if (instance == null) instance = new CloudRepository(context);
        return instance;
    }
    private int handle(String key) {
        if (key == null || key.isEmpty()) return 0;
        if (!handles.containsKey(key)) { handles.put(key, nextHandle); keys.put(nextHandle++, key); }
        return handles.get(key);
    }
    private String key(int id) {
        String key = keys.get(id);
        if (key == null) throw new IllegalArgumentException("Dữ liệu đã thay đổi. Vui lòng mở lại màn hình.");
        return key;
    }
    public boolean isReady() { return ready && FirebaseAuth.getInstance().getCurrentUser() != null; }
    public String getRole() { return role; }
    public int currentUserId() { return handle(uid); }
    public void observe(Runnable listener) { observers.add(listener); }
    public void unobserve(Runnable listener) { observers.remove(listener); }
    private void notifyChanged() { for (Runnable r : new ArrayList<>(observers)) r.run(); }
    public void stop() {
        CheckoutDraft.clear();
        generation++;
        for(TaskCompletionSource<Void> pending:pendingInitial) pending.trySetException(new IllegalStateException("Phiên đồng bộ đã kết thúc."));
        pendingInitial.clear();
        simulationHandler.removeCallbacksAndMessages(null); simulations.clear();
        for (Map.Entry<Query, ValueEventListener> entry : subscriptions.entrySet()) entry.getKey().removeEventListener(entry.getValue());
        subscriptions.clear(); snapshots.clear(); ready = false; uid = null; role = null;
    }
    public Task<Void> start(String userUid, String userRole) {
        stop(); uid = userUid; role = userRole;
        final int session = generation;
        List<Task<?>> initial = new ArrayList<>();
        initial.add(watch("restaurants", root.child("restaurants")));
        initial.add(watch("foods", root.child("foods")));
        initial.add(watch("options", root.child("options")));
        initial.add(watch("users", "admin".equals(role) ? root.child("users") : root.child("users").orderByKey().equalTo(uid)));
        initial.add(watch("orders", "user".equals(role) ? root.child("orders").orderByChild("ownerUid").equalTo(uid) : root.child("orders")));
        return Tasks.whenAll(initial).continueWith(task -> {
            if (session != generation) throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
            if (!task.isSuccessful()) { stop(); throw task.getException(); }
            ready = true;
            return null;
        });
    }
    private Task<Void> watch(String name, Query query) {
        final int session = generation;
        TaskCompletionSource<Void> first = new TaskCompletionSource<>();
        pendingInitial.add(first);
        ValueEventListener listener = new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (session != generation) return;
                snapshots.put(name, snapshot);
                if ("users".equals(name)) {
                    DataSnapshot own = snapshot.child(uid);
                    if (!own.exists() || bool(own, "disabled") || !role.equals(str(own, "role"))) {
                        first.trySetException(new IllegalStateException("Tài khoản bị khóa hoặc quyền đã thay đổi. Hãy đăng nhập lại."));
                        FirebaseAuth.getInstance().signOut();
                        notifyChanged(); return;
                    }
                }
                first.trySetResult(null); notifyChanged();
            }
            public void onCancelled(DatabaseError error) {
                if (session != generation) return;
                first.trySetException(error.toException());
                android.widget.Toast.makeText(context, "Không đồng bộ được " + name + ": " + error.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                if(ready && error.getCode()==DatabaseError.PERMISSION_DENIED) FirebaseAuth.getInstance().signOut();
            }
        };
        subscriptions.put(query, listener); query.addValueEventListener(listener);
        return first.getTask();
    }
    private Iterable<DataSnapshot> rows(String collection) {
        DataSnapshot snapshot = snapshots.get(collection);
        return snapshot == null ? Collections.emptyList() : snapshot.getChildren();
    }
    private DataSnapshot row(String collection, int id) {
        DataSnapshot s = snapshots.get(collection);
        return s == null ? null : s.child(key(id));
    }
    private static String str(DataSnapshot s, String field) {
        Object value = s == null ? null : s.child(field).getValue(); return value == null ? "" : value.toString();
    }
    private static int num(DataSnapshot s, String field) {
        Object value = s == null ? null : s.child(field).getValue(); return value instanceof Number ? ((Number)value).intValue() : 0;
    }
    private static boolean bool(DataSnapshot s, String field) { return s != null && Boolean.TRUE.equals(s.child(field).getValue()); }
    private String asset(int id) { try { return context.getResources().getResourceEntryName(id); } catch (Exception e) { return "ic_user"; } }
    private int image(DataSnapshot s, String field, int fallback) {
        String name = str(s, field); int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return id == 0 ? fallback : id;
    }
    private static Map<String,Object> map(Object... pairs) {
        Map<String,Object> result = new HashMap<>();
        for (int i=0;i<pairs.length;i+=2) result.put((String)pairs[i], pairs[i+1]);
        return result;
    }
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> list = new ArrayList<>();
        for (DataSnapshot s : rows("restaurants")) list.add(new Restaurant(handle(s.getKey()), str(s,"name"), image(s,"logo",BrandLogos.forName(str(s,"name"))), image(s,"cover",R.drawable.cover_menu_1), str(s,"address"), str(s,"openHours")));
        return list;
    }
    public List<Food> getAllFoods() {
        List<Food> list = new ArrayList<>();
        for (DataSnapshot s : rows("foods")) {
            Food f = new Food(handle(s.getKey()), handle(str(s,"restaurantKey")), str(s,"name"), image(s,"image",R.drawable.ic_food), num(s,"price"), str(s,"description"), bool(s,"available"));
            f.setOptions(getOptionsForFood(f.getId())); list.add(f);
        }
        return list;
    }
    public ArrayList<Food> getFoodsByRestaurantId(int id) { ArrayList<Food> list = new ArrayList<>(); for (Food f:getAllFoods()) if(f.getRestaurantId()==id) list.add(f); return list; }
    public List<FoodOption> getOptionsForFood(int id) {
        List<FoodOption> list = new ArrayList<>();
        for (DataSnapshot s:rows("options")) if (handle(str(s,"foodKey"))==id) list.add(new FoodOption(handle(s.getKey()),id,str(s,"optionName"),num(s,"extraPrice")));
        return list;
    }
    private User user(DataSnapshot s) { User user=new User(handle(s.getKey()),str(s,"email"),null,str(s,"fullName"),str(s,"role"),image(s,"avatar",R.drawable.ic_user)); user.setDisabled(bool(s,"disabled")); return user; }
    public List<User> getAllUsers() { List<User> list=new ArrayList<>(); for(DataSnapshot s:rows("users")) list.add(user(s)); return list; }
    public User getUserById(int id) { DataSnapshot s=row("users",id); return s==null||!s.exists()?null:user(s); }
    public List<Order> getAllOrders() {
        List<Order> list=new ArrayList<>();
        for(DataSnapshot s:rows("orders")) {
            int id=handle(s.getKey());
            Order o=new Order(id,handle(str(s,"ownerUid")),str(s,"userEmail"),num(s,"totalPrice"),str(s,"date"),str(s,"status"),handle(str(s,"shipperUid")),str(s,"shipperName"));
            o.setDeliveryProgress(num(s,"deliveryProgress")); o.setCloudKey(s.getKey());
            try {
                org.json.JSONArray items=new org.json.JSONArray(str(s,"itemsJson"));
                for(int i=0;i<items.length();i++) {
                    org.json.JSONObject item=items.getJSONObject(i);
                    o.getItems().add(new OrderItem(0,id,handle(item.getString("foodKey")),item.getString("foodName"),item.getInt("price"),item.getInt("quantity")));
                }
            } catch(org.json.JSONException invalid) { android.util.Log.w("CloudRepository","Order item snapshot is invalid: "+s.getKey()); }
            list.add(o);
        }
        Collections.reverse(list); return list;
    }
    public List<Order> getOrdersByUserId(int id) { List<Order> list=new ArrayList<>(); for(Order o:getAllOrders()) if(o.getUserId()==id) list.add(o); return list; }
    public List<Order> getAvailableOrdersForShipper() { List<Order> list=new ArrayList<>(); for(Order o:getAllOrders()) if("Chờ xác nhận".equals(o.getStatus())&&o.getShipperId()==0) list.add(o); return list; }
    public List<Order> getShipperActiveOrders(int id) { List<Order> list=new ArrayList<>(); for(Order o:getAllOrders()) if(o.getShipperId()==id&&"Đang giao".equals(o.getStatus())) list.add(o); return list; }
    public List<Order> getShipperHistoryOrders(int id) { List<Order> list=new ArrayList<>(); for(Order o:getAllOrders()) if(o.getShipperId()==id&&("Đã hoàn thành".equals(o.getStatus())||"Đã hủy".equals(o.getStatus()))) list.add(o); return list; }
    public long getTotalRevenue() { long total=0; for(Order o:getAllOrders()) if("Đã hoàn thành".equals(o.getStatus())) total+=o.getTotalPrice(); return total; }
    public int getTotalUsersCount(){return getAllUsers().size();}
    public int getTotalFoodsCount(){return getAllFoods().size();}
    public int getTotalOrdersCount(){return getAllOrders().size();}
    public List<FoodSaleStat> getFoodSalesStatistics() {
        Map<String,int[]> stats=new LinkedHashMap<>();
        for(Order o:getAllOrders()) if("Đã hoàn thành".equals(o.getStatus())) for(OrderItem item:o.getItems()) {
            int[] values=stats.computeIfAbsent(item.getFoodName(),n->new int[2]); values[0]+=item.getQuantity(); values[1]+=item.getPrice()*item.getQuantity();
        }
        List<FoodSaleStat> list=new ArrayList<>(); for(Map.Entry<String,int[]> e:stats.entrySet()) list.add(new FoodSaleStat(e.getKey(),e.getValue()[0],e.getValue()[1])); return list;
    }
    public Task<Void> addRestaurant(Restaurant r) { return root.child("restaurants").push().setValue(map("name",r.getName(),"address",r.getAddress(),"openHours",r.getOpenHoursRaw(),"logo",asset(r.getLogo()),"cover",asset(r.getCover()))); }
    public Task<Void> seedDemoCatalog() {
        if (!"admin".equals(role)) return Tasks.forException(new IllegalStateException("Chỉ admin được nạp dữ liệu mẫu."));
        if (!getAllRestaurants().isEmpty() || !getAllFoods().isEmpty()) return Tasks.forException(new IllegalStateException("Danh mục đã có dữ liệu; không ghi đè."));
        try (java.io.InputStream stream=context.getAssets().open("demo-catalog.json")) {
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
            byte[] buffer=new byte[4096]; int count;
            while((count=stream.read(buffer))!=-1) bytes.write(buffer,0,count);
            org.json.JSONObject json=new org.json.JSONObject(bytes.toString("UTF-8"));
            Map<String,Object> updates=new HashMap<>();
            for(String collection: new String[]{"restaurants","foods","options"}) {
                org.json.JSONObject entries=json.getJSONObject(collection);
                java.util.Iterator<String> ids=entries.keys();
                while(ids.hasNext()) { String id=ids.next(); updates.put(collection+"/"+id,jsonMap(entries.getJSONObject(id))); }
            }
            return root.updateChildren(updates);
        } catch(Exception e) { return Tasks.forException(e); }
    }
    private Map<String,Object> jsonMap(org.json.JSONObject json) throws org.json.JSONException {
        Map<String,Object> result=new HashMap<>(); java.util.Iterator<String> fields=json.keys();
        while(fields.hasNext()) {String field=fields.next(); result.put(field,json.get(field));}
        return result;
    }
    private Map<String,Object> food(Food f) { return map("name",f.getName(),"restaurantKey",key(f.getRestaurantId()),"price",f.getPrice(),"description",f.getDescription(),"available",f.isAvailable(),"image",asset(f.getImage())); }
    public Task<Void> addFood(Food f) { return root.child("foods").push().setValue(food(f)); }
    public Task<Void> updateFood(Food f) { return root.child("foods").child(key(f.getId())).setValue(food(f)); }
    public Task<Void> toggleFoodAvailability(int id,boolean available) { return root.child("foods").child(key(id)).child("available").setValue(available); }
    public Task<Void> addFoodOption(FoodOption o) { return root.child("options").push().setValue(map("foodKey",key(o.getFoodId()),"optionName",o.getOptionName(),"extraPrice",o.getExtraPrice())); }
    public Task<Void> deleteFoodOption(int id) { return root.child("options").child(key(id)).removeValue(); }
    public Task<Void> deleteFood(int id) {
        Map<String,Object> changes=map("foods/"+key(id),null);
        for(FoodOption o:getOptionsForFood(id)) changes.put("options/"+key(o.getId()),null);
        return root.updateChildren(changes);
    }
    public Task<Void> deleteRestaurant(int id) {
        Map<String,Object> changes=map("restaurants/"+key(id),null);
        for(Food f:getFoodsByRestaurantId(id)) { changes.put("foods/"+key(f.getId()),null); for(FoodOption o:getOptionsForFood(f.getId())) changes.put("options/"+key(o.getId()),null); }
        return root.updateChildren(changes);
    }
    public Task<Void> updateUserRole(int id,String newRole) { return root.child("users").child(key(id)).child("role").setValue(newRole); }
    public Task<Void> deleteUser(int id) { return root.child("users").child(key(id)).child("disabled").setValue(!bool(row("users",id),"disabled")); }
    public Task<Void> updateUserProfile(int id,String name,int avatar) { return root.child("users").child(key(id)).updateChildren(map("fullName",name,"avatar",asset(avatar))); }
    public Task<Void> registerUser(User user) {
        FirebaseApp secondary=FirebaseApp.initializeApp(context,FirebaseApp.getInstance().getOptions(),"create-"+UUID.randomUUID());
        FirebaseAuth auth=FirebaseAuth.getInstance(secondary);
        Task<Void> task=auth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword()).continueWithTask(created -> {
            if(!created.isSuccessful()) throw created.getException();
            return root.child("users").child(created.getResult().getUser().getUid()).setValue(map("email",user.getEmail(),"fullName",user.getFullName(),"role","user","avatar","ic_user"))
                    .continueWithTask(saved -> {
                        if(saved.isSuccessful()) return Tasks.forResult(null);
                        Exception failure=saved.getException();
                        return created.getResult().getUser().delete().continueWithTask(deleted -> Tasks.forException(failure));
                    });
        });
        task.addOnCompleteListener(done -> {auth.signOut();secondary.delete();}); return task;
    }
    public Task<Void> createOrder(Order order) {
        if(uid==null) return Tasks.forException(new IllegalStateException("Hãy đăng nhập lại."));
        List<Map<String,Object>> items=new ArrayList<>();
        for(OrderItem item:order.getItems()) {
            DataSnapshot f=row("foods",item.getFoodId());
            if(f==null||!f.exists()||!bool(f,"available")) return Tasks.forException(new IllegalStateException("Có món đã hết hàng hoặc bị xóa. Hãy chọn lại giỏ hàng."));
            items.add(map("foodKey",key(item.getFoodId()),"foodName",item.getFoodName(),"price",item.getPrice(),"quantity",item.getQuantity()));
        }
        DatabaseReference ref=root.child("orders").push(); order.setId(handle(ref.getKey()));
        return ref.setValue(map("ownerUid",uid,"userEmail",FirebaseAuth.getInstance().getCurrentUser().getEmail(),"totalPrice",order.getTotalPrice(),"date",order.getDate(),"status","Chờ xác nhận","shipperUid","","shipperName","","itemsJson",new org.json.JSONArray(items).toString(),"paymentStatus","simulated_paid","deliveryProgress",0));
    }
    public Task<Void> deleteOrder(int id) { return root.child("orders").child(key(id)).removeValue(); }
    public Task<Void> updateOrderStatus(int id,String status) { return root.child("orders").child(key(id)).child("status").setValue(status); }
    public Task<Void> assignOrderToShipper(int id,int shipperId,String name) {
        final String claimingUid=uid;
        if(claimingUid==null) return Tasks.forException(new IllegalStateException("Hãy đăng nhập lại."));
        TaskCompletionSource<Void> done=new TaskCompletionSource<>();
        root.child("orders").child(key(id)).runTransaction(new Transaction.Handler() {
            public Transaction.Result doTransaction(MutableData data) {
                if(data.getValue()==null) return Transaction.success(data);
                if(!"Chờ xác nhận".equals(data.child("status").getValue())||!"".equals(data.child("shipperUid").getValue())) return Transaction.abort();
                data.child("shipperUid").setValue(claimingUid); data.child("shipperName").setValue(name); data.child("status").setValue("Đang giao"); return Transaction.success(data);
            }
            public void onComplete(DatabaseError error,boolean committed,DataSnapshot snapshot) {
                if(error!=null) done.setException(error.toException());
                else if(!committed||snapshot==null||!claimingUid.equals(str(snapshot,"shipperUid"))) done.setException(new IllegalStateException("Đơn đã được nhận hoặc hủy."));
                else done.setResult(null);
            }
        },false); return done.getTask();
    }
    public Task<Void> updateProgress(int id,int progress,String text) { return root.child("orders").child(key(id)).updateChildren(map("deliveryProgress",progress,"simulateStatusText",text)); }
    public boolean isSimulating(int id) { return simulations.contains(id); }
    public void simulateDelivery(int id) {
        if (!simulations.add(id)) return;
        final int session=generation;
        Runnable tick=new Runnable() {
            public void run() {
                if(session!=generation) return;
                DataSnapshot order=row("orders",id);
                if(order==null || !"Đang giao".equals(str(order,"status")) || !Objects.equals(uid,str(order,"shipperUid"))) {simulations.remove(id);return;}
                int progress=Math.min(100,num(order,"deliveryProgress")+20);
                updateProgress(id,progress,progress==100?"Đã đến nơi":"Đang giao hàng").addOnCompleteListener(task -> {
                    if(session!=generation) return;
                    if(!task.isSuccessful()||progress==100) {simulations.remove(id);notifyChanged();}
                    else simulationHandler.postDelayed(this,1200);
                });
            }
        };
        simulationHandler.post(tick);
    }
}
