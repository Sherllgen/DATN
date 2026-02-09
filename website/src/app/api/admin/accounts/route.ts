import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

export async function GET(request: NextRequest) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;

        if (!accessToken) {
            return NextResponse.json(
                { status: 401, message: "Unauthorized" },
                { status: 401 }
            );
        }

        const { searchParams } = new URL(request.url);
        const page = searchParams.get("page") || "0";
        const size = searchParams.get("size") || "10";
        const sortBy = searchParams.get("sortBy") || "createdAt";
        const sortDir = searchParams.get("sortDir") || "DESC";
        const status = searchParams.get("status");
        const role = searchParams.get("role");
        const search = searchParams.get("search");

        const params = new URLSearchParams({ page, size, sortBy, sortDir });
        if (status) params.append("status", status);
        if (role) params.append("role", role);
        if (search) params.append("search", search);

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/admin/accounts?${params.toString()}`,
            {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error fetching accounts:", error);
        return NextResponse.json(
            { status: 500, message: "Internal Server Error" },
            { status: 500 }
        );
    }
}
