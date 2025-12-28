import axios from "axios";

const API_BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function loginApi(email: string, password: string) {
    const res = await axios.post(`${API_BACKEND_URL}/api/v1/auth/login`, {
        email,
        password,
    });

    return res.data;
}

export async function changePasswordApi(
    currentPassword: string,
    newPassword: string,
    confirmPassword: string
) {
    const res = await axios.put(`${API_BACKEND_URL}/api/v1/users/me/password`, {
        currentPassword,
        newPassword,
        confirmPassword,
    });

    return res.data;
}

export async function forgotPasswordApi(email: string) {
    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/forgot-password`,
        {
            email,
        }
    );

    return res.data;
}

export async function resetPasswordApi(
    token: string,
    newPassword: string,
    confirmPassword: string
) {
    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/reset-password`,
        {
            token,
            newPassword,
            confirmPassword,
        }
    );

    return res.data;
}

export async function submitRegistrationApi(file: File) {
    const formData = new FormData();
    formData.append("registrationForm", file);

    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/register/station-owner`,
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );

    return res.data;
}
