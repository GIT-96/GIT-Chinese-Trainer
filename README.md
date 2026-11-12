# GIT Chinese Trainer AI 🇨🇳

Ứng dụng PWA luyện tiếng Trung cho Samsung Android và máy tính: gõ chữ Hán, dịch nghĩa tiếng Việt, nhập Pinyin, nghe Mandarin bằng Web Speech API, chấm điểm, ôn tập câu sai, tra cứu từ vựng và trợ lý học tập.

## Tính năng
- 3 dạng bài luyện tập, bài ngẫu nhiên 10 câu, chấm điểm 10 điểm/câu.
- Phát âm `zh-CN`, nghe bình thường/chậm (phụ thuộc giọng TTS của thiết bị).
- 50 từ mẫu HSK 1 theo cách phân loại phổ biến; **chưa phải bộ từ vựng HSK 1 đầy đủ**.
- Chấm Pinyin có dấu hoặc số thanh điệu (ví dụ `ni3 hao3`).
- Lưu thống kê, điểm và từ sai qua `localStorage` trên thiết bị.
- Trợ lý cơ bản tra cứu từ HSK 1; tùy chọn backend AI thực.
- PWA có service worker để lưu tệp ứng dụng và từ vựng ngoại tuyến sau lần tải đầu.

## Chạy thử
Không mở `index.html` trực tiếp bằng `file://` vì trình duyệt chặn tải JSON. Dùng máy chủ HTTP:

```bash
python -m http.server 8080
```

Truy cập `http://localhost:8080`.

## Đăng lên GitHub Pages
1. Tạo repository `GIT-Chinese-Trainer` trên tài khoản GitHub.
2. Upload **nội dung** thư mục dự án vào gốc repository, bao gồm `.github/workflows/pages.yml`.
3. Vào **Settings → Pages → Build and deployment → Source: GitHub Actions**.
4. Push lên nhánh `main`, chờ workflow **Deploy GitHub Pages** thành công.
5. Mở `https://<username>.github.io/GIT-Chinese-Trainer/` trên Chrome Samsung, chọn **Add to Home screen / Install app**.

## Kết nối AI thực
GitHub Pages chỉ phục vụ nội dung tĩnh, **không thể bảo vệ API key**. Hãy chạy một backend riêng qua HTTPS, lưu API key bằng biến môi trường. Backend phải chấp nhận POST JSON `{ "message": "...", "level": "HSK1" }` và trả `{ "reply": "..." }`. Cấu hình CORS chỉ cho phép origin trang web của bạn; thêm rate limiting, xác thực nếu công khai, giới hạn độ dài đầu vào và kiểm soát chi phí. Trên tab Trợ lý AI, nhập URL backend. **Không nhập khóa API vào frontend**.

## Giới hạn
- Trợ lý mặc định là tra cứu theo quy tắc, không phải mô hình ngôn ngữ AI.
- Bộ chấm dùng các đáp án cho phép; các cách dịch hợp lệ khác có thể bị đánh dấu sai.
- Âm thanh Web Speech API phụ thuộc giọng đọc có sẵn trên thiết bị, có thể cần kết nối mạng.
- Dữ liệu lưu cục bộ có thể mất khi xóa dữ liệu trình duyệt.
- Chưa hỗ trợ chấm phát âm qua microphone hoặc đồng bộ tài khoản.

## Bảo mật
Không lưu khóa bí mật, token hay thông tin nhạy cảm trong repo, JavaScript frontend hoặc localStorage.
