# Hướng dẫn chạy hệ thống e-commerce

## 1. Yêu cầu hệ thống

- Máy chủ/VPS hoặc máy tính cá nhân cài đặt Docker và Docker Compose.
- Có kết nối internet ổn định.
- Tài khoản Cloudflare (nếu sử dụng Cloudflare Tunnel để expose dịch vụ ra internet).

## 2. Chuẩn bị môi trường

- Cài đặt Docker: https://docs.docker.com/get-docker/
- Cài đặt Docker Compose: https://docs.docker.com/compose/install/
- Đăng ký tài khoản Cloudflare và tạo Tunnel (nếu cần): https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/

## 3. Cấu trúc thư mục dự án

- Các dịch vụ: `user/`, `product/`, `transaction/`, `wallet/`, `notify/`
- File cấu hình: `docker-compose.yml` (ở thư mục gốc)
- Các file dữ liệu mẫu, script, tài liệu hướng dẫn

## 4. Khởi động hệ thống bằng Docker Compose

1. Mở terminal tại thư mục gốc dự án.
2. Chạy lệnh sau để build và khởi động toàn bộ stack:

   ```powershell
   docker compose up -d
   ```

   - Các service sẽ tự động build (nếu chưa có image) và khởi động: backend, database (MySQL, MongoDB), cache (Redis), Kafka, Kafka UI.
   - Kiểm tra trạng thái các container:

   ```powershell
   docker compose ps
   ```
3. Truy cập các dịch vụ qua port đã expose (xem trong `docker-compose.yml`).

   - Ví dụ: http://localhost:8080 (backend), http://localhost:8081 (Kafka UI), ...

## 5. Sử dụng Cloudflare Tunnel để expose dịch vụ ra internet

- Cài đặt Cloudflare Tunnel theo hướng dẫn: https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/
- Tạo tunnel và cấu hình domain/subdomain trỏ về dịch vụ cần expose (ví dụ: backend, Kafka UI).
- Đảm bảo SSL/TLS được bật để bảo vệ kết nối.

## 6. Lưu ý bảo mật

- Chỉ expose các port cần thiết ra internet (ưu tiên dùng Cloudflare Tunnel).
- Đổi mật khẩu mặc định cho database, Redis, Kafka.
- Kiểm tra log truy cập, cảnh báo bất thường.
- Thường xuyên backup dữ liệu và kiểm tra khả năng phục hồi.

## 7. Troubleshooting

- Nếu service không khởi động, kiểm tra log bằng lệnh:
  ```powershell
  docker compose logs <service-name>
  ```
- Kiểm tra cấu hình môi trường, biến môi trường trong `docker-compose.yml`.
- Đảm bảo các port không bị xung đột hoặc bị firewall chặn.
- Kiểm tra trạng thái Cloudflare Tunnel nếu không truy cập được từ internet.

## 8. Tắt hệ thống

- Để dừng toàn bộ stack:
  ```powershell
  docker compose down
  ```

---

## 9. Hướng dẫn chạy file jar trực tiếp

Nếu không sử dụng Docker, bạn có thể chạy từng service bằng file jar như sau:

### 1. Chuẩn bị

- Đảm bảo đã cài đặt Java (JDK 21).
- Di chuyển vào thư mục chứa file jar của service (ví dụ: `user/target/user-0.0.1-SNAPSHOT.jar`).

### 2. Chạy service

Chạy lệnh sau trong terminal:

```powershell
java -jar user-0.0.1-SNAPSHOT.jar
```

Hoặc với các service khác:

```powershell
java -jar product-0.0.1-SNAPSHOT.jar
java -jar transaction-0.0.1-SNAPSHOT.jar
java -jar wallet-0.0.1-SNAPSHOT.jar
```

### 3. Cấu hình môi trường

- Có thể truyền biến môi trường hoặc file cấu hình khi chạy:

```powershell
java -jar user-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
```

- Hoặc sử dụng biến môi trường:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
java -jar user-0.0.1-SNAPSHOT.jar
```

### 4. Lưu ý

- Đảm bảo các service backend kết nối đúng tới database, Kafka, Redis, MongoDB (có thể chỉnh sửa trong file cấu hình hoặc biến môi trường).
- Nếu chạy nhiều service, cần kiểm tra port không bị trùng lặp.
- Có thể chạy nhiều service song song ở các terminal khác nhau.

---

Nếu cần hỗ trợ thêm về chạy file jar, vui lòng liên hệ admin hoặc xem tài liệu chi tiết trong từng thư mục dịch vụ.
