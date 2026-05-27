# BÁO CÁO PHÂN TÍCH KIẾN TRÚC VÀ HIỆN TRẠNG DỰ ÁN KH3T SHOP

Tài liệu này phân tích chi tiết cấu trúc hệ thống **KH3T Shop** dựa trên mã nguồn hiện tại của dự án và đối chiếu với các tiêu chí đánh giá báo cáo kiến trúc phần mềm trong yêu cầu của bạn.

---

## I. TỔNG QUAN HỆ TRẠNG DỰ ÁN

Dự án **KH3T Shop** hiện tại đã được cấu trúc lại từ dạng Decoupled (FE/BE tách biệt hoàn toàn) sang kiến trúc **Monolith (Spring Boot & React)** hoặc hỗ trợ chạy song song dạng **Decoupled Containerized** nhờ hệ thống API Gateway (Nginx). 

### 1. Công nghệ sử dụng (Technologies Used)
*   **Backend (Spring Boot 3.x - Java 21)**:
    *   **Spring Security & JWT**: Quản lý xác thực và phân quyền cho người dùng.
    *   **Spring Data JPA**: Giao tiếp cơ sở dữ liệu MySQL/MariaDB.
    *   **Redis**: Caching danh sách sản phẩm phục vụ nghiệp vụ bán hàng và tối ưu hóa context cho Chatbot AI.
    *   **Bucket4j**: Giới hạn tần suất request (Rate Limiting) trên các endpoint nhạy cảm (như `/auth/login`).
    *   **WebClient**: Call APIs bất đồng bộ/đồng bộ tới các bên thứ ba (Google Gemini API, Google Calendar API, SePay Gateway).
    *   **MapStruct**: Tự động ánh xạ dữ liệu giữa Entities và DTOs.
*   **Frontend (React 19 - Vite - TailwindCSS v4)**:
    *   Được tích hợp trực tiếp vào thư mục `static/` của Spring Boot (trong chế độ Monolith) để đóng gói thành một file `.war`/`.jar` duy nhất.
    *   Sử dụng **Axios** để gọi APIs, **Recharts** để vẽ biểu đồ doanh thu và phân tích, **Formik & Yup** để validate form, **Sonner** hiển thị thông báo.
*   **DevOps & Infrastructure**:
    *   **Docker & Docker Compose**: Đóng gói các dịch vụ (MySQL, Redis, Nginx, Backend, Frontend, Jenkins).
    *   **Nginx**: Làm Reverse Proxy và API Gateway phân phối request giữa FE và BE.
    *   **CI/CD**: Đã cấu hình pipeline trên **GitLab CI** (`.gitlab-ci.yml`) và **Jenkins** (`Jenkinsfile`).
    *   **IaC (Infrastructure as Code)**: Có cấu hình **Terraform** cơ bản (`main.tf` sử dụng Docker provider để chạy thử nghiệm Nginx).

---

## II. ĐỐI CHIẾU TIÊU CHÍ ĐÁNH GIÁ (ĐÃ CÓ VS CHƯA CÓ)

Dựa trên hình ảnh yêu cầu của bạn, dưới đây là bảng phân tích chi tiết những gì dự án **đã có** và **chưa có**:

### 1. Slide và Chương trình Demo (Tiêu chí 1)

