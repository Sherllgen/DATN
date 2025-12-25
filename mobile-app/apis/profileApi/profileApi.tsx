import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export async function getProfileApi() {
    const res = await axiosInstance.get(`${API_BACKEND_URL}/api/v1/users/me`);

    return res.data;
}

export async function updateProfileApi(data: {
    fullName: string;
    gender: string;
    birthday: string;
}) {
    const res = await axiosInstance.put(
        `${API_BACKEND_URL}/api/v1/users/me`,
        data
    );

    return res.data;
}

export async function getUploadSignature() {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/users/me/avatar/upload-signature`,
        {}
    );
    return res.data;
}

export async function uploadAvatarApi(avatarUrl: string, publicId: string) {
    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/users/me/avatar`,
        { avatarUrl, publicId }
    );
    return res.data;
}
