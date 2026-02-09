import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

// GET /api/admin/stations - List stations with status filter
export async function GET(request: NextRequest) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;

        if (!accessToken) {
            return NextResponse.json(
                { error: "Unauthorized" },
                { status: 401 }
            );
        }

        const { searchParams } = new URL(request.url);
        const status = searchParams.get("status");
        const page = searchParams.get("page") || "0";
        const size = searchParams.get("size") || "20";

        let url = `${API_BACKEND_URL}/api/v1/admin/stations?page=${page}&size=${size}`;
        if (status) {
            url += `&status=${status}`;
        }

        const response = await fetch(url, {
            method: "GET",
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error fetching admin stations:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}
