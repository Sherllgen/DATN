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

const data = {
    user: {
        name: "Ed Sheeran",
        email: "edsheeran@example.com",
        avatar: "https://www.rophim.li/images/avatars/pack1/14.jpg",
    },
    navGroups: [
        {
            label: "Dashboards",
            items: [
                {
                    title: "Dashboard 1",
                    url: "/dashboard",
                    icon: LayoutDashboard,
                },
                {
                    title: "Users",
                    url: "/users",
                    icon: Users,
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
    ],
};

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
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
                {data.navGroups.map((group) => (
                    <NavMain
                        key={group.label}
                        label={group.label}
                        items={group.items}
                    />
                ))}
            </SidebarContent>
            <SidebarFooter>
                {/* <SidebarNotification /> */}
                <NavUser user={data.user} />
            </SidebarFooter>
        </Sidebar>
    );
}
