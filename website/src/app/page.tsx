"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function HomePage() {
    const router = useRouter();

    useEffect(() => {
        router.replace("/dashboard");
    }, [router]);

    // Show a loading state while redirecting
    return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-center">
                <div className="mx-auto border-primary border-b-2 rounded-full w-8 h-8 animate-spin"></div>
                <p className="mt-2 text-muted-foreground">
                    Redirecting to dashboard...
                </p>
            </div>
        </div>
    );
}
