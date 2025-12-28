import { UserRole } from "@/types/user";
import type { RoutePermission } from "@/types/user";

/**
 * Route configuration với role requirements (dùng chung cho cả middleware và sidebar)
 */
export const protectedRoutes: RoutePermission[] = [
    { path: "/dashboard", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/chat", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/mail", roles: [UserRole.ADMIN] },
    { path: "/calendar", roles: [UserRole.ADMIN] },
    { path: "/tasks", roles: [UserRole.ADMIN] },
    { path: "/manage_accounts", roles: [UserRole.ADMIN] },
    { path: "/registration", roles: [UserRole.ADMIN] },
    { path: "/settings", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/faqs" }, // Tất cả user authenticated
    { path: "/pricing" }, // Tất cả user authenticated
];
