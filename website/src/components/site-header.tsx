"use client";

import * as React from "react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { CommandSearch, SearchTrigger } from "@/components/command-search";
import { ModeToggle } from "@/components/mode-toggle";

export function SiteHeader() {
    const [searchOpen, setSearchOpen] = React.useState(false);

    React.useEffect(() => {
        const down = (e: KeyboardEvent) => {
            if (e.key === "k" && (e.metaKey || e.ctrlKey)) {
                e.preventDefault();
                setSearchOpen((open) => !open);
            }
        };

        document.addEventListener("keydown", down);
        return () => document.removeEventListener("keydown", down);
    }, []);

    return (
        <>
            <header className="flex h-(--header-height) shrink-0 items-center gap-2 border-b transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-(--header-height)">
                <div className="flex items-center gap-1 lg:gap-2 px-4 lg:px-6 py-3 w-full">
                    <SidebarTrigger className="-ml-1" />
                </div>
            </header>
        </>
    );
}
