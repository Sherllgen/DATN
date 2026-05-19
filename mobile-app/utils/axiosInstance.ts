import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import * as SecureStore from "expo-secure-store";
import { router } from "expo-router";
import { useAuthStore, SECURE_KEY_ACCESS_TOKEN, SECURE_KEY_REFRESH_TOKEN } from "@/contexts/auth.store";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

const axiosInstance = axios.create({
    withCredentials: true,
});

// REQUEST INTERCEPTOR
// Attach the current in-memory access token to every outgoing request.
axiosInstance.interceptors.request.use(
    async (config) => {
        const accessToken = useAuthStore.getState().accessToken;
        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        console.log("AXIOS REQUEST:", {
            method: config.method?.toUpperCase(),
            url: config.baseURL ? `${config.baseURL}${config.url}` : config.url,
            params: config.params,
        });

        return config;
    },
    (error) => {
        console.log("REQUEST ERROR:", error);
        return Promise.reject(error);
    }
);

// 401 AUTO-REFRESH STATE
// Ensures only ONE refresh call is in-flight at a time. Every other request that
// receives a 401 while a refresh is pending queues itself and resolves/rejects
// together once the refresh completes.
let isRefreshing = false;
type QueueEntry = { resolve: (token: string) => void; reject: (err: unknown) => void };
let failedQueue: QueueEntry[] = [];

function processQueue(error: unknown, token: string | null) {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else {
            resolve(token!);
        }
    });
    failedQueue = [];
}

// RESPONSE INTERCEPTOR
axiosInstance.interceptors.response.use(
    (response) => {
        console.log("AXIOS RESPONSE:", {
            status: response.status,
            url: response.config.url,
            data: response.data,
        });
        return response;
    },
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // Skip cancelled requests immediately
        if (error.code === "ERR_CANCELED") {
            return Promise.reject(error);
        }

        // 401 Unauthorized: attempt token refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
            // If a refresh is already in-flight, queue this request
            if (isRefreshing) {
                return new Promise<string>((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then((newToken) => {
                    originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    return axiosInstance(originalRequest);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // Read refresh token from secure storage
                const storedRefreshToken = await SecureStore.getItemAsync(SECURE_KEY_REFRESH_TOKEN);

                if (!storedRefreshToken) {
                    throw new Error("No refresh token stored");
                }

                const refreshRes = await axios.post(
                    `${API_BACKEND_URL}/api/v1/auth/refresh`,
                    { refreshToken: storedRefreshToken },
                    { withCredentials: true }
                );

                const { accessToken: newAccessToken, refreshToken: newRefreshToken } =
                    refreshRes.data.data;

                // Persist new tokens atomically
                await useAuthStore.getState().saveTokens(newAccessToken, newRefreshToken);

                // Drain the queue with the new token
                processQueue(null, newAccessToken);

                // Retry the original request with the new access token
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                // Refresh failed — force logout and redirect to login
                processQueue(refreshError, null);
                await useAuthStore.getState().logout();
                router.replace("/auth/login");
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        // ── All other errors: log and reject ────────────────────────────────
        if (axios.isAxiosError(error)) {
            console.log("AXIOS ERROR DETAILS:", {
                message: error.message,
                code: error.code,
                config: {
                    method: error.config?.method,
                    url: error.config?.baseURL
                        ? `${error.config.baseURL}${error.config.url}`
                        : error.config?.url,
                },
                response: error.response
                    ? {
                          status: error.response.status,
                          statusText: error.response.statusText,
                          data: error.response.data,
                      }
                    : "No response received (Network error)",
            });
        } else {
            console.log("UNKNOWN ERROR:", error);
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;
