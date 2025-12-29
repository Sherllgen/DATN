# Role-Based Access Control (RBAC) Documentation

## Tổng quan

Hệ thống RBAC được implement để quản lý quyền truy cập dựa trên vai trò người dùng. Bao gồm:

-   ✅ **User roles**: ADMIN, STATION_OWNER, USER
-   ✅ **Middleware protection**: Tự động check quyền truy cập cho mỗi route
-   ✅ **Client-side guards**: Components và hooks để điều khiển hiển thị
-   ✅ **Type-safe**: TypeScript types cho tất cả roles và permissions

## User Roles

```typescript
export enum UserRole {
    ADMIN = "ADMIN", // Quản trị viên - full access
    STATION_OWNER = "STATION_OWNER", // Chủ trạm sạc
    USER = "USER", // User thông thường
}
```

## Cấu hình Routes

### Middleware Configuration

File: `src/middleware.ts`

```typescript
const protectedRoutes: RoutePermission[] = [
    { path: "/dashboard", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/users", roles: [UserRole.ADMIN] }, // Chỉ admin
    { path: "/settings", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/faqs" }, // Tất cả user authenticated
];
```

**Cách hoạt động:**

-   Nếu route có `roles` array → chỉ users với role đó mới truy cập được
-   Nếu route không có `roles` → tất cả authenticated users đều truy cập được
-   Nếu user không có quyền → redirect tới `/unauthorized`

## Client-Side Usage

### 1. Hooks

#### `useHasRole(roles)`

Kiểm tra user có role cụ thể không:

```typescript
import { useHasRole } from "@/hooks/use-role";
import { UserRole } from "@/types/user";

function MyComponent() {
    const isAdmin = useHasRole(UserRole.ADMIN);
    const canManage = useHasRole([UserRole.ADMIN, UserRole.STATION_OWNER]);

    return (
        <div>
            {isAdmin && <AdminButton />}
            {canManage && <ManageButton />}
        </div>
    );
}
```

#### `useIsAdmin()` & `useIsStationOwner()`

Shortcuts cho việc check role phổ biến:

```typescript
import { useIsAdmin, useIsStationOwner } from "@/hooks/use-role";

function Dashboard() {
    const isAdmin = useIsAdmin();
    const isOwner = useIsStationOwner();

    return (
        <div>
            {isAdmin && <AdminPanel />}
            {isOwner && <OwnerPanel />}
        </div>
    );
}
```

#### `useUserRole()`

Lấy role hiện tại:

```typescript
import { useUserRole } from "@/hooks/use-role";

function Header() {
    const role = useUserRole();

    return <div>Current role: {role}</div>;
}
```

### 2. Components

#### `<RoleGuard>`

Wrapper component để điều khiển render dựa trên role:

```typescript
import { RoleGuard } from "@/components/role-guard";
import { UserRole } from "@/types/user";

function App() {
    return (
        <RoleGuard roles={UserRole.ADMIN} fallback={<div>Access denied</div>}>
            <AdminContent />
        </RoleGuard>
    );
}

// Multiple roles
function Dashboard() {
    return (
        <RoleGuard roles={[UserRole.ADMIN, UserRole.STATION_OWNER]}>
            <ManagementPanel />
        </RoleGuard>
    );
}
```

#### `<AdminOnly>` & `<StationOwnerOnly>`

Shortcuts components:

```typescript
import { AdminOnly, StationOwnerOnly } from "@/components/role-guard";

function Page() {
    return (
        <>
            <AdminOnly>
                <UserManagement />
            </AdminOnly>

            <StationOwnerOnly fallback={<div>Owner only</div>}>
                <StationSettings />
            </StationOwnerOnly>
        </>
    );
}
```

## Backend Integration

### Login Response Format

Backend cần trả về format sau khi login:

```json
{
    "data": {
        "accessToken": "jwt_token_here",
        "refreshToken": "refresh_token_here",
        "user": {
            "id": "user_id",
            "email": "user@example.com",
            "fullName": "User Name",
            "role": "ADMIN" // ← Quan trọng!
        }
    }
}
```

### User Profile Response

API `/api/v1/users/me` cũng cần trả về role:

```json
{
    "data": {
        "id": "user_id",
        "email": "user@example.com",
        "fullName": "User Name",
        "role": "STATION_OWNER" // ← Quan trọng!
    }
}
```

## Examples

