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
        `${API_BACKEND_URL}/api/v1/admin/station-owner`
    );

    return res.data;
}

export async function underReviewRegistrationApi(profileId: number) {
    const res = await axiosInstance.put(
        `${API_BACKEND_URL}/api/v1/admin/station-owner/${profileId}`
    );
    return res.data;
}

export async function approveRegistrationApi(profileId: number) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/admin/station-owner/${profileId}/approve`
    );
    return res.data;
}

export async function rejectRegistrationApi(profileId: number, reason: string) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/admin/station-owner/${profileId}/reject`,
        { reason }
    );
    return res.data;
}

export async function lockAccountApi(userId: number) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/admin/accounts/${userId}/lock`
    );
    return res.data;
}
export async function unlockAccountApi(userId: number) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/admin/accounts/${userId}/unlock`
    );
    return res.data;
}

export async function deleteAccountApi(userId: number) {
    const res = await axiosInstance.delete(
        `${API_BACKEND_URL}/api/v1/admin/accounts/${userId}`
    );
    return res.data;
}
