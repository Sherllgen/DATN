import { UserRole } from "@/types/user";
import type { RoutePermission } from "@/types/user";

/**
 * Kiểm tra xem user có quyền truy cập route không (dùng chung cho cả middleware và frontend)
 */
export function hasAccess(
    userRole: string | undefined,
    route: RoutePermission
): boolean {
    if (!route.roles || route.roles.length === 0) {
        return true;
    }
    if (!userRole) {
        return false;
    }
    return route.roles.includes(userRole as UserRole);
}
