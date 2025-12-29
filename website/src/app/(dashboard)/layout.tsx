"use client";

import React, { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ToastContainer } from "react-toastify";

import { AppSidebar } from "@/components/app-sidebar";
import { SiteHeader } from "@/components/site-header";
import { SidebarProvider, SidebarInset } from "@/components/ui/sidebar";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Button } from "@/components/ui/button";

import { useSidebarConfig } from "@/hooks/use-sidebar-config";
import { useUserStore } from "@/contexts/user.store";
import { getProfileApi } from "@/apis/stationOwner/stationOwnerApi";

interface DashboardLayoutProps {
    children: React.ReactNode;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
    const router = useRouter();
    const { config } = useSidebarConfig();
    const setUser = useUserStore((state) => state.setUser);

    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    /**
     * Fetch user profile
     * - Success: set user into store
     * - 401/403: redirect to login
     * - Other errors: show error UI
     */
    const fetchProfile = useCallback(async () => {
        try {
            setIsLoading(true);
            setError(null);

            const response = await getProfileApi();

            if (!response?.data) {
                throw new Error("EMPTY_PROFILE");
            }

            setUser({
                ...response.data,
                role: response.data.roles[0],
            });
        } catch (err: any) {
            console.error("Fetch profile failed:", err);

            if (err.status === 401 || err.status === 403) {
                try {
                    await fetch("/api/auth/logout", { method: "POST" });
                } catch (logoutErr) {
                    console.error("Logout API failed:", logoutErr);
                } finally {
                    router.replace("/sign-in-3");
                    return;
                }
            }

            setError("Không thể tải thông tin người dùng. Vui lòng thử lại.");
        } finally {
            setIsLoading(false);
        }
    }, [router, setUser]);

    useEffect(() => {
        fetchProfile();
    }, [fetchProfile]);

    /* ----------------------- UI STATES ----------------------- */

    if (isLoading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="space-y-4 text-center">
                    <LoadingSpinner size="lg" />
                    <p className="text-muted-foreground">
                        Đang tải thông tin người dùng...
                    </p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="space-y-4 p-6 max-w-md text-center">
                    <div className="text-destructive text-5xl">⚠️</div>
                    <h2 className="font-semibold text-2xl">Có lỗi xảy ra</h2>
                    <p className="text-muted-foreground">{error}</p>
                    <Button onClick={() => router.replace("/sign-in-3")}>
                        Đăng nhập lại
                    </Button>
                </div>
            </div>
        );
    }

    /* ----------------------- LAYOUT ----------------------- */

    return (
        <SidebarProvider
            style={
                {
                    "--sidebar-width": "16rem",
                    "--sidebar-width-icon": "3rem",
                    "--header-height": "calc(var(--spacing) * 14)",
                } as React.CSSProperties
            }
            className={config.collapsible === "none" ? "sidebar-none-mode" : ""}
        >
            {config.side === "left" ? (
                <>
                    <AppSidebar
                        variant={config.variant}
                        collapsible={config.collapsible}
                        side={config.side}
                    />
                    <MainContent>{children}</MainContent>
                </>
            ) : (
                <>
                    <MainContent>{children}</MainContent>
                    <AppSidebar
                        variant={config.variant}
                        collapsible={config.collapsible}
                        side={config.side}
                    />
                </>
            )}
        </SidebarProvider>
    );
}

/* ----------------------- SUB COMPONENT ----------------------- */

function MainContent({ children }: { children: React.ReactNode }) {
    return (
        <SidebarInset>
            <SiteHeader />
            <div className="flex flex-col flex-1">
                <div className="@container/main flex flex-col flex-1 gap-2">
                    <div className="flex flex-col gap-4 md:gap-6 py-4 md:py-6">
                        {children}
                    </div>
                </div>
            </div>
            <ToastContainer autoClose={3000} />
        </SidebarInset>
    );
}
