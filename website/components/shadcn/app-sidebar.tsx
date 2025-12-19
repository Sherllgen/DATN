"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar";
import { NavUser } from "../shadcn/nav-user";
import Image from "next/image";

// Menu items.
const items = [
    {
        title: "Quản lý trạm sạc",
        url: "/station-management",
    },
    {
        title: "Quản lý trụ sạc",
        url: "/charger-management",
    },
    {
        title: "Thêm dữ liệu",
        url: "/add-data",
    },
    {
        title: "Báo cáo & thống kê",
        url: "/reports",
    },
];

const data = {
    user: {
        name: "Nguyen Van A",
        email: "testuser1@gmail.com",
        avatar: "https://www.rophim.li/images/avatars/pack1/14.jpg",
    },
};

export function AppSidebar() {
    const pathname = usePathname();

    return (
        <Sidebar>
            <SidebarContent>
                <SidebarGroup>
                    <div className="flex items-center mb-4">
                        <Image
                            src="/logoBlue.png"
                            alt="Logo"
                            width={100}
                            height={100}
                            className=""
                        />
                        <p className="ml-2 font-bold text-primary text-xl">
                            STATION X
                        </p>
                    </div>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            {items.map((item) => {
                                const isActive =
                                    pathname === item.url ||
                                    pathname.startsWith(item.url + "/");

                                return (
                                    <SidebarMenuItem key={item.title}>
                                        <SidebarMenuButton
                                            asChild
                                            isActive={isActive}
                                            className="data-[state=open]:bg-transparent focus:bg-transparent active:bg-transparent"
                                        >
                                            <Link href={item.url}>
                                                {/* <item.icon /> */}
                                                <span>{item.title}</span>
                                            </Link>
                                        </SidebarMenuButton>
                                    </SidebarMenuItem>
                                );
                            })}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={data.user} />
            </SidebarFooter>
        </Sidebar>
    );
}
