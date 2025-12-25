import axios, { AxiosError } from "axios";

type LogLevel = "INFO" | "WARN" | "ERROR";

interface LogOptions {
    context?: string;
    level?: LogLevel;
    extra?: Record<string, any>;
}

const isDev = __DEV__;

export const logAxiosError = (error: unknown, options: LogOptions = {}) => {
    if (!isDev) return;

    const { context, level = "ERROR", extra } = options;

    if (!axios.isAxiosError(error)) {
        console.error(`[${level}]${context ? ` (${context})` : ""}`, error);
        return;
    }

    const axiosError = error as AxiosError;

    const logPayload = {
        type: "AXIOS_ERROR",
        context,
        message: axiosError.message,
        method: axiosError.config?.method?.toUpperCase(),
        url: axiosError.config?.url,
        status: axiosError.response?.status,
        statusText: axiosError.response?.statusText,
        requestData: axiosError.config?.data,
        responseData: axiosError.response?.data,
        headers: axiosError.response?.headers,
        extra,
    };

    console.groupCollapsed(
        `%c[${level}] Axios Error${context ? ` (${context})` : ""}`,
        "color: red; font-weight: bold;"
    );
    console.error(logPayload);
    console.groupEnd();
};
