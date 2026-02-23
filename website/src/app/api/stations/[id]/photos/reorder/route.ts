import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// PUT /api/stations/[id]/photos/reorder
export async function PUT(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
        }

        const body = await request.json();

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/stations/${id}/photos/reorder`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${accessToken}`,
                },
                body: JSON.stringify(body),
            }
        );

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to reorder photos" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Reorder photos error:", error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 });
    }
}
