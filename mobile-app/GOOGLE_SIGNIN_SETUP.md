# Hướng dẫn cấu hình Google Sign In

## Bước 1: Cài đặt dependencies

```bash
npm install expo-auth-session
```

Gói `expo-web-browser` đã được cài đặt sẵn.

## Bước 2: Lấy Google Client ID

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Vào **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth client ID**
5. Chọn application type:
    - **Web application** cho development/testing
    - **Android** và **iOS** cho production

### Cấu hình cho Web (Development/Testing)

- **Application type**: Web application
- **Name**: EVGO Mobile App (Web)
- **Authorized redirect URIs**:
    - Thêm: `https://auth.expo.io/@your-expo-username/mobile-app`
    - Hoặc dùng: `exp://localhost:8081` cho local development

### Cấu hình cho Android (Production)

- **Application type**: Android
- **Name**: EVGO Mobile App (Android)
- **Package name**: `com.trunganh2004.mobileapp` (lấy từ app.json)
- **SHA-1 certificate fingerprint**:
    - Lấy từ debug keystore (development):
        ```bash
        cd android/app
        keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
        ```
    - Lấy từ release keystore (production):
        ```bash
        keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
        ```

### Cấu hình cho iOS (Production)

- **Application type**: iOS
- **Name**: EVGO Mobile App (iOS)
- **Bundle ID**: Lấy từ app.json/iOS config

## Bước 3: Cấu hình Environment Variables

Tạo file `.env` từ `.env.example` và thêm:

```env
EXPO_PUBLIC_MAP_API_KEY=your_map_api_key
EXPO_PUBLIC_BACKEND_URL=your_backend_url
EXPO_PUBLIC_GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
```

**Lưu ý**:

- Sử dụng **Web Client ID** cho development với Expo Go
- Sử dụng **Android/iOS Client ID** khi build native app

## Bước 4: Cấu hình Backend

Đảm bảo backend API endpoint `/api/v1/auth/google` đã được cài đặt và có thể:

- Nhận `idToken` từ Google
- Verify idToken với Google
- Tạo hoặc tìm user trong database
- Trả về `accessToken` và thông tin `user`

## Bước 5: Testing

### Development với Expo Go:

```bash
npm start
```

Quét QR code và test Google Sign In.

### Build và test trên thiết bị thật:

```bash
# Android
eas build --platform android --profile preview

# iOS
eas build --platform ios --profile preview
```

## Troubleshooting

### Lỗi "redirect_uri_mismatch"

- Kiểm tra redirect URI trong Google Console phải khớp với URI được generate bởi `AuthSession.makeRedirectUri()`
- In ra console để xem redirect URI:
    ```typescript
    console.log(
        AuthSession.makeRedirectUri({ scheme: "com.evgo.mobile", path: "auth" })
    );
    ```

### Lỗi "invalid_client"

- Kiểm tra Google Client ID đã đúng
- Đảm bảo sử dụng đúng Client ID cho platform (Web/Android/iOS)

### Không nhận được callback

- Kiểm tra scheme trong app.json: `"scheme": "com.evgo.mobile"`
- Kiểm tra `WebBrowser.maybeCompleteAuthSession()` đã được gọi

## Deep Linking Configuration (Optional)

Nếu cần custom deep linking, cập nhật [app.json](app.json):

```json
{
    "expo": {
        "scheme": "com.evgo.mobile",
        "ios": {
            "bundleIdentifier": "com.evgo.mobile"
        },
        "android": {
            "package": "com.trunganh2004.mobileapp"
        }
    }
}
```

## Tài liệu tham khảo

- [Expo AuthSession Documentation](https://docs.expo.dev/guides/authentication/)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Expo Deep Linking](https://docs.expo.dev/guides/deep-linking/)
