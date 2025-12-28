/**
 * User roles trong hệ thống
 */
export enum UserRole {
    ADMIN = "SUPER_ADMIN",
    STATION_OWNER = "STATION_OWNER",
    STAFF = "STAFF",
    USER = "USER",
}

/**
 * User type với role information
 */
export interface User {
    id: string;
    fullName: string;
    email: string;
    phone?: string;
    gender?: string;
    birthday?: string;
    avatarUrl?: string;
    role: UserRole;
    permissions?: string[];
}

/**
 * Route configuration với role requirements
 */
export interface RoutePermission {
    path: string;
    roles?: UserRole[];
}
