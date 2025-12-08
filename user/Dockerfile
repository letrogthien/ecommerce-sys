# Sử dụng base image JDK gọn nhẹ
FROM openjdk:21-jdk-slim

# Thư mục làm việc trong container
WORKDIR /app

# Copy file JAR đã build vào container
COPY target/user-0.0.1-SNAPSHOT.jar app.jar

# Copy PEM keys vào container (chỉ dùng nội bộ)
COPY private-access.pem /app/private-access.pem
COPY public-access.pem /app/public-access.pem

# Mở cổng ứng dụng (tuỳ config Spring Boot, mặc định 8080)
EXPOSE 8081

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
