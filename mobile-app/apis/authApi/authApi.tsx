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

export async function verifyEmailOtp(otp: string, email: string) {
    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/verify-email`,
        {
            otp,
            email,
        }
    );

    return res.data;
}

export async function resendEmailOtp(email: string) {
    const res = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/resend-email-otp`,
        {
            email,
        }
    );
    return res.data;
}