| Tiêu chí | Trạng thái trong dự án | Chi tiết hiện trạng & Đánh giá |
| :--- | :--- | :--- |
| **Chương trình Demo** | **ĐÃ CÓ (Đầy đủ)** | Hệ thống hoạt động tốt cả Backend và Frontend. Có các tính năng hoàn chỉnh: Giỏ hàng, Đặt hàng, Thanh toán ngân hàng (SePay), Gửi Email thông báo (Async), AI Chatbot (Client + Admin), Dự báo doanh thu. |
| **Trình bày các chức năng** | **CHƯA CÓ TRÊN SLIDE** | Mã nguồn đã có đầy đủ chức năng nhưng bạn **cần đưa vào slide** các chức năng chính: đặt hàng, tích điểm khách hàng, tích hợp thanh toán SePay qua webhook, đồng bộ lịch Google Calendar, phân tích dự báo và Chatbot AI. |
| **Sơ đồ kiến trúc (C4, sơ đồ tổng quát)** | **CHƯA CÓ** | Dự án chưa có file hình ảnh sơ đồ. **Bạn cần vẽ bổ sung** sơ đồ tổng quát (Nginx -> React + Spring Boot -> DB/Redis/Gemini) và sơ đồ **C4 Model** (đặc biệt là Container & Component). *Xem gợi ý vẽ ở mục III*. |
| **Công nghệ sử dụng** | **ĐÃ CÓ** | Đã liệt kê chi tiết trong mã nguồn (Spring Boot, React, Tailwind CSS, Redis, Docker, Nginx, Jenkins, Terraform). |

### 2. Các khối kiến thức kiểm tra (Tiêu chí 2)

| Tiêu chí | Trạng thái trong dự án | Chi tiết hiện trạng & Đánh giá |
| :--- | :--- | :--- |
| **Architecture Style** | **ĐÃ CÓ** | Dự án hỗ trợ 2 style kiến trúc chính:<br>1. **Monolith (Layered)**: React được nhúng vào tài nguyên static của Spring Boot.<br>2. **Decoupled Architecture**: Sử dụng Nginx để chuyển tiếp yêu cầu đến Frontend (port 3000) và Backend (port 8080) chạy trên các container riêng biệt. |
| **Architecture Characteristic** | **ĐÃ CÓ (Một phần)** | Các thuộc tính chất lượng phần mềm được cài đặt:<br>- **Performance & Scalability**: Caching sản phẩm bằng Redis.<br>- **Security**: JWT Authentication, Rate Limiting chống spam/brute-force login.<br>- **Reliability**: Cấu hình health check cho MySQL trong Docker Compose. |
| **DevOps** | **ĐÃ CÓ (Đầy đủ skeleton)** | Có đầy đủ các file cấu hình tiêu chuẩn:<br>- Đóng gói: `Dockerfile` cho cả FE và BE.<br>- Phối hợp: `docker-compose.yml`.<br>- CI/CD: `.gitlab-ci.yml` và `Jenkinsfile`.<br>- IaC: `main.tf` (Terraform cơ bản). |
| **Mức độ áp dụng AI** | **ĐÃ CÓ (Rất tốt)** | Áp dụng AI ở mức độ cao:<br>1. **Generative AI (Gemini 2.5 Flash)**: Chatbot tư vấn bán hàng tự động lọc và gợi ý sản phẩm, tự động phát hiện ý định so sánh sản phẩm (Compare Intent). Trợ lý CEO cung cấp báo cáo doanh số, dự báo real-time.<br>2. **AI/ML Thống kê (Prediction)**: Các thuật toán dự báo doanh thu (ARIMA, Exponential Smoothing, Polynomial Regression, Weighted Moving Average). |

### 3. Các kỹ thuật áp dụng (Tiêu chí 3)

