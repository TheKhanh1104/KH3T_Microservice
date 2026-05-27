# NỘI DUNG SLIDE THUYẾT TRÌNH KH3T SHOP (DÁN VÀO CANVA)

Tài liệu này chứa nội dung slide đã được tối ưu hóa cho việc copy-paste trực tiếp vào mẫu slide Canva của bạn.

---

## SLIDE 1: TRANG TIÊU ĐỀ (TITLE)

*   **Tiêu đề chính**: BÁO CÁO KIẾN TRÚC PHẦN MỀM
*   **Tiêu đề phụ**: Hệ Thống Quản Lý và Bán Hàng Thời Trang – KH3T Shop
*   **Thông tin nhóm**: [Điền tên các thành viên của bạn tại đây]
*   **Giảng viên hướng dẫn**: [Điền tên GVHD tại đây]

---

## SLIDE 2: TỔNG QUAN HỆ THỐNG (OVERVIEW)

*   **Tiêu đề**: Tổng Quan Hệ Thống KH3T Shop
*   **Nội dung**:
    *   Nền tảng thương mại điện tử thời trang hiện đại cho Khách hàng và Admin.
    *   **Mục tiêu**: Xây dựng kiến trúc ổn định, bảo mật cao, phản hồi nhanh và tích hợp công nghệ AI.
    *   **Tính năng chính**: Bán hàng trực tuyến, thanh toán tự động, chat thông minh và dự báo kinh doanh.

---

## SLIDE 3: CHỨC NĂNG CỐT LÕI (FEATURES)

*   **Tiêu đề**: Các Chức Năng Đã Triển Khai
*   **Nội dung**:
    *   **Thanh toán SePay**: Tự động nhận diện giao dịch chuyển khoản qua Webhook.
    *   **Trợ lý ảo Gemini (AI)**: Chat tư vấn sản phẩm cho khách hàng và phân tích dữ liệu cho Admin.
    *   **Dự báo doanh thu**: Sử dụng các mô hình dự báo toán học từ dữ liệu lịch sử bán hàng.
    *   **Đồng bộ Lịch & Mail**: Tự động kết nối Google Calendar và gửi email hóa đơn (Async).

---

## SLIDE 4: SƠ ĐỒ C1 - CONTEXT DIAGRAM

*   **Tiêu đề**: Sơ Đồ Kiến Trúc C1 – Ngữ Cảnh Hệ Thống
*   **Mô tả**:
    *   Làm rõ ranh giới hệ thống KH3T Shop và luồng kết nối với các bên thứ ba.
    *   **Tác nhân**: Khách hàng, Admin/Chủ shop.
    *   **Liên kết**: Google Gemini API, Google Calendar API, SMTP Mail Server, SePay Webhook.

*(Dán hình ảnh Sơ đồ C1 được tải từ Mermaid Live Editor vào trang này)*

---

## SLIDE 5: SƠ ĐỒ C2 - CONTAINER DIAGRAM

*   **Tiêu đề**: Sơ Đồ Kiến Trúc C2 – Container & Công Nghệ
*   **Mô tả**:
    *   **Frontend**: React App (Vite, TailwindCSS) chạy trên trình duyệt Client.
    *   **Gateway**: Nginx Proxy (Port 80) tiếp nhận và phân phối lưu lượng request.
    *   **Backend**: Spring Boot (Java 21, Port 8080) xử lý toàn bộ logic nghiệp vụ.
    *   **Cache Server**: Redis (Port 6379) lưu đệm sản phẩm tối ưu tốc độ đọc.
    *   **Database**: MySQL (Port 3306) lưu trữ dữ liệu quan hệ cốt lõi.

*(Dán hình ảnh Sơ đồ C2 được tải từ Mermaid Live Editor vào trang này)*

---

## SLIDE 6: SƠ ĐỒ C3 - COMPONENT DIAGRAM

*   **Tiêu đề**: Sơ Đồ Kiến Trúc C3 – Thành Phần Spring Boot
*   **Mô tả**:
    *   **Tầng Bảo mật/Lọc**: `RateLimitingFilter` (Bucket4j) và `SecurityConfig` (JWT).
    *   **Tầng Điều hướng (Controllers)**: ChatController, AdminChatController, ProductController...
    *   **Tầng Nghiệp vụ (Services)**: GeminiService, ProductCacheService, RevenueForecastService...
    *   **Module Dự báo (Strategy Pattern)**: Tự động chạy ARIMA, Exponential Smoothing...
    *   **Tầng Dữ liệu (Repositories)**: Các Spring Data JPA interfaces kết nối MySQL.

*(Dán hình ảnh Sơ đồ C3 được tải từ Mermaid Live Editor vào trang này)*

---

## SLIDE 7: TRÍ TUỆ NHÂN TẠO & GIẢI THUẬT (AI & PREDICTION)

*   **Tiêu đề**: Tích Hợp AI & Thuật Toán Dự Báo
*   **Nội dung**:
    *   **Google Gemini AI**: Đọc real-time dữ liệu sản phẩm, tồn kho và lịch sử trò chuyện để tư vấn cá nhân hóa (khách hàng) và báo cáo tài chính nhanh (Admin).
    *   **Strategy Pattern cho Dự báo**: Cho phép hoán đổi linh hoạt thuật toán dự báo doanh thu tại runtime:
        *   ARIMA & Hồi quy đa thức (Polynomial Regression).
        *   San phẳng mũ (Exponential Smoothing) & Trung bình trượt (WMA).

---

## SLIDE 8: HIỆU NĂNG & BẢO MẬT (PERFORMANCE)

*   **Tiêu đề**: Tối Ưu Hiệu Năng & Bảo Mật Hệ Thống
*   **Nội dung**:
    *   **Redis Caching**: Giảm tải hơn 90% truy cập trực tiếp MySQL, tăng tốc độ cung cấp ngữ cảnh cho AI Chatbot.
    *   **Xử lý bất đồng bộ (@Async)**: Tách luồng gửi email hóa đơn và cập nhật lịch hẹn không làm gián đoạn luồng người dùng.
    *   **Rate Limiting (Bucket4j)**: Giới hạn tối đa 5 requests đăng nhập/phút cho mỗi IP nhằm chống Brute-force.

---

## SLIDE 9: QUY TRÌNH DEVOPS & CI/CD

*   **Tiêu đề**: Quy Trình Vận Hành Tự Động Hóa DevOps
*   **Nội dung**:
    *   **Containerization**: Đóng gói đồng bộ các service qua Dockerfile và docker-compose.
    *   **CI/CD Pipeline**:
        *   *GitLab CI*: Tự động chạy test và build source.
        *   *Jenkins Pipeline*: Tự động build backend/frontend, đóng gói docker images và deploy.
    *   **IaC (Infrastructure as Code)**: Dùng Terraform để khởi tạo hạ tầng Nginx gateway nhanh chóng.

---

## SLIDE 10: PHẢN BIỆN KIẾN TRÚC (Q&A)

*   **Tiêu đề**: Câu Hỏi Phản Biện Kiến Trúc Cốt Lõi
*   **Nội dung**:
    *   **Tại sao dùng Monolith?** Phù hợp quy mô KH3T Shop, dễ phát triển, deploy đơn giản, tránh network latency của microservices.
    *   **Tại sao dùng Redis?** Tần suất đọc sản phẩm rất cao, cần dữ liệu tức thời phục vụ prompt chatbot AI.
    *   **Tại sao chưa dùng CQRS/Event Sourcing?** Tăng độ phức tạp (over-engineering) và chi phí vận hành không cần thiết ở giai đoạn hiện tại.
