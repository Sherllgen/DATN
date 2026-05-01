import type { Metadata } from "next";

import "./globals.css";

import { ThemeProvider } from "@/components/theme-provider";
import { SidebarConfigProvider } from "@/contexts/sidebar-context";
import { inter } from "@/lib/fonts";

import { ReactQueryProvider } from "@/components/providers/query-provider";

export const metadata: Metadata = {
    title: "EV Management",
    description: "A dashboard built with Next.js and shadcn/ui",
};

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en" className={`${inter.variable} antialiased`}>
            <body className={inter.className}>
                <ReactQueryProvider>
                    <ThemeProvider
                        defaultTheme="system"
                        storageKey="nextjs-ui-theme"
                    >
                        <SidebarConfigProvider>{children}</SidebarConfigProvider>
                    </ThemeProvider>
                </ReactQueryProvider>
            </body>
        </html>
    );
}