| Kỹ thuật | Trạng thái trong dự án | Chi tiết hiện trạng & Đánh giá |
| :--- | :--- | :--- |
| **Design Pattern** | **ĐÃ CÓ (Đầy đủ)** | - **Strategy Pattern**: Rất rõ ràng trong phần dự báo doanh thu. Interface `ForecastAlgorithm` định nghĩa thuật toán và các lớp kế thừa như `ARIMAForecaster`, `ExponentialSmoothingForecaster`, v.v. là các strategy được chọn động tại runtime.<br>- **Chain of Responsibility**: Thể hiện qua Filter Chain của Spring Security và `RateLimitingFilter`.<br>- **Repository Pattern**: Tách biệt truy xuất dữ liệu thông qua Spring Data JPA.<br>- **Dependency Injection / Singleton**: Cơ chế cơ bản của Spring Boot. |
| **CQRS** | **CHƯA CÓ** | **Không có trong dự án**. Dự án vẫn sử dụng kiến trúc phân tầng truyền thống (Layered Architecture), nơi các hàm Read (đọc) và Write (ghi) dùng chung một Database và chung một luồng Repository/Service. |
| **Event Sourcing** | **CHƯA CÓ** | **Không có trong dự án**. Dự án không lưu trữ trạng thái dưới dạng một chuỗi các sự kiện (Event Store) và không sử dụng Event-driven architecture (như Kafka/RabbitMQ). Mọi thay đổi trạng thái đều được cập nhật trực tiếp (mutate) vào MySQL. |
| **Sync/Async** | **ĐÃ CÓ** | - **Sync**: Toàn bộ các API giao tiếp thông thường giữa Client - Server là đồng bộ (REST API HTTP).<br>- **Async**: Được cài đặt thông qua `@Async` và `@EnableAsync` trong `EmailService.java` giúp gửi email hóa đơn/xác nhận mà không làm nghẽn luồng xử lý chính của người dùng. |

---

## III. HƯỚNG DẪN BỔ SUNG CÁC PHẦN CÒN THIẾU

Để hoàn thiện phần báo cáo dự án đạt điểm tối đa, bạn cần tự chuẩn bị hoặc vẽ thêm các nội dung sau:

### 1. Vẽ sơ đồ kiến trúc C4 Model
Bạn nên đưa sơ đồ này vào Slide. Dưới đây là gợi ý thiết kế kèm theo mã **Mermaid** có thể render trực tiếp hoặc import vào Draw.io:

*   **C1 - System Context (Sơ đồ ngữ cảnh)**:
    *   **Actor**: Khách hàng, Admin/Chủ shop.
    *   **Hệ thống chính**: KH3T Shop System.
    *   **Hệ thống bên thứ ba**: SePay (xác nhận chuyển khoản ngân hàng), Google Gemini API (trí tuệ nhân tạo), Google Calendar (lịch hẹn/sự kiện), SMTP Mail Server (gửi email hóa đơn).

    ```mermaid
    graph TD
        User["Khách hàng và Admin"] -->|Sử dụng hệ thống| System["Hệ thống KH3T Shop (Web và API)"]
        System -->|Đồng bộ lịch hẹn| GoogleCalendar["Google Calendar API"]
        System -->|Tư vấn và Phân tích dữ liệu| Gemini["Google Gemini API"]
        System -->|Gửi Email hóa đơn (Async)| MailServer["Mail Server (SMTP)"]
        SePay["Hệ thống SePay"] -->|Webhook xác nhận chuyển khoản| System
    ```

*   **C2 - Containers (Sơ đồ container)**:
    *   **API Gateway (Nginx)**: Tiếp nhận mọi request từ client qua port 80. Nếu đường dẫn là `/api/*` thì chuyển tiếp sang Spring Boot Backend, ngược lại chuyển sang React Frontend.
    *   **Frontend (React - Vite - Tailwind v4)**: Chạy trên trình duyệt client, render giao diện.
    *   **Backend (Spring Boot - Java 21)**: Cung cấp API RESTful.
    *   **Database (MySQL)**: Lưu trữ thông tin người dùng, sản phẩm, đơn hàng, hóa đơn.
    *   **Cache (Redis)**: Lưu trữ đệm sản phẩm để tăng hiệu suất truy vấn.

    ```mermaid
    graph TB
        subgraph Client ["Trình duyệt Client"]
            ReactApp["Frontend: React App (Vite, TailwindCSS)"]
        end

        subgraph Infrastructure ["Hạ tầng KH3T Shop"]
            Nginx["Reverse Proxy: Nginx Gateway (Port 80)"]
            SpringBoot["Backend: Spring Boot App (Port 8080)"]
            MySQL[("Database: MySQL (Port 3306)")]
            Redis[("Cache: Redis (Port 6379)")]
        end

        subgraph External ["Hệ thống bên ngoài"]
            GeminiAPI["Google Gemini API (AI)"]
            CalendarAPI["Google Calendar API"]
            EmailService["SMTP Mail Server"]
            SePayAPI["SePay Payment API"]
        end

        ReactApp -->|Gửi request qua Nginx| Nginx
        Nginx -->|Đường dẫn /api/*| SpringBoot
        Nginx -->|Đường dẫn tĩnh /| ReactApp

        SpringBoot -->|Query & Persist| MySQL
        SpringBoot -->|Cache/Retrieve Product| Redis
        SpringBoot -->|Tư vấn & Phân tích| GeminiAPI
        SpringBoot -->|Đồng bộ lịch hẹn| CalendarAPI
        SpringBoot -->|Gửi Mail Async| EmailService
        
        SePayAPI -->|Gửi Webhook chuyển khoản| SpringBoot
    ```

