# Hướng dẫn sử dụng Authentication với HttpOnly Cookies

## Tổng quan

Hệ thống authentication đã được triển khai với các tính năng:

-   Login với Server Actions
-   Lưu trữ accessToken trong httpOnly cookies (bảo mật cao)
-   Middleware để bảo vệ routes
-   Auto redirect khi chưa đăng nhập
-   Logout functionality

## Cấu trúc files

```
src/
├── app/
│   ├── (auth)/
│   │   └── sign-in-3/
│   │       ├── actions.ts          # Server Actions cho login/logout
│   │       ├── page.tsx            # Login page
│   │       └── components/
│   │           └── login-form-3.tsx # Form component
│   └── api/
│       └── auth/
│           └── logout/
│               └── route.ts        # API route cho logout
├── lib/
│   ├── auth.ts                     # Helper functions để lấy token
│   └── serverAxios.ts              # Axios instance cho server-side
├── middleware.ts                    # Route protection middleware
└── utils/
    └── axiosInstance.ts            # Axios instance cho client-side
```

## Cách sử dụng

### 1. Login

Form đăng nhập tự động xử lý:

-   Gửi email và password đến server
-   Nhận accessToken và lưu vào httpOnly cookie
-   Redirect đến /dashboard sau khi thành công

```tsx
// File: src/app/(auth)/sign-in-3/page.tsx
// Chỉ cần render LoginForm3 component
<LoginForm3 />
```

### 2. Lấy AccessToken trong Server Components/Actions

```typescript
import { getAccessToken, isAuthenticated } from "@/lib/auth";

// Kiểm tra đã đăng nhập
const isLoggedIn = await isAuthenticated();

// Lấy token
const token = await getAccessToken();
```

### 3. Gọi API từ Server Actions

```typescript
import { createServerAxios } from "@/lib/serverAxios";

export async function getUserProfile() {
    "use server";

    const axios = await createServerAxios();

    try {
        const response = await axios.get("/api/v1/users/me");
        return response.data;
    } catch (error) {
        console.error("Error fetching profile:", error);
        throw error;
    }
}
```

### 4. Gọi API từ Client Components

```typescript
import axiosInstance from "@/utils/axiosInstance";

async function fetchData() {
    try {
        // Cookie sẽ tự động được gửi kèm request
        const response = await axiosInstance.get(
            `${process.env.EXPO_PUBLIC_BACKEND_URL}/api/v1/users/me`
        );
        return response.data;
    } catch (error) {
        console.error("Error:", error);
    }
}
```

### 5. Logout

**Cách 1: Sử dụng Server Action**

```typescript
import { logoutAction } from "@/app/(auth)/sign-in-3/actions";

// Trong component
<Button onClick={() => logoutAction()}>Logout</Button>;
```

**Cách 2: Sử dụng API Route**

```typescript
async function handleLogout() {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/sign-in-3";
}
```

## Route Protection

Middleware tự động bảo vệ các routes:

### Protected Routes (yêu cầu đăng nhập)

-   /dashboard
-   /chat
-   /mail
-   /calendar
-   /tasks
-   /users
-   /settings

### Auth Routes (chỉ cho người chưa đăng nhập)

-   /sign-in-3
-   /register

Để thêm route mới vào danh sách bảo vệ, chỉnh sửa `src/middleware.ts`:

```typescript
const protectedRoutes = ["/dashboard", "/your-new-route"];
```

## Cấu hình Backend

Đảm bảo backend của bạn:

1. **Endpoint login**: `POST /api/v1/auth/login`

    ```json
    Request: {
      "email": "user@example.com",
      "password": "password123"
    }

    Response: {
      "accessToken": "eyJhbGc...",
      "refreshToken": "eyJhbGc..." (optional)
    }
    ```

2. **Cho phép CORS với credentials**:

    ```javascript
    app.use(
        cors({
            origin: "http://localhost:3000",
            credentials: true,
        })
    );
    ```

3. **Chấp nhận Authorization header**:
    ```javascript
    app.use((req, res, next) => {
        const token = req.headers.authorization?.split(" ")[1];
        // Verify token
    });
    ```

## Security Features

-   ✅ **HttpOnly Cookies**: Token không thể truy cập từ JavaScript (chống XSS)
-   ✅ **Secure Flag**: Cookie chỉ gửi qua HTTPS trong production
-   ✅ **SameSite**: Bảo vệ chống CSRF attacks
-   ✅ **Auto Redirect**: Người dùng chưa đăng nhập tự động redirect
-   ✅ **Token trong Cookie**: Tự động gửi với mọi request

## Troubleshooting

### Cookie không được set

-   Kiểm tra `withCredentials: true` trong axios config
-   Đảm bảo backend có CORS với `credentials: true`

### Redirect loop

-   Kiểm tra middleware config
-   Đảm bảo `/sign-in-3` không nằm trong `protectedRoutes`

### Token không được gửi

-   Kiểm tra cookie có được set trong DevTools > Application > Cookies
-   Đảm bảo request được gửi đến cùng domain (hoặc backend cho phép CORS)

## Environment Variables

Đảm bảo có biến môi trường sau trong `.env.local`:

```env
EXPO_PUBLIC_BACKEND_URL=http://localhost:8080
NODE_ENV=development
```
