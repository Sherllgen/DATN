"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/contexts/user.store";
import { UserRole } from "@/types/user";

export default function HomePage() {
    const router = useRouter();
    const user = useUserStore((s) => s.user);

    useEffect(() => {
        if (user?.role === UserRole.ADMIN) {
            router.replace("/admin/accounts");
        } else {
            router.replace("/dashboard");
        }
    }, [router, user]);

    // Show a loading state while redirecting
    return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-center">
                <div className="mx-auto border-primary border-b-2 rounded-full w-8 h-8 animate-spin"></div>
                <p className="mt-2 text-muted-foreground">
                    Redirecting...
                </p>
            </div>
        </div>
    );
}