*   **C3 - Components (Sơ đồ thành phần trong Spring Boot)**:
    *   **Security & Filter Layer**: `JwtUtil`, `RateLimitingFilter` (Bucket4j).
    *   **Controller Layer**: `ChatController`, `AdminChatController`, `ProductController`, `OrderController`, v.v.
    *   **Service Layer**: `ProductService`, `OrderService`, `GeminiService`, `RevenueForecastService`, v.v.
    *   **Prediction Engine**: `ForecastAlgorithm` interface và các class thuật toán cụ thể.
    *   **Repository Layer**: Các Spring Data JPA interfaces.

    ```mermaid
    graph TB
        subgraph ClientApp ["React App (Frontend)"]
            React["Giao diện người dùng"]
        end

        subgraph SpringBoot ["Spring Boot Backend Container"]
            subgraph FilterSecurity ["Bộ lọc & Bảo mật"]
                Filter["RateLimitingFilter (Bucket4j)"]
                SecConfig["SecurityConfig (JWT)"]
            end

            subgraph Controllers ["Controllers (API Endpoints)"]
                AuthCtrl["AuthenticationController"]
                ProdCtrl["ProductController"]
                OrderCtrl["OrderController"]
                ChatCtrl["ChatController (AI Customer)"]
                AdminChatCtrl["AdminChatController (AI CEO)"]
                ForecastCtrl["RevenueForecastController"]
            end

            subgraph Services ["Services (Logic nghiệp vụ)"]
                AuthSvc["AuthenticationService & JwtService"]
                ProdSvc["ProductService"]
                ProdCache["ProductCacheService (Redis Caching)"]
                OrderSvc["OrderService"]
                GeminiSvc["GeminiService (Google Gemini API)"]
                ForecastSvc["RevenueForecastService"]
            end

            subgraph PredictionEngine ["Prediction Module (Strategy Pattern)"]
                AlgInterface["ForecastAlgorithm (Interface)"]
                Arima["ARIMAForecaster"]
                ExpSmooth["ExponentialSmoothingForecaster"]
                PolyReg["PolynomialRegressionForecaster"]
                Wma["WeightedMovingAverageForecaster"]
            end

            subgraph Repositories ["Repositories (JPA)"]
                ProdRepo["ProductRepository"]
                OrderRepo["OrderRepository"]
                AccRepo["AccountRepository"]
            end
        end

        subgraph DatabaseSystem ["Hệ thống dữ liệu"]
            MySQLDb[("MySQL Database")]
            RedisCache[("Redis Cache")]
        end

        React -->|Gửi request| Filter
        Filter -->|Kiểm tra Rate Limit/JWT| SecConfig
        SecConfig -->|Chuyển tiếp| Controllers

        AuthCtrl --> AuthSvc
        ProdCtrl --> ProdSvc
        OrderCtrl --> OrderSvc
        ChatCtrl --> GeminiSvc
        ChatCtrl --> ProdCache
        AdminChatCtrl --> GeminiSvc
        ForecastCtrl --> ForecastSvc

        ProdSvc --> ProdCache
        ProdCache -->|Đọc/Ghi Cache| RedisCache
        ProdSvc --> ProdRepo
        OrderSvc --> OrderRepo
        AuthSvc --> AccRepo

        ForecastSvc --> AlgInterface
        AlgInterface --> Arima
        AlgInterface --> ExpSmooth
        AlgInterface --> PolyReg
        AlgInterface --> Wma

        ProdRepo --> MySQLDb
        OrderRepo --> MySQLDb
        AccRepo --> MySQLDb
    ```

