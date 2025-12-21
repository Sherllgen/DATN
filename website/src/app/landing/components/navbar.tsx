"use client";

import { useState } from "react";
import Link from "next/link";
import {
    Menu,
    Github,
    LayoutDashboard,
    ChevronDown,
    X,
    Moon,
    Sun,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    NavigationMenuTrigger,
} from "@/components/ui/navigation-menu";
import {
    Sheet,
    SheetContent,
    SheetTrigger,
    SheetHeader,
    SheetTitle,
} from "@/components/ui/sheet";
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Logo } from "@/components/logo";
import { MegaMenu } from "@/components/landing/mega-menu";
import { ModeToggle } from "@/components/mode-toggle";
import { useTheme } from "@/hooks/use-theme";

const navigationItems = [
    { name: "Home", href: "/landing" },
    { name: "Features", href: "#features" },
    // { name: "Solutions", href: "#features", hasMegaMenu: true },
    // { name: "Team", href: "#team" },
    { name: "Pricing", href: "#pricing" },
    { name: "FAQ", href: "#faq" },
    { name: "Contact", href: "#contact" },
];

// Solutions menu items for mobile
const solutionsItems = [
    { title: "Browse Products" },
    { name: "Free Blocks", href: "#free-blocks" },
    { name: "Premium Templates", href: "#premium-templates" },
    { name: "Admin Dashboards", href: "#admin-dashboards" },
    { name: "Landing Pages", href: "#landing-pages" },
    { title: "Categories" },
    { name: "E-commerce", href: "#ecommerce" },
    { name: "SaaS Dashboards", href: "#saas-dashboards" },
    { name: "Analytics", href: "#analytics" },
    { name: "Authentication", href: "#authentication" },
    { title: "Resources" },
    { name: "Documentation", href: "#docs" },
    { name: "Component Showcase", href: "#showcase" },
    { name: "GitHub Repository", href: "#github" },
    { name: "Design System", href: "#design-system" },
];

// Smooth scroll function
const smoothScrollTo = (targetId: string) => {
    if (targetId.startsWith("#")) {
        const element = document.querySelector(targetId);
        if (element) {
            element.scrollIntoView({
                behavior: "smooth",
                block: "start",
            });
        }
    }
};

