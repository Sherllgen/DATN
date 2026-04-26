import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

/**
 * Registers an Expo push token with the backend for the authenticated user.
 * The backend stores the token and uses it to dispatch push notifications later.
 */
export async function registerPushTokenApi(
    deviceToken: string,
    deviceType: "ios" | "android"
) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/notifications/push-tokens`,
        { deviceToken, deviceType }
    );
    return res.data;
}

/**
 * Removes the push token from the backend to stop receiving push notifications on this device.
 */
export async function unregisterPushTokenApi(deviceToken: string) {
    const res = await axiosInstance.delete(
        `${API_BACKEND_URL}/api/v1/notifications/push-tokens/${encodeURIComponent(deviceToken)}`
    );
    return res.data;
}
