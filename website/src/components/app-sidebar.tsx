"use client";

import * as React from "react";
import {
    LayoutPanelLeft,
    LayoutDashboard,
    Mail,
    CheckSquare,
    MessageCircle,
    Calendar,
    Shield,
    AlertTriangle,
    Settings,
    HelpCircle,
    CreditCard,
    LayoutTemplate,
    Users,
    MessagesSquare,
} from "lucide-react";
import Link from "next/link";
import { SidebarNotification } from "@/components/sidebar-notification";

import { NavMain } from "@/components/nav-main";
import { NavUser } from "@/components/nav-user";
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar";
import Image from "next/image";
import { useUserStore } from "@/contexts/user.store";
import { protectedRoutes } from "@/config/protected-routes";
import { hasAccess } from "@/config/has-access";

const navGroupsRaw = [
    {
        label: "Dashboards",
        items: [
            {
                title: "Dashboard",
                url: "/dashboard",
                icon: LayoutDashboard,
            },
            {
                title: "Manage Accounts",
                url: "/manage_accounts",
                icon: Users,
            },
            {
                title: "Registration",
                url: "/registration",
                icon: LayoutTemplate,
            },
        ],
    },
    {
        label: "Apps",
        items: [
            {
                title: "Mail",
                url: "/mail",
                icon: Mail,
            },
            {
                title: "Chat",
                url: "/chat",
                icon: MessagesSquare,
            },
            {
                title: "Calendar",
                url: "/calendar",
                icon: Calendar,
            },
        ],
    },
    {
        label: "Pages",
        items: [
            {
                title: "Settings",
                url: "#",
                icon: Settings,
                items: [
                    {
                        title: "User Settings",
                        url: "/settings/user",
                    },
                    {
                        title: "Account Settings",
                        url: "/settings/account",
                    },
                    {
                        title: "Plans & Billing",
                        url: "/settings/billing",
                    },
                ],
            },
            {
                title: "FAQs",
                url: "/faqs",
                icon: HelpCircle,
            },
            {
                title: "Pricing",
                url: "/pricing",
                icon: CreditCard,
            },
        ],
    },
];

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const user = useUserStore((s) => s.user);
    const userRole = user?.role;

    // Lọc menu theo quyền truy cập
    const navGroups = React.useMemo(() => {
        // Helper: lấy path gốc cho các url con (ví dụ /settings/user -> /settings)
        const getRootPath = (url: string) => {
            if (url.startsWith("/settings")) return "/settings";
            return url.split("/")[1] ? `/${url.split("/")[1]}` : url;
        };

        return navGroupsRaw.map((group) => ({
            ...group,
            items: group.items
                .map((item) => {
                    // Nếu có subitems (ví dụ Settings)
                    if (item.items) {
                        // Kiểm tra quyền với path gốc settings
                        const rootPath = getRootPath(item.items[0].url);
                        const route = protectedRoutes.find(
                            (r) => r.path === rootPath
                        );
                        if (route && !hasAccess(userRole, route)) return null;
                        return item;
                    }
                    // Kiểm tra quyền với từng item
                    const rootPath = getRootPath(item.url);
                    const route = protectedRoutes.find(
                        (r) => r.path === rootPath
                    );
                    if (route && !hasAccess(userRole, route)) return null;
                    return item;
                })
                .filter((item): item is (typeof group.items)[0] => !!item),
        }));
    }, [userRole]);

    return (
        <Sidebar {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton size="lg" asChild>
                            <Link href="/dashboard">
                                <div className="flex justify-center items-center bg-primary rounded-lg size-8 aspect-square text-primary-foreground">
                                    <Image
                                        src="/logo.png"
                                        alt="Logo"
                                        width={26}
                                        height={26}
                                        className="brightness-0 invert"
                                    />
                                </div>
                                <div className="flex-1 grid text-sm text-left leading-tight">
                                    <span className="font-medium truncate">
                                        EV Management
                                    </span>
                                    <span className="text-xs truncate">
                                        Management Platform
                                    </span>
                                </div>
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                {navGroups.map((group) => (
                    <NavMain
                        key={group.label}
                        label={group.label}
                        items={group.items}
                    />
                ))}
            </SidebarContent>
            <SidebarFooter>
                {/* <SidebarNotification /> */}
                <NavUser />
            </SidebarFooter>
        </Sidebar>
    );
}
