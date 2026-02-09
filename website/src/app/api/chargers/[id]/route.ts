import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

interface RouteContext {
    params: Promise<{ id: string }>;
}

// GET /api/chargers/[id] - Get charger by ID
export async function GET(request: NextRequest, context: RouteContext) {
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

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/chargers/${id}`,
            {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            }
        );

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error fetching charger:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}

// PUT /api/chargers/[id] - Update charger
export async function PUT(request: NextRequest, context: RouteContext) {
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
            `${API_BACKEND_URL}/api/v1/chargers/${id}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${accessToken}`,
                },
                body: JSON.stringify(body),
            }
        );

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error updating charger:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}

// DELETE /api/chargers/[id] - Delete charger
export async function DELETE(request: NextRequest, context: RouteContext) {
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

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/chargers/${id}`,
            {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            }
        );

        if (response.status === 204) {
            return new NextResponse(null, { status: 204 });
        }

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error deleting charger:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}
