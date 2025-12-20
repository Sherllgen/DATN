"use client";

import { usePathname } from "next/navigation";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/shadcn/app-sidebar";

export default function AppLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();

    const noSidebarRoutes = ["/login", "/register", "/forgot-password"];
    const hideSidebar = noSidebarRoutes.includes(pathname);

    return (
        <SidebarProvider>
            {!hideSidebar && <AppSidebar />}
            <main className={`w-full ${!hideSidebar ? "px-6" : ""}`}>
                {!hideSidebar && <SidebarTrigger />}
                {children}
            </main>
        </SidebarProvider>
    );
}
