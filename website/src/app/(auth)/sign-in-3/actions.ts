"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

const API_BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function loginAction(formData: FormData) {
    const email = formData.get("email") as string;
    const password = formData.get("password") as string;

    try {
        const response = await fetch(`${API_BACKEND_URL}/api/v1/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                email,
                password,
            }),
        });

        if (!response.ok) {
            const error = await response.json();
            return {
                success: false,
                message: error.message || "Login failed",
            };
        }

        const data = await response.json();

        // Lưu accessToken vào httpOnly cookie
        // Backend trả về data.data.accessToken, không phải data.accessToken
        if (data.data?.accessToken) {
            (await cookies()).set("accessToken", data.data.accessToken, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                sameSite: "lax",
                maxAge: 60 * 60 * 24 * 1, // 1 ngày
                path: "/",
            });

            // Có thể lưu thêm refreshToken nếu cần
            if (data.data.refreshToken) {
                (await cookies()).set("refreshToken", data.data.refreshToken, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === "production",
                    sameSite: "lax",
                    maxAge: 60 * 60 * 24 * 30, // 30 ngày
                    path: "/",
                });
            }
        }
    } catch (error) {
        console.error("Login error:", error);
        return {
            success: false,
            message: "An error occurred during login",
        };
    }

    redirect("/dashboard");
}

export async function logoutAction() {
    (await cookies()).delete("accessToken");
    (await cookies()).delete("refreshToken");
    redirect("/sign-in-3");
}
