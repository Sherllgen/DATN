import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function POST(request: NextRequest) {
    try {
        const cookieStore = await cookies();

        // Xóa cookies
        cookieStore.delete("accessToken");
        cookieStore.delete("refreshToken");
        cookieStore.delete("userRole");

        return NextResponse.json(
            { success: true, message: "Logged out successfully" },
            { status: 200 }
        );
    } catch (error) {
        return NextResponse.json(
            { success: false, message: "Logout failed" },
            { status: 500 }
        );
    }
}
