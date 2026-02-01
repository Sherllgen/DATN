import axios from "axios";
import { getAccessToken } from "@/lib/auth";

/**
 * Axios instance for server-side requests (Server Actions, API Routes)
 * Tự động thêm accessToken từ httpOnly cookie vào header
 */
export async function createServerAxios() {
    const token = await getAccessToken();

    const instance = axios.create({
        baseURL: process.env.NEXT_PUBLIC_BACKEND_URL,
        headers: {
            "Content-Type": "application/json",
            ...(token && { Authorization: `Bearer ${token}` }),
        },
    });

    // Request interceptor
    instance.interceptors.request.use(
        (config) => {
            console.log("➡️ SERVER AXIOS REQUEST:", {
                method: config.method?.toUpperCase(),
                url: config.url,
                headers: config.headers,
            });
            return config;
        },
        (error) => {
            console.error("❌ SERVER REQUEST ERROR:", error);
            return Promise.reject(error);
        }
    );

    // Response interceptor
    instance.interceptors.response.use(
        (response) => {
            console.log("✅ SERVER AXIOS RESPONSE:", {
                status: response.status,
                url: response.config.url,
            });
            return response;
        },
        (error) => {
            if (axios.isAxiosError(error)) {
                console.error("🔥 SERVER AXIOS ERROR:", {
                    message: error.message,
                    status: error.response?.status,
                    data: error.response?.data,
                });
            }
            return Promise.reject(error);
        }
    );

    return instance;
}