### 2. Xử lý câu hỏi về CQRS và Event Sourcing
Vì dự án **chưa áp dụng** 2 kỹ thuật này, giảng viên chắc chắn sẽ hỏi. Bạn nên chuẩn bị câu trả lời như sau:
*   **Lý do chưa áp dụng CQRS**: Dự án hiện tại có quy mô vừa và nhỏ (Monolith), lượng truy cập đồng thời chưa quá lớn để cần tách biệt hoàn toàn database đọc (Read DB - thường dùng NoSQL/Elasticsearch) và database ghi (Write DB - RDBMS). Việc áp dụng CQRS ở giai đoạn này sẽ làm tăng độ phức tạp của hệ thống không cần thiết (Over-engineering), gây khó khăn cho việc đồng bộ dữ liệu (Eventual Consistency) và làm tăng chi phí vận hành.
*   **Lý do chưa áp dụng Event Sourcing**: Nghiệp vụ của KH3T Shop chủ yếu xoay quanh các giao dịch CRUD thông thường. Trạng thái hiện tại của đơn hàng (Pending, Completed, Cancelled) được lưu trực tiếp trong DB là đủ đáp ứng nhu cầu kinh doanh. Việc lưu trữ toàn bộ lịch sử biến động dưới dạng chuỗi sự kiện (Event Store) chỉ thực sự cần thiết đối với các hệ thống tài chính phức tạp, kiểm toán khắt khe hoặc cần quay ngược thời gian (Time-travel debugging) để khôi phục trạng thái.

---

## IV. BỘ CÂU HỎI PHẢN BIỆN KIẾN TRÚC GỢI Ý (Q&A PREPARATION)

Dưới đây là lời giải chi tiết cho các câu hỏi ví dụ xuất hiện trong hình ảnh yêu cầu của bạn, được ánh xạ chính xác vào dự án KH3T Shop:

### 1. Chương trình dùng kiến trúc gì? Tại sao lại sử dụng kiến trúc đó? Có hợp lý không?
*   **Trả lời**: 
    *   Chương trình sử dụng kiến trúc **Monolith** (Monolithic Architecture) kết hợp với thiết kế phân tầng **Layered Architecture** (Controller -> Service -> Repository -> Entity) ở phía Backend. Frontend React được đóng gói trực tiếp vào thư mục static tài nguyên của Spring Boot. Bên cạnh đó, dự án có khả năng chuyển đổi linh hoạt sang dạng **Decoupled** bằng cách sử dụng **Nginx** làm Reverse Proxy điều hướng request.
    *   **Tính hợp lý**: Cực kỳ hợp lý ở giai đoạn hiện tại. Với một cửa hàng thời trang quy mô nhỏ đến trung bình như KH3T Shop:
        *   Giúp việc phát triển và kiểm thử diễn ra cực kỳ nhanh chóng.
        *   Dễ dàng deploy (chỉ cần chạy một file WAR/JAR duy nhất hoặc deploy cụm qua docker-compose).
        *   Tránh các lỗi phức tạp liên quan đến kết nối mạng giữa các dịch vụ (network latency) và đồng bộ giao dịch phân tán (distributed transactions) nếu chia nhỏ thành Microservices.

