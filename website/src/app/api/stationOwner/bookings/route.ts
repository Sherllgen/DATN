import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

export async function GET(request: NextRequest) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;

        if (!accessToken) {
            return NextResponse.json(
                { message: "Unauthorized - No token" },
                { status: 401 }
            );
        }

        const searchParams = request.nextUrl.searchParams;
        const page = searchParams.get("page") || "0";
        const size = searchParams.get("size") || "10";
        const sortBy = searchParams.get("sortBy") || "createdAt";
        const sortDir = searchParams.get("sortDir") || "DESC";

        const queryParams = new URLSearchParams({
            page,
            size,
            sortBy,
            sortDir
        });

        const response = await fetch(`${API_BACKEND_URL}/api/v1/bookings/owner?${queryParams.toString()}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to fetch owner bookings" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Get owner bookings error:", error);
        return NextResponse.json(
            { message: "Internal server error" },
            { status: 500 }
        );
    }
}
