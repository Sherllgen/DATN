import { MenuIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
} from "@/components/ui/navigation-menu";

import { cn } from "@/lib/utils";

import Logo from "@/components/Landing-page/logo";
import Link from "next/link";

export type NavigationSection = {
    title: string;
    href: string;
};

type HeaderProps = {
    navigationData: NavigationSection[];
    className?: string;
};

const Header = ({ navigationData, className }: HeaderProps) => {
    return (
        <header
            className={cn(
                "top-0 z-50 sticky bg-background border-b h-16",
                className
            )}
        >
            <div className="flex justify-between items-center gap-6 mx-auto px-4 sm:px-6 lg:px-8 max-w-7xl h-full">
                {/* Logo */}
                <a href="#">
                    <Logo className="gap-3" />
                </a>

                {/* Navigation */}
                <NavigationMenu className="max-md:hidden">
                    <NavigationMenuList className="flex-wrap justify-start gap-0">
                        {navigationData.map((navItem) => (
                            <NavigationMenuItem key={navItem.title}>
                                <NavigationMenuLink
                                    href={navItem.href}
                                    className="hover:bg-transparent px-3 py-1.5 font-medium text-base! text-muted-foreground hover:text-primary"
                                >
                                    {navItem.title}
                                </NavigationMenuLink>
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>

                {/* Login Button */}
                <Button className="rounded-lg" asChild>
                    <Link href="/login">Login</Link>
                </Button>

                {/* Navigation for small screens */}
                <div className="md:hidden flex gap-4">
                    <Button className="rounded-lg" asChild>
                        <Link href="/login">Login</Link>
                    </Button>

                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline" size="icon">
                                <MenuIcon />
                                <span className="sr-only">Menu</span>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-56" align="end">
                            {navigationData.map((item, index) => (
                                <DropdownMenuItem key={index}>
                                    <a href={item.href}>{item.title}</a>
                                </DropdownMenuItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </header>
    );
};

export default Header;
