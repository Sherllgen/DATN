import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const API_BACKEND_URL = process.env.API_BACKEND_URL || "http://localhost:8080";

export async function GET(
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

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/admin/accounts/${id}`,
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
        console.error("Error fetching account:", error);
        return NextResponse.json(
            { status: 500, message: "Internal Server Error" },
            { status: 500 }
        );
    }
}

export async function DELETE(
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

        const response = await fetch(
            `${API_BACKEND_URL}/api/v1/admin/accounts/${id}`,
            {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const data = await response.json();
        return NextResponse.json(data, { status: response.status });
    } catch (error) {
        console.error("Error deleting account:", error);
        return NextResponse.json(
            { status: 500, message: "Internal Server Error" },
            { status: 500 }
        );
    }
}