### 2. Tại sao sử dụng Redis cho chức năng đó? Sử dụng như vậy có hợp lý không?
*   **Trả lời**:
    *   Redis được sử dụng làm **Cache Server** để lưu trữ danh sách sản phẩm trong `ProductCacheService`. 
    *   **Tính hợp lý**: Rất hợp lý vì:
        *   *Tần suất đọc cực cao so với tần suất ghi*: Khách hàng lướt xem sản phẩm liên tục, trong khi sản phẩm chỉ được cập nhật/thêm mới bởi admin với tần suất rất thấp. Cache sản phẩm giúp giảm tải hơn 90% truy vấn trực tiếp vào database MySQL.
        *   *Tối ưu hóa AI Chatbot*: Chatbot Gemini cần toàn bộ danh sách sản phẩm (tên, giá bán, mô tả, rating, size còn lại) làm ngữ cảnh (context) để tư vấn khách hàng. Mỗi câu chat của khách đều kích hoạt việc lấy danh sách này. Nếu truy cập DB MySQL mỗi lần khách nhắn tin, DB sẽ nhanh chóng bị quá tải. Redis cung cấp tốc độ phản hồi cực nhanh (< vài mili-giây), giúp bot trả lời ngay lập tức mà không ảnh hưởng tới DB.

### 3. Tại sao sử dụng Rate Limiting (Bucket4j)? Sử dụng như vậy có hợp lý không?
*   **Trả lời**:
    *   Rate Limiting được cấu hình bằng thư viện `Bucket4j` thông qua `RateLimitingFilter.java` nhằm giới hạn mỗi địa chỉ IP chỉ được gửi tối đa **5 request đăng nhập trong vòng 1 phút** lên endpoint `/auth/login`.
    *   **Tính hợp lý**: Hợp lý vì login là endpoint nhạy cảm, dễ bị tấn công Brute-Force (dò mật khẩu) hoặc DDOS (gây cạn kiệt tài nguyên máy chủ). Cấu hình này giúp bảo vệ hệ thống và tài khoản của người dùng an toàn hơn.

### 4. Làm thế nào để tăng hiệu suất (Performance) cho hệ thống này?
Nếu giảng viên hỏi cách tối ưu hóa hiệu năng trong tương lai, bạn nên đưa ra các giải pháp sau:
1.  **Ở tầng Database (MySQL)**: Đánh chỉ mục (Index) trên các cột thường xuyên tìm kiếm hoặc lọc (như `product_name`, `category_id`, `order_date`). Cấu hình Connection Pool (HikariCP) tối ưu hơn.
2.  **Ở tầng Cache (Redis)**: Áp dụng chiến lược Cache Eviction hợp lý, chuyển đổi cache sang Redis Cluster khi lượng dữ liệu và người dùng tăng cao. Cấu hình Cache cho cả thông tin người dùng/session.
3.  **Tối ưu hóa Prompt AI**: Hiện tại Chatbot gửi toàn bộ danh sách sản phẩm lên Gemini. Khi số lượng sản phẩm lên đến hàng ngàn, kích thước prompt sẽ rất lớn, gây tốn chi phí API và tăng thời gian phản hồi (latency). Giải pháp tối ưu là áp dụng kỹ thuật **RAG (Retrieval-Augmented Generation)**: sử dụng Vector Database để chỉ tìm và gửi các sản phẩm liên quan nhất đến câu hỏi của khách hàng vào prompt của Gemini thay vì gửi toàn bộ.
4.  **Sử dụng hàng đợi (Message Queue)**: Chuyển các tác vụ nặng hoặc tốn thời gian (như tích hợp lịch Google Calendar, xử lý webhook SePay, gửi email hàng loạt) sang xử lý bất đồng bộ thông qua các Message Broker như RabbitMQ hoặc Kafka thay vì chỉ dùng `@Async` của Spring (vẫn chạy trên JVM thread pool cục bộ).
