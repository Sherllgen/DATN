"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

const API_BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export interface LoginActionResult {
    success: boolean;
    message?: string;
    redirect?: string;
}

export async function loginAction(formData: FormData): Promise<LoginActionResult> {
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

        // Save accessToken to httpOnly cookie
        // Backend returns data.data.accessToken, not data.accessToken
        if (data.data?.accessToken) {
            (await cookies()).set("accessToken", data.data.accessToken, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                sameSite: "lax",
                maxAge: 60 * 60 * 24 * 1, // 1 day
                path: "/",
            });
        }

        if (data.data.refreshToken) {
            (await cookies()).set("refreshToken", data.data.refreshToken, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                sameSite: "lax",
                maxAge: 60 * 60 * 24 * 30, // 30 days
                path: "/",
            });
        }

        // Save user role so middleware can check RBAC
        // Backend returns roles as array, get the first role (user usually has only 1 role)
        if (data.data.user?.roles && data.data.user.roles.length > 0) {
            const primaryRole = Array.isArray(data.data.user.roles) 
                ? data.data.user.roles[0] 
                : data.data.user.roles;
            
            console.log("Setting userRole cookie:", primaryRole);
            
            (await cookies()).set("userRole", primaryRole, {
                httpOnly: false, // Needs to be false for middleware to read
                secure: process.env.NODE_ENV === "production",
                sameSite: "lax",
                maxAge: 60 * 60 * 24 * 1,
                path: "/",
            });

            if (primaryRole === "SUPER_ADMIN") {
                // Return destination to redirect outside try-catch
                return { success: true, redirect: "/admin/accounts" };
            }
        }
        return { success: true, redirect: "/dashboard" };
    } catch (error) {
        if (error instanceof Error && error.message === "NEXT_REDIRECT") {
            throw error;
        }
        console.error("Login error:", error);
        return {
            success: false,
            message: "An error occurred during login",
        };
    }
}

// LoginForm3 handles the redirect based on the return value if successful

export async function logoutAction() {
    (await cookies()).delete("accessToken");
    (await cookies()).delete("refreshToken");
    (await cookies()).delete("userRole");
    redirect("/sign-in-3");
}
