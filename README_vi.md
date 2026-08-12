# FIDS Android App

FIDS (Fitness & Inner Development System) là ứng dụng Android về sức khỏe tinh thần và phát triển bản thân, được xây dựng bằng Kotlin và Jetpack Compose. Ứng dụng cung cấp các bài khảo sát tâm lý, tư vấn bằng AI, bài tập thở, tính năng cộng đồng và lộ trình phát triển bản thân.

## Tính năng

- **Đăng nhập / Đăng ký** – Xác thực người dùng qua Supabase Auth
- **Khảo sát & Trắc nghiệm tâm lý** – Làm bài và nhận kết quả ngay lập tức
- **Chat tư vấn AI** – Trò chuyện với AI tư vấn viên sử dụng Google Gemini (qua REST API)
- **Lịch sử kết quả** – Xem lại các bài khảo sát và trắc nghiệm đã làm
- **Bài tập thở** – Hướng dẫn thở sâu giúp giảm căng thẳng
- **Màn hình SOS** – Truy cập nhanh các hỗ trợ khẩn cấp
- **Lộ trình rèn luyện** – 4 dự án rèn luyện (Cải thiện Lớp học, Kết nối Gia đình, Cai nghiện Điện thoại, An toàn & Cộng đồng)
- **Wiki FIDS** – Kho kiến thức về sức khỏe tinh thần
- **Trung tâm kỹ năng** – Chi tiết và hướng dẫn các kỹ năng sống
- **Cộng đồng** – Kết nối và chia sẻ với cộng đồng
- **Hồ sơ cá nhân** – Quản lý thông tin tài khoản

## Công nghệ sử dụng

| Thành phần | Công nghệ                                          |
|------------|----------------------------------------------------|
| Ngôn ngữ   | Kotlin                                             |
| Giao diện  | Jetpack Compose, Material 3, Navigation Compose    |
| Backend    | Supabase (Auth, PostgREST, Storage)                |
| Chat AI    | Google Gemini API qua Ktor Client                  |
| Mạng       | Ktor Client, kotlinx.serialization                 |
| Lưu trữ    | DataStore Preferences                              |
| Ảnh        | Coil                                               |

## Yêu cầu

- Android Studio (bản ổn định mới nhất)
- JDK 17
- Android SDK 35 (compileSdk), minSdk 24

## Hướng dẫn cài đặt

1. Clone repository:

   ```bash
   git clone https://github.com/HH1112HH/FIDS_ANDROID_APP.git
   ```

2. Mở dự án bằng Android Studio.
3. Sao chép `local.properties.example` thành `local.properties` và điền key của bạn:
   - **Supabase**: tạo dự án trên [Supabase](https://supabase.com) và dán URL cùng anon/publishable key.
   - **Gemini**: lấy API key từ [Google AI Studio](https://aistudio.google.com/).
   - Key được tiêm vào lúc build qua `BuildConfig` và không bao giờ lưu trong repository.
4. Chạy ứng dụng trên trình giả lập hoặc máy thật.

## Cấu trúc dự án

```
app/src/main/java/com/example/fidsapp/
├── MainActivity.kt            # Điểm khởi đầu & điều hướng
├── LoginScreen.kt             # Đăng nhập / Đăng ký
├── HomeScreen.kt              # Màn hình chính
├── SurveyScreen.kt            # Khảo sát & trắc nghiệm
├── ResultScreen.kt            # Kết quả khảo sát / trắc nghiệm
├── HistoryScreen.kt           # Lịch sử kết quả
├── ChatScreen.kt              # Giao diện chat AI
├── ChatViewModel.kt           # Tích hợp Gemini API
├── BreathingScreen.kt         # Bài tập thở
├── SOSScreen.kt               # Hỗ trợ khẩn cấp
├── RoadmapScreen.kt           # Lộ trình rèn luyện
├── ProjectDetailScreen.kt     # Chi tiết nhiệm vụ dự án
├── WikiFidsScreen.kt          # Kiến thức sức khỏe tinh thần
├── SkillDetailScreen.kt       # Chi tiết kỹ năng sống
├── CommunityScreen.kt         # Tính năng cộng đồng
├── ProfileScreen.kt           # Hồ sơ người dùng
├── SupabaseClient.kt          # Cấu hình Supabase
└── ui/theme/                  # Chủ đề Compose
```

## Lưu ý bảo mật

Tất cả thông tin nhạy cảm (Supabase URL/key, Gemini API key) được giữ trong `local.properties` — file đã bị loại khỏi quản lý phiên bản và được tiêm vào lúc build qua `BuildConfig`. Tuyệt đối không commit key thật — nếu key từng bị công khai, hãy tạo lại key mới trong Supabase và Google AI Studio.

> **Quan trọng:** các commit cũ trong lịch sử repository này có thể vẫn chứa key bị lộ. Nếu bạn clone dự án này và dùng chung tài khoản Supabase/Gemini, hãy tạo lại các key đó.

## Giấy phép

Dự án phục vụ mục đích học tập. Chưa khai báo giấy phép.
