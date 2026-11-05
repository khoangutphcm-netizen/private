FOODNOW — HƯỚNG DẪN CHẠY VÀ NỘP BÀI

Project Firebase: foodnow-app-22dff
Realtime Database: https://foodnow-app-22dff-default-rtdb.asia-southeast1.firebasedatabase.app
Package Android: com.example.myapplication

1. CẤU HÌNH MỘT LẦN TRƯỚC KHI GỬI CHO THẦY

- Authentication: bật Email/Password. Các tài khoản demo do chủ project tạo:
  admin@gmail.com, user@gmail.com, shipper@gmail.com.
  Mật khẩu là mật khẩu đã đặt trong Firebase, KHÔNG phải mật khẩu SQLite cũ.
- Realtime Database > Data: với mỗi UID đầy đủ từ Authentication, tạo
  users/UID với email, fullName, role (admin / user / shipper), avatar="ic_user".
  Không đặt mật khẩu hoặc hash mật khẩu trong Realtime Database.
  Ví dụ hồ sơ admin:
  {"email":"admin@gmail.com","fullName":"Quản trị viên","role":"admin","avatar":"ic_user"}
- Realtime Database > Rules: thay nội dung bằng database.rules.json rồi Publish.
  Dùng file database.rules.json mới, KHÔNG dùng firebase-account.rules.json cũ.
  Việc sửa file trên máy không tự thay đổi Rules đang chạy trên Firebase.
- Đăng nhập admin trong app. Khi danh mục trống, chọn Nạp dữ liệu mẫu.
  App tải 4 nhà hàng, 16 món, 9 topping lên Firebase. Không tạo tài khoản hoặc đơn giả.
- Gửi mật khẩu của ba tài khoản demo cho thầy qua kênh nộp bài riêng.
  Không cần gửi quyền quản trị Firebase Console hoặc service-account key.

2. CHẠY ỨNG DỤNG

- Cài app/build/outputs/apk/debug/app-debug.apk trên Android 7.0 trở lên.
- Hoặc mở thư mục chứa settings.gradle bằng Android Studio, dùng Gradle JDK 17,
  cài Android SDK 33, Sync Gradle rồi Run.
- Cần Internet để đăng nhập, tải dữ liệu và xác nhận các thay đổi.
- Sau khi tiến trình app bị hệ điều hành đóng, app yêu cầu đăng nhập lại để tải
  đầy đủ dữ liệu và quyền hiện hành.
- Mỗi máy dùng cùng project Firebase nên tài khoản và dữ liệu dùng chung.

3. CÁC CHỨC NĂNG ĐÃ CHUYỂN SANG FIREBASE

- Đăng nhập/đăng ký, gửi email đặt lại mật khẩu, đổi mật khẩu bằng xác thực lại.
- Hồ sơ và ảnh đại diện theo UID; ảnh là tên tài nguyên kèm APK, không phải mã
  resource ID của riêng từng bản build.
- Nhà hàng, món ăn, trạng thái còn/hết, topping: đọc realtime và admin quản lý.
- Giỏ hàng chọn số lượng/topping. Menu hoặc giá thay đổi thì yêu cầu chọn lại giỏ.
- Thanh toán hoàn toàn mô phỏng: thành công tạo đơn; thất bại/hủy giữ giỏ.
- Đơn dùng Firebase push key, khách chỉ đọc đơn của mình; admin/shipper đọc các
  đơn phục vụ quản lý/giao hàng. Các máy tự cập nhật qua listener.
- Nhận đơn bằng transaction: hai shipper tranh cùng một đơn chỉ một người thắng.
- Tiến độ giao mô phỏng được ghi lên Firebase và hiện ở màn hình đơn khách.
- Khách hủy đơn đang chờ; khách/shipper xác nhận hoàn thành đơn đang giao.
- Doanh thu và thống kê món chỉ tính đơn hoàn thành; thu nhập shipper 15.000đ/đơn.
- Admin tạo tài khoản Authentication mà không mất phiên admin, đổi quyền, khóa/
  mở khóa truy cập ứng dụng. Khóa KHÔNG xóa vĩnh viễn tài khoản Authentication.
  Nếu cần xóa vĩnh viễn, chủ project xóa trong Authentication Console.
- Admin không tự khóa hoặc tự đổi quyền của chính mình.
- SQLite cũ được giữ trong mã nguồn để tham khảo; màn hình hiện tại không dùng
  dữ liệu/tài khoản SQLite làm nguồn chính, và không tự đẩy dữ liệu cũ lên cloud.

4. KỊCH BẢN TEST HAI MÁY

1) Máy A đăng nhập user, máy B đăng nhập admin; sửa giá hoặc còn/hết trên B và
   kiểm tra menu trên A cập nhật.
2) Máy A đặt đơn với thanh toán mô phỏng thành công; admin thấy đơn mới.
3) Máy B đăng nhập shipper, nhận đơn; khách thấy trạng thái Đang giao.
4) Chạy mô phỏng ship; khách thấy phần trăm tiến độ; xác nhận giao xong.
5) Admin kiểm tra doanh thu, shipper kiểm tra lịch sử và thu nhập.
6) Tạo đơn thứ hai rồi hủy khi đang chờ; đơn không còn trong danh sách cần giao.
7) Admin tạo user mới, đổi role, khóa rồi mở khóa; kiểm tra đăng nhập tương ứng.

5. KIỂM TRA VÀ GIỚI HẠN

- Kiểm thử Rules bằng Firebase Emulator: hồ sơ, tự nâng quyền bị từ chối,
  danh mục, đơn theo chủ sở hữu, nhận đơn đồng thời, giao/hủy, khóa tài khoản.
- Chạy lại từ thư mục dự án (Node.js và Java 21+ cho Firebase Emulator):
  npm --prefix firebase-tests ci
  node firebase-tests/node_modules/firebase-tools/lib/bin/firebase.js emulators:exec --only database --project demo-foodnow "node firebase-tests/sync.test.cjs"
- Rules và mã nguồn đã được kiểm thử cục bộ; cần Publish Rules và kiểm tra đăng
  nhập thật với mật khẩu demo trên project của bạn trước khi nộp.
- Đây là bài demo thanh toán. Tổng tiền được tính ở ứng dụng; chưa có máy chủ
  kiểm giá/tồn kho hoặc cổng thanh toán thật. Không dùng để thu tiền thật.
- Giá trị itemsJson lưu ảnh chụp chi tiết món/topping tại thời điểm đặt và bị
  Rules khóa sửa sau khi tạo đơn; đơn cũ dùng cấu trúc khác không tự chuyển đổi.

6. TỰ NÉN ZIP

Giữ app/src, app/build.gradle, app/google-services.json, app/proguard-rules.pro,
gradle/wrapper (bao gồm JAR), gradlew, gradlew.bat, build.gradle,
settings.gradle, gradle.properties, database.rules.json, firebase.json,
README_FIREBASE.txt và test nếu muốn. Có thể gửi APK riêng kèm ZIP.
Không đưa .gradle, .idea, build, app/build, local.properties, node_modules,
firebase-tests/node_modules, các file *-debug.log vào ZIP.
google-services.json là cấu hình ứng dụng; không đưa khóa quản trị máy chủ vào ZIP.