### Example 1: Conditional Rendering trong Component

```typescript
import { useHasRole, useIsAdmin } from "@/hooks/use-role";
import { UserRole } from "@/types/user";

export function Navbar() {
    const isAdmin = useIsAdmin();
    const canManageStations = useHasRole([
        UserRole.ADMIN,
        UserRole.STATION_OWNER,
    ]);

    return (
        <nav>
            <a href="/dashboard">Dashboard</a>

            {canManageStations && <a href="/stations">Manage Stations</a>}

            {isAdmin && <a href="/users">User Management</a>}
        </nav>
    );
}
```

### Example 2: Sử dụng RoleGuard trong Page

```typescript
import { RoleGuard, AdminOnly } from "@/components/role-guard";
import { UserRole } from "@/types/user";

export default function SettingsPage() {
    return (
        <div>
            <h1>Settings</h1>

            {/* Section cho tất cả users */}
            <section>
                <h2>Profile Settings</h2>
                <ProfileForm />
            </section>

            {/* Section chỉ cho Station Owner và Admin */}
            <RoleGuard roles={[UserRole.ADMIN, UserRole.STATION_OWNER]}>
                <section>
                    <h2>Station Management</h2>
                    <StationSettings />
                </section>
            </RoleGuard>

            {/* Section chỉ cho Admin */}
            <AdminOnly>
                <section>
                    <h2>System Configuration</h2>
                    <SystemConfig />
                </section>
            </AdminOnly>
        </div>
    );
}
```

### Example 3: Thêm Route mới với Role Protection

**Bước 1:** Update middleware

```typescript
// src/middleware.ts
const protectedRoutes: RoutePermission[] = [
    // ... existing routes
    { path: "/analytics", roles: [UserRole.ADMIN, UserRole.STATION_OWNER] },
    { path: "/reports", roles: [UserRole.ADMIN] }, // Admin only
];
```

**Bước 2:** Tạo page với role guards

```typescript
// src/app/analytics/page.tsx
import { RoleGuard } from "@/components/role-guard";
import { UserRole } from "@/types/user";

export default function AnalyticsPage() {
    return (
        <div>
            <h1>Analytics</h1>

            {/* Basic analytics cho cả Admin và Station Owner */}
            <BasicAnalytics />

            {/* Advanced analytics chỉ cho Admin */}
            <RoleGuard roles={UserRole.ADMIN}>
                <AdvancedAnalytics />
            </RoleGuard>
        </div>
    );
}
```

## Testing

### Test với các roles khác nhau:

1. **Login với ADMIN role** → Có thể truy cập tất cả routes
2. **Login với STATION_OWNER role** → Không thể truy cập `/users`
3. **Login với USER role** → Chỉ truy cập được các route không có role restriction

## Troubleshooting

### User bị redirect về `/unauthorized` khi đăng nhập

**Nguyên nhân:** Backend không trả về `role` trong response

**Giải pháp:** Check response từ API `/api/v1/auth/login` và `/api/v1/users/me` có field `role` không

### Role không được lưu vào cookie

**Nguyên nhân:** `data.data.user.role` không tồn tại trong login response

**Giải pháp:** Xem lại [login action](<src/app/(auth)/sign-in-3/actions.ts#L47-L54>)

### Component vẫn hiển thị dù user không có quyền

**Nguyên nhân:** Chỉ dùng middleware check, chưa dùng client-side guards

**Giải pháp:** Thêm `<RoleGuard>` hoặc `useHasRole()` hook vào component

## Files Changed

-   ✅ `src/types/user.ts` - User types và roles
-   ✅ `src/contexts/user.store.tsx` - User store với role support
-   ✅ `src/middleware.ts` - RBAC middleware
-   ✅ `src/hooks/use-role.ts` - Role checking hooks
-   ✅ `src/components/role-guard.tsx` - Role guard components
-   ✅ `src/app/unauthorized/page.tsx` - Unauthorized page
-   ✅ `src/app/(auth)/sign-in-3/actions.ts` - Login action lưu role

## Next Steps (Optional)

Nếu muốn nâng cao thêm:

1. **Permissions system**: Thêm fine-grained permissions thay vì chỉ roles
2. **Dynamic roles**: Load roles từ backend thay vì hardcode
3. **Audit logging**: Log mọi lần user truy cập protected resources
4. **Role hierarchy**: ADMIN kế thừa tất cả permissions của STATION_OWNER
