"use client";

import * as React from "react";
import {
    LayoutDashboard,
    Shield,
    Settings,
    HelpCircle,
    CreditCard,
    LayoutTemplate,
    Users,
    Zap,
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
                title: "Stations",
                url: "/stations",
                icon: Zap,
            },
        ],
    },
    {
        label: "Admin",
        items: [
            {
                title: "Account Management",
                url: "/admin/accounts",
                icon: Users,
            },
            {
                title: "Registration Review",
                url: "/admin/registration",
                icon: LayoutTemplate,
            },
            {
                title: "Station Review",
                url: "/admin/stations",
                icon: Shield,
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
                        title: "Business Profile",
                        url: "/settings/business",
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
        ],
    },
];

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const user = useUserStore((s) => s.user);
    const userRole = user?.role;

    // Filter menu based on access permissions
    const navGroups = React.useMemo(() => {
        // Helper: get base path for sub-urls (e.g., /settings/user -> /settings)
        const getRootPath = (url: string) => {
            if (url.startsWith("/settings")) return "/settings";
            return url.split("/")[1] ? `/${url.split("/")[1]}` : url;
        };

        return navGroupsRaw.map((group) => ({
            ...group,
            items: group.items
                .map((item) => {
                    // Deep copy to avoid mutating raw data
                    let currentItem = { ...item };

                    // Remove specific sub-items that are empty or not needed for Admin and Station Owner
                    if (
                        (userRole === "SUPER_ADMIN" ||
                            userRole === "STATION_OWNER") &&
                        currentItem.items
                    ) {
                        currentItem.items = currentItem.items.filter(
                            (sub) =>
                                !["User Settings", "Business Profile", "Plans & Billing"].includes(
                                    sub.title
                                )
                        );
                    }

                    // If there are subitems
                    if (currentItem.items) {
                        const rootPath = getRootPath(currentItem.items[0].url);
                        const route = protectedRoutes.find(
                            (r) => r.path === rootPath
                        );
                        if (route && !hasAccess(userRole, route)) return null;
                        return currentItem;
                    }
                    // Check permissions for each item
                    const rootPath = getRootPath(currentItem.url);
                    const route = protectedRoutes.find(
                        (r) => r.path === rootPath
                    );
                    if (route && !hasAccess(userRole, route)) return null;
                    return currentItem;
                })
                .filter((item): item is (typeof group.items)[0] => !!item),
        })).filter((group) => group.items.length > 0);
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
