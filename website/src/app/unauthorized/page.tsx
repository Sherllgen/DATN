"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useState } from "react";

export default function UnauthorizedPage() {
    const router = useRouter();
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            await fetch("/api/auth/logout", {
                method: "POST",
            });
            // Redirect to login after logout
            router.push("/sign-in-3");
        } catch (error) {
            console.error("Logout failed:", error);
            setIsLoggingOut(false);
        }
    };

    return (
        <div className="flex flex-col justify-center items-center p-6 min-h-screen">
            <div className="space-y-6 max-w-md text-center">
                <div className="text-7xl">🚫</div>
                <h1 className="font-bold text-4xl">Access Denied</h1>
                <p className="text-muted-foreground text-lg">
                    You do not have permission to access this page. Please contact
                    an administrator if you think this is a mistake.
                </p>
                <div className="flex justify-center gap-4 pt-4">
                    <Button asChild>
                        <Link href="/">Go to Home</Link>
                    </Button>
                    <Button
                        variant="outline"
                        onClick={handleLogout}
                        disabled={isLoggingOut}
                    >
                        {isLoggingOut ? "Logging out..." : "Logout"}
                    </Button>
                </div>
            </div>
        </div>
    );
}
