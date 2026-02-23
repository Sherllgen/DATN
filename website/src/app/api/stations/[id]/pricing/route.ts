import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// GET /api/stations/[id]/pricing — Get active pricing (public)
export async function GET(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
        }

        const response = await fetch(`${API_BACKEND_URL}/api/v1/stations/${id}/pricing`, {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to get pricing" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Get pricing error:", error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 });
    }
}

// POST /api/stations/[id]/pricing — Create new pricing version
export async function POST(request: Request, context: RouteContext) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await context.params;

        if (!accessToken) {
            return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
        }

        const body = await request.json();

        const response = await fetch(`${API_BACKEND_URL}/api/v1/stations/${id}/pricing`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
            body: JSON.stringify(body),
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            return NextResponse.json(
                { message: error.message || "Failed to create pricing" },
                { status: response.status }
            );
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Create pricing error:", error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 });
    }
}
