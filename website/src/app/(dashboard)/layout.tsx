"use client";

import React, { useEffect } from "react";
import { AppSidebar } from "@/components/app-sidebar";
import { SiteHeader } from "@/components/site-header";
import { SidebarProvider, SidebarInset } from "@/components/ui/sidebar";
import {
    ThemeCustomizer,
    ThemeCustomizerTrigger,
} from "@/components/theme-customizer";
import { useSidebarConfig } from "@/hooks/use-sidebar-config";
import { useUserStore } from "@/contexts/user.store";

import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Button } from "@/components/ui/button";
import { getProfileApi } from "@/apis/stationOwner/stationOwnerApi";

export default function DashboardLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);
    const { config } = useSidebarConfig();
    const setUser = useUserStore((state) => state.setUser);

    const fetchProfile = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const response = await getProfileApi();

            if (response.data) {
                setUser(response.data);
            } else {
                setError(
                    "Không thể tải thông tin người dùng. Vui lòng thử lại."
                );
            }
        } catch (error) {
            console.error("Failed to fetch profile:", error);
            setError("Không thể tải thông tin người dùng. Vui lòng thử lại.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchProfile();
    }, [setUser]);

    // Hiển thị loading spinner khi đang lấy dữ liệu
    if (isLoading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="space-y-4 text-center">
                    <LoadingSpinner size="lg" />
                    <p className="text-muted-foreground">
                        Đang tải thông tin...
                    </p>
                </div>
            </div>
        );
    }

    // Hiển thị error state khi có lỗi
    if (error) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="space-y-4 mx-auto p-6 max-w-md text-center">
                    <div className="text-destructive text-5xl">⚠️</div>
                    <h2 className="font-semibold text-2xl">Có lỗi xảy ra</h2>
                    <p className="text-muted-foreground">{error}</p>
                    <Button onClick={fetchProfile} variant="default">
                        Thử lại
                    </Button>
                </div>
            </div>
        );
    }

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
                    <SidebarInset>
                        <SiteHeader />
                        <div className="flex flex-col flex-1">
                            <div className="@container/main flex flex-col flex-1 gap-2">
                                <div className="flex flex-col gap-4 md:gap-6 py-4 md:py-6">
                                    {children}
                                </div>
                            </div>
                        </div>
                        {/* <SiteFooter /> */}
                    </SidebarInset>
                </>
            ) : (
                <>
                    <SidebarInset>
                        <SiteHeader />
                        <div className="flex flex-col flex-1">
                            <div className="@container/main flex flex-col flex-1 gap-2">
                                <div className="flex flex-col gap-4 md:gap-6 py-4 md:py-6">
                                    {children}
                                </div>
                            </div>
                        </div>
                        {/* <SiteFooter /> */}
                    </SidebarInset>
                    <AppSidebar
                        variant={config.variant}
                        collapsible={config.collapsible}
                        side={config.side}
                    />
                </>
            )}

            {/* Theme Customizer */}
            {/* <ThemeCustomizerTrigger
                onClick={() => setThemeCustomizerOpen(true)}
            /> */}
            {/* <ThemeCustomizer
                open={themeCustomizerOpen}
                onOpenChange={setThemeCustomizerOpen}
            /> */}
            {/* <UpgradeToProButton /> */}
        </SidebarProvider>
    );
}
