import axios from "axios";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export async function loginApi(email: string, password: string) {
    const res = await axios.post(`${API_BACKEND_URL}/api/v1/auth/login`, {
        email,
        password,
    });

    return res.data;
}

export async function registerApi(
    fullName: string,
    email: string,
    password: string
) {
    const res = await axios.post(`${API_BACKEND_URL}/api/v1/auth/register`, {
        fullName,
        email,
        password,
    });

    return res.data;
}

export async function loginGoogleApi(idToken: string) {
    const res = await axios.post(`${API_BACKEND_URL}/api/v1/auth/google`, {
        idToken,
    });
    return res.data;
}

export async function verifyEmailOtpApi(otp: string, email: string) {
    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/verify-email`,
        {
            otp,
            email,
        }
    );

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
