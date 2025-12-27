import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Các route yêu cầu đăng nhập
const protectedRoutes = [
    "/dashboard",
    "/chat",
    "/mail",
    "/calendar",
    "/tasks",
    "/users",
    "/settings",
];
// Các route dành cho guest (chưa đăng nhập)
const authRoutes = ["/landing", "/sign-in-3", "/register"];

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;
    const accessToken = request.cookies.get("accessToken")?.value;

    // Kiểm tra nếu đang truy cập protected route mà chưa đăng nhập
    const isProtectedRoute = protectedRoutes.some((route) =>
        pathname.startsWith(route)
    );

    if (isProtectedRoute && !accessToken) {
        // Redirect về trang đăng nhập
        const url = new URL("/sign-in-3", request.url);
        url.searchParams.set("redirect", pathname);
        return NextResponse.redirect(url);
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
