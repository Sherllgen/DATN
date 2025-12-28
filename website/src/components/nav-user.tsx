"use client";

import {
    CreditCard,
    EllipsisVertical,
    LogOut,
    BellDot,
    CircleUser,
} from "lucide-react";
import Link from "next/link";
import { logoutAction } from "@/app/(auth)/sign-in-3/actions";

import { Logo } from "@/components/logo";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    useSidebar,
} from "@/components/ui/sidebar";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { useUserStore } from "@/contexts/user.store";

export function NavUser() {
    const { isMobile } = useSidebar();

    const user = useUserStore((state) => state.user!);
    console.log("user:", user);

    return (
        <SidebarMenu>
            <SidebarMenuItem>
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <SidebarMenuButton
                            size="lg"
                            className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground cursor-pointer"
                        >
                            <Avatar className="rounded-lg w-8 h-8">
                                <AvatarImage
                                    src={user.avatarUrl || ""}
                                    alt={user.fullName}
                                />
                                <AvatarFallback className="rounded-lg">
                                    CN
                                </AvatarFallback>
                            </Avatar>
                            <div className="flex-1 grid text-sm text-left leading-tight">
                                <span className="font-medium truncate">
                                    {user.fullName}
                                </span>
                                <span className="text-muted-foreground text-xs truncate">
                                    {user.email}
                                </span>
                            </div>
                            <EllipsisVertical className="ml-auto size-4" />
                        </SidebarMenuButton>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent
                        className="w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg"
                        side={isMobile ? "bottom" : "right"}
                        align="end"
                        sideOffset={4}
                    >
                        <DropdownMenuLabel className="p-0 font-normal">
                            <div className="flex items-center gap-2 px-1 py-1.5 text-sm text-left">
                                <Avatar className="rounded-lg w-8 h-8">
                                    <AvatarImage
                                        src={user.avatarUrl}
                                        alt={user.fullName}
                                    />
                                    <AvatarFallback className="rounded-lg">
                                        CN
                                    </AvatarFallback>
                                </Avatar>
                                <div className="flex-1 grid text-sm text-left leading-tight">
                                    <span className="font-medium truncate">
                                        {user.fullName}
                                    </span>
                                    <span className="text-muted-foreground text-xs truncate">
                                        {user.email}
                                    </span>
                                </div>
                            </div>
                        </DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuGroup>
                            <DropdownMenuItem
                                asChild
                                className="cursor-pointer"
                            >
                                <Link href="/settings/account">
                                    <CircleUser />
                                    Account
                                </Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                asChild
                                className="cursor-pointer"
                            >
                                <Link href="/settings/billing">
                                    <CreditCard />
                                    Billing
                                </Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                asChild
                                className="cursor-pointer"
                            >
                                <Link href="/settings/notifications">
                                    <BellDot />
                                    Notifications
                                </Link>
                            </DropdownMenuItem>
                        </DropdownMenuGroup>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                            className="cursor-pointer"
                            onClick={async () => {
                                await logoutAction();
                            }}
                        >
                            <LogOut />
                            Log out
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            </SidebarMenuItem>
        </SidebarMenu>
    );
}
