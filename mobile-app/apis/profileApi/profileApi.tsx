import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export async function getProfile() {
    const res = await axiosInstance.get(`${API_BACKEND_URL}/api/v1/users/me`);

    return res.data;
}
