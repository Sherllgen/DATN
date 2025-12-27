import { cookies } from "next/headers";

/**
 * Lấy accessToken từ httpOnly cookie (dùng trong Server Components/Actions)
 */
export async function getAccessToken(): Promise<string | undefined> {
    const cookieStore = await cookies();
    return cookieStore.get("accessToken")?.value;
}

/**
 * Lấy refreshToken từ httpOnly cookie (dùng trong Server Components/Actions)
 */
export async function getRefreshToken(): Promise<string | undefined> {
    const cookieStore = await cookies();
    return cookieStore.get("refreshToken")?.value;
}

/**
 * Kiểm tra xem user đã đăng nhập hay chưa
 */
export async function isAuthenticated(): Promise<boolean> {
    const token = await getAccessToken();
    return !!token;
}
