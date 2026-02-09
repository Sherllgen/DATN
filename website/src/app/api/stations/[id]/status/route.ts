import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// PATCH /api/stations/[id]/status - Update station status
export async function PATCH(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json(
                { message: "Unauthorized - No token" },
                { status: 401 }
            );
        }

        const { searchParams } = new URL(request.url);
        const status = searchParams.get("status");

        if (!status) {
            return NextResponse.json(
                { message: "Status parameter is required" },
                { status: 400 }
            );
        }

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/stations/${id}/status?status=${status}`,
            {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${accessToken}`,
                },
            }
        );

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to update station status" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Update station status error:", error);
        return NextResponse.json(
            { message: "Internal server error" },
            { status: 500 }
        );
    }
}
