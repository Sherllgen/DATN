import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// GET /api/stations/[id]/photos
export async function GET(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
        }

        const response = await fetch(`${API_BACKEND_URL}/api/v1/stations/${id}/photos`, {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to get photos" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Get photos error:", error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 });
    }
}

// POST /api/stations/[id]/photos — multipart file upload
export async function POST(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
        }

        const formData = await request.formData();

        // Forward FormData to backend as-is
        const response = await fetch(`${API_BACKEND_URL}/api/v1/stations/${id}/photos`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                // Do NOT set Content-Type — fetch auto-sets multipart boundary
            },
            body: formData,
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to add photo" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Add photo error:", error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 });
    }
}
