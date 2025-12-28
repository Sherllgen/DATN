import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function getListAccounts() {
    const res = await axiosInstance.get(
        `${API_BACKEND_URL}/api/v1/admin/accounts`
    );

    return res.data;
}

export async function getListRegistration() {
    const res = await axiosInstance.get(
        `${API_BACKEND_URL}/api/v1/admin/station-owners`
    );

    return res.data;
}
