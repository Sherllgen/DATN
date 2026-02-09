import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// POST /api/admin/stations/[id]/reject - Reject station
export async function POST(request: NextRequest, context: RouteContext) {
    try {
        const { id } = await context.params;
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;

        if (!accessToken) {
            return NextResponse.json(
                { error: "Unauthorized" },
                { status: 401 }
            );
        }

        const body = await request.json();

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/admin/stations/${id}/reject`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${accessToken}`,
                },
                body: JSON.stringify(body),
            }
        );

        if (response.status === 204 || response.status === 200) {
            return NextResponse.json(
                { message: "Station rejected successfully" },
                { status: 200 }
            );
        }

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error rejecting station:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}
