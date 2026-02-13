import axiosInstance from "@/utils/axiosInstance";

// ==================== Account Management ====================

export interface AccountFilterParams {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
    status?: string;
    role?: string;
    search?: string;
}

export async function getListAccounts(params?: AccountFilterParams) {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append("page", String(params.page));
    if (params?.size !== undefined) queryParams.append("size", String(params.size));
    if (params?.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params?.sortDir) queryParams.append("sortDir", params.sortDir);
    if (params?.status) queryParams.append("status", params.status);
    if (params?.role) queryParams.append("role", params.role);
    if (params?.search) queryParams.append("search", params.search);

    const res = await axiosInstance.get(`/api/admin/accounts?${queryParams.toString()}`);
    return res.data;
}

export async function getAccountById(userId: number) {
    const res = await axiosInstance.get(`/api/admin/accounts/${userId}`);
    return res.data;
}

export async function lockAccountApi(userId: number) {
    const res = await axiosInstance.post(`/api/admin/accounts/${userId}/lock`);
    return res.data;
}

export async function unlockAccountApi(userId: number) {
    const res = await axiosInstance.post(`/api/admin/accounts/${userId}/unlock`);
    return res.data;
}

export async function deleteAccountApi(userId: number) {
    const res = await axiosInstance.delete(`/api/admin/accounts/${userId}`);
    return res.data;
}

// ==================== Registration Review ====================

export interface RegistrationFilterParams {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
    status?: string;
}

export async function getListRegistration(params?: RegistrationFilterParams) {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append("page", String(params.page));
    if (params?.size !== undefined) queryParams.append("size", String(params.size));
    if (params?.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params?.sortDir) queryParams.append("sortDir", params.sortDir);
    if (params?.status) queryParams.append("status", params.status);

    const res = await axiosInstance.get(`/api/admin/registration?${queryParams.toString()}`);
    return res.data;
}

export async function getRegistrationDetail(profileId: number) {
    const res = await axiosInstance.get(`/api/admin/registration/${profileId}`);
    return res.data;
}

export async function underReviewRegistrationApi(profileId: number) {
    const res = await axiosInstance.put(`/api/admin/registration/${profileId}`);
    return res.data;
}

export async function approveRegistrationApi(profileId: number) {
    const res = await axiosInstance.post(`/api/admin/registration/${profileId}/approve`);
    return res.data;
}

export async function rejectRegistrationApi(profileId: number, reason: string) {
    const res = await axiosInstance.post(`/api/admin/registration/${profileId}/reject`, { reason });
    return res.data;
}
