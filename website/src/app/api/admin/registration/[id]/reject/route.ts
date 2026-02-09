import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

export async function POST(
    request: NextRequest,
    { params }: { params: Promise<{ id: string }> }
) {
    try {
        const cookieStore = await cookies();
        const accessToken = cookieStore.get("accessToken")?.value;
        const { id } = await params;

        if (!accessToken) {
            return NextResponse.json(
                { status: 401, message: "Unauthorized" },
                { status: 401 }
            );
        }

        const body = await request.json();

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/admin/station-owner/${id}/reject`,
            {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(body),
            }
        );

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error rejecting registration:", error);
        return NextResponse.json(
            { status: 500, message: "Internal Server Error" },
            { status: 500 }
        );
    }
}
