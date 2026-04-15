# KH3T Shop - Monolith Architecture

Dự án đã được chuyển đổi từ kiến trúc Decoupled (FE/BE tách biệt) sang kiến trúc Monolith thuần túy với Spring Boot và React.

## Cấu trúc dự án

```
kh3tshop-be/
├── src/main/
│   ├── java/fit/iuh/kh3tshopbe/
│   │   ├── configuration/     # Cấu hình Spring Boot
│   │   │   └── WebConfig.java # Cấu hình serve static files
│   │   ├── controller/        # REST API controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entities/         # JPA Entities (Layered Architecture)
│   │   ├── enums/            # Enums
│   │   ├── exception/        # Exception handlers
│   │   ├── mapper/           # MapStruct mappers
│   │   ├── repository/       # JPA Repositories (Layered Architecture)
│   │   ├── service/          # Business Logic Services (Layered Architecture)
│   │   └── Kh3tshopBeApplication.java
│   └── resources/
│       ├── static/kh3tshop-fe/  # React source code
│       └── application.properties
└── pom.xml                    # Maven config với frontend build
```

## Cách chạy

### Development Mode
```bash
# Build và chạy Spring Boot (sẽ tự động build React)
mvn clean spring-boot:run
```

### Production Build
```bash
# Build full application
mvn clean package -DskipTests

# Chạy JAR file
java -jar target/kh3tshop-be-0.0.1-SNAPSHOT.war
```

## Kiến trúc Monolith

- **Backend**: Spring Boot với kiến trúc Layered (Entities → Repository → Service → Controller)
- **Frontend**: React được tích hợp vào Spring Boot static resources
- **Build**: Maven tự động build React và copy files vào Spring Boot

## API Endpoints

- Frontend: `http://localhost:8080/` (React SPA)
- API: `http://localhost:8080/api/*` (Spring Boot REST APIs)

## Cấu hình quan trọng

### WebConfig.java
- Forward tất cả routes không phải API đến `index.html`
- Serve static files từ `classpath:/static/`
- Hỗ trợ SPA routing

### pom.xml
- `frontend-maven-plugin`: Build React app
- `maven-resources-plugin`: Copy React build output vào Spring Boot

## Lưu ý

- Cấu trúc Layered Architecture của backend được giữ nguyên
- Frontend sử dụng relative URLs cho API calls
- Không thay đổi logic code hiện tại</content>
<parameter name="filePath">d:\HK2_2025_2026\software-architecture\Conservation_KH3Tshop\README-MONOLITH.md