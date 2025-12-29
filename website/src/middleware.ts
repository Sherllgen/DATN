import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { protectedRoutes } from "@/config/protected-routes";
import { RoutePermission, UserRole } from "./types/user";
import { hasAccess } from "@/config/has-access";

// Các route dành cho guest (chưa đăng nhập)
const authRoutes = ["/sign-in-3", "/register"];

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;
    const accessToken = request.cookies.get("accessToken")?.value;
    const userRole = request.cookies.get("userRole")?.value;

    // Kiểm tra nếu đang truy cập protected route
    const matchedRoute = protectedRoutes.find((route) =>
        pathname.startsWith(route.path)
    );

    if (matchedRoute) {
        // Chưa đăng nhập -> redirect về login
        if (!accessToken) {
            const url = new URL("/sign-in-3", request.url);
            url.searchParams.set("redirect", pathname);
            return NextResponse.redirect(url);
        }

        // Đã đăng nhập nhưng không có quyền -> redirect về unauthorized
        if (!hasAccess(userRole, matchedRoute)) {
            return NextResponse.redirect(new URL("/unauthorized", request.url));
        }
    }

    // Kiểm tra nếu đã đăng nhập mà truy cập auth routes
    const isAuthRoute = authRoutes.some((route) => pathname.startsWith(route));

    if (isAuthRoute && accessToken) {
        // Redirect về dashboard
        return NextResponse.redirect(new URL("/dashboard", request.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        // Match all request paths except for the ones starting with:
        // - api (API routes)
        // - _next/static (static files)
        // - _next/image (image optimization files)
        // - favicon.ico (favicon file)
        "/((?!api|_next/static|_next/image|favicon.ico).*)",
    ],
};
