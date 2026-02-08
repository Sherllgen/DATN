import { useAuthStore } from "@/contexts/auth.store";
import axios from "axios";

const axiosInstance = axios.create({
    withCredentials: true,
});

// REQUEST INTERCEPTOR — gắn token & log yêu cầu
axiosInstance.interceptors.request.use(
    async (config) => {
        const accessToken = useAuthStore.getState().accessToken;

        if (accessToken) {
            console.log("accessToken: ", accessToken);

            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        // Log request
        console.log("AXIOS REQUEST:", {
            method: config.method?.toUpperCase(),
            url: config.baseURL ? `${config.baseURL}${config.url}` : config.url,
            headers: config.headers,
            params: config.params,
            data: config.data,
        });

        return config;
    },
    (error) => {
        console.error("REQUEST ERROR:", error);
        return Promise.reject(error);
    }
);

// RESPONSE INTERCEPTOR — log lỗi chi tiết
axiosInstance.interceptors.response.use(
    (response) => {
        console.log("AXIOS RESPONSE:", {
            status: response.status,
            url: response.config.url,
            data: response.data,
        });
        return response;
    },
    (error) => {
        if (axios.isAxiosError(error)) {
            console.error("AXIOS ERROR DETAILS:", {
                message: error.message,
                code: error.code,
                config: {
                    method: error.config?.method,
                    url: error.config?.baseURL
                        ? `${error.config.baseURL}${error.config.url}`
                        : error.config?.url,
                    headers: error.config?.headers,
                    data: error.config?.data,
                },
                response: error.response
                    ? {
                        status: error.response.status,
                        statusText: error.response.statusText,
                        data: error.response.data, // JSON error từ backend
                        headers: error.response.headers,
                    }
                    : "No response received (Network error)",
                request: !!error.request,
            });
        } else {
            console.error("UNKNOWN ERROR:", error);
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;