export function LandingNavbar() {
    const [isOpen, setIsOpen] = useState(false);
    const [solutionsOpen, setSolutionsOpen] = useState(false);
    const { setTheme, theme } = useTheme();

    return (
        <header className="top-0 z-50 sticky bg-background/80 supports-backdrop-filter:bg-background/60 backdrop-blur-xl border-b w-full">
            <div className="flex justify-between items-center mx-auto px-4 sm:px-6 lg:px-8 h-16 container">
                {/* Logo */}
                <div className="flex items-center space-x-2">
                    <Link
                        href="/landing"
                        className="flex items-center space-x-2 cursor-pointer"
                    >
                        {/* <Logo size={32} /> */}
                        <span className="ml-6 font-bold">EV Management</span>
                    </Link>
                </div>

                {/* Desktop Navigation */}
                <NavigationMenu className="hidden xl:flex">
                    <NavigationMenuList>
                        {navigationItems.map((item: any) => (
                            <NavigationMenuItem key={item.name}>
                                {item.hasMegaMenu ? (
                                    <>
                                        <NavigationMenuTrigger className="bg-transparent data-[state=open]:bg-transparent data-active:bg-transparent hover:bg-transparent focus:bg-transparent px-4 py-2 font-medium hover:text-primary focus:text-primary text-sm transition-colors cursor-pointer">
                                            {item.name}
                                        </NavigationMenuTrigger>
                                        <NavigationMenuContent>
                                            <MegaMenu />
                                        </NavigationMenuContent>
                                    </>
                                ) : (
                                    <NavigationMenuLink
                                        className="group inline-flex justify-center items-center px-4 py-2 focus:outline-none w-max h-10 font-medium hover:text-primary focus:text-primary text-sm transition-colors cursor-pointer"
                                        onClick={(e: React.MouseEvent) => {
                                            e.preventDefault();
                                            if (item.href.startsWith("#")) {
                                                smoothScrollTo(item.href);
                                            } else {
                                                window.location.href =
                                                    item.href;
                                            }
                                        }}
                                    >
                                        {item.name}
                                    </NavigationMenuLink>
                                )}
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>

                {/* Desktop CTA */}
                <div className="hidden xl:flex items-center space-x-2">
                    <Button variant="ghost" asChild className="cursor-pointer">
                        <Link href="/auth/sign-in">Sign In</Link>
                    </Button>
                    <Button asChild className="cursor-pointer">
                        <Link href="/register">Get Started</Link>
                    </Button>
                </div>

                {/* Mobile Menu */}
                <Sheet open={isOpen} onOpenChange={setIsOpen}>
                    <SheetTrigger asChild className="xl:hidden">
                        <Button
                            variant="ghost"
                            size="icon"
                            className="cursor-pointer"
                        >
                            <Menu className="w-5 h-5" />
                            <span className="sr-only">Toggle menu</span>
                        </Button>
                    </SheetTrigger>
                    <SheetContent
                        side="right"
                        className="[&>button]:hidden flex flex-col gap-0 p-0 w-full sm:w-[400px] overflow-hidden"
                    >
                        <div className="flex flex-col h-full">
                            {/* Header */}
                            <SheetHeader className="space-y-0 p-4 pb-2 border-b">
                                <div className="flex items-center gap-2">
                                    <div className="bg-primary/10 p-2 rounded-lg">
                                        <Logo size={16} />
                                    </div>
                                    <SheetTitle className="font-semibold text-lg">
                                        ShadcnStore
                                    </SheetTitle>
                                    <div className="flex items-center gap-2 ml-auto">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() =>
                                                setTheme(
                                                    theme === "light"
                                                        ? "dark"
                                                        : "light"
                                                )
                                            }
                                            className="w-8 h-8 cursor-pointer"
                                        >
                                            <Moon className="w-4 h-4 rotate-0 dark:-rotate-90 scale-100 dark:scale-0 transition-all" />
                                            <Sun className="absolute w-4 h-4 rotate-90 dark:rotate-0 scale-0 dark:scale-100 transition-all" />
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            asChild
                                            className="w-8 h-8 cursor-pointer"
                                        >
                                            <a
                                                href="https://github.com/silicondeck/shadcn-dashboard-landing-template"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                aria-label="GitHub Repository"
                                            >
                                                <Github className="w-4 h-4" />
                                            </a>
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => setIsOpen(false)}
                                            className="w-8 h-8 cursor-pointer"
                                        >
                                            <X className="w-4 h-4" />
                                        </Button>
                                    </div>
                                </div>
                            </SheetHeader>

                            {/* Navigation Links */}
                            <div className="flex-1 overflow-y-auto">
                                <nav className="space-y-1 p-6">
                                    {navigationItems.map((item: any) => (
                                        <div key={item.name}>
                                            {item.hasMegaMenu ? (
                                                <Collapsible
                                                    open={solutionsOpen}
                                                    onOpenChange={
                                                        setSolutionsOpen
                                                    }
                                                >
                                                    <CollapsibleTrigger className="flex justify-between items-center hover:bg-accent px-4 py-3 rounded-lg w-full font-medium text-base transition-colors hover:text-accent-foreground cursor-pointer">
                                                        {item.name}
                                                        <ChevronDown
                                                            className={`h-4 w-4 transition-transform ${
                                                                solutionsOpen
                                                                    ? "rotate-180"
                                                                    : ""
                                                            }`}
                                                        />
                                                    </CollapsibleTrigger>
                                                    <CollapsibleContent className="space-y-1 pl-4">
                                                        {solutionsItems.map(
                                                            (solution, index) =>
                                                                solution.title ? (
                                                                    <div
                                                                        key={`title-${index}`}
                                                                        className="mt-5 px-4 py-2 font-semibold text-muted-foreground/50 text-xs uppercase tracking-wider"
                                                                    >
                                                                        {
                                                                            solution.title
                                                                        }
                                                                    </div>
                                                                ) : (
                                                                    <a
                                                                        key={
                                                                            solution.name
                                                                        }
                                                                        href={
                                                                            solution.href
                                                                        }
                                                                        className="flex items-center hover:bg-accent px-4 py-2 rounded-lg text-sm transition-colors hover:text-accent-foreground cursor-pointer"
                                                                        onClick={(
                                                                            e
                                                                        ) => {
                                                                            setIsOpen(
                                                                                false
                                                                            );
                                                                            if (
                                                                                solution.href?.startsWith(
                                                                                    "#"
                                                                                )
                                                                            ) {
                                                                                e.preventDefault();
                                                                                setTimeout(
                                                                                    () =>
                                                                                        smoothScrollTo(
                                                                                            solution.href
                                                                                        ),
                                                                                    100
                                                                                );
                                                                            }
                                                                        }}
                                                                    >
                                                                        {
                                                                            solution.name
                                                                        }
                                                                    </a>
                                                                )
                                                        )}
                                                    </CollapsibleContent>
                                                </Collapsible>
                                            ) : (
                                                <a
                                                    href={item.href}
                                                    className="flex items-center hover:bg-accent px-4 py-3 rounded-lg font-medium text-base transition-colors hover:text-accent-foreground cursor-pointer"
                                                    onClick={(e) => {
                                                        setIsOpen(false);
                                                        if (
                                                            item.href.startsWith(
                                                                "#"
                                                            )
                                                        ) {
                                                            e.preventDefault();
                                                            setTimeout(
                                                                () =>
                                                                    smoothScrollTo(
                                                                        item.href
                                                                    ),
                                                                100
                                                            );
                                                        }
                                                    }}
                                                >
                                                    {item.name}
                                                </a>
                                            )}
                                        </div>
                                    ))}
                                </nav>
                            </div>

                            {/* Footer Actions */}
                            <div className="space-y-4 p-6 border-t">
                                {/* Primary Actions */}
                                <div className="space-y-3">
                                    <Button
                                        variant="outline"
                                        size="lg"
                                        asChild
                                        className="w-full cursor-pointer"
                                    >
                                        <Link href="/dashboard">
                                            <LayoutDashboard className="size-4" />
                                            Dashboard
                                        </Link>
                                    </Button>

                                    <div className="gap-3 grid grid-cols-2">
                                        <Button
                                            variant="outline"
                                            size="lg"
                                            asChild
                                            className="cursor-pointer"
                                        >
                                            <Link href="/auth/sign-in">
                                                Sign In
                                            </Link>
                                        </Button>
                                        <Button
                                            asChild
                                            size="lg"
                                            className="cursor-pointer"
                                        >
                                            <Link href="/auth/sign-up">
                                                Get Started
                                            </Link>
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </SheetContent>
                </Sheet>
            </div>
        </header>
    );
}
