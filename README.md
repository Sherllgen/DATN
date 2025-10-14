# EV-Go — Hệ thống Quản lý & Tìm kiếm Trạm Sạc Xe Điện

> Đồ án chuyên ngành — Đại học Bách Khoa TP.HCM  
> **Nhóm thực hiện:** EVGo Team  

---

## Giới thiệu

**EV-Go** là nền tảng quản lý trạm sạc xe điện toàn diện, hỗ trợ ba nhóm đối tượng chính:
- **Super Admin:** Quản lý toàn hệ thống, người dùng, doanh thu và khiếu nại.  
- **Chủ trạm (Station Owner):** Quản lý trạm sạc, theo dõi hoạt động, doanh thu và phản hồi người dùng.  
- **Người dùng (User/Guest):** Tìm kiếm, đặt chỗ, thanh toán, sạc xe và đánh giá trạm.

Dự án hướng tới việc **số hóa và tối ưu hóa quy trình vận hành trạm sạc**, đồng thời mang đến **trải nghiệm tiện lợi và minh bạch** cho người dùng xe điện.

---

## Chức năng chính

### Người dùng
- Đăng ký / đăng nhập, cập nhật hồ sơ, phương tiện.  
- Tìm trạm sạc theo GPS hoặc bộ lọc (loại sạc, giá, tiện ích...).  
- Đặt chỗ, thanh toán, theo dõi tiến trình sạc, đánh giá trạm.

### Chủ trạm
- Quản lý thông tin trạm, cổng sạc, giá, trạng thái.  
- Theo dõi lượt sạc, doanh thu, và phản hồi người dùng.

### Super Admin
- Quản lý người dùng, chủ trạm, trạm sạc toàn hệ thống.  
- Giám sát doanh thu, xử lý khiếu nại, thống kê & báo cáo.

---

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|-------------|-----------|
| **Backend** | Java Spring Boot · PostgreSQL · Redis · Docker |
| **Frontend (Admin Web)** | React.js · TailwindCSS |
| **Mobile App (User)** | React Native · Expo |

---
