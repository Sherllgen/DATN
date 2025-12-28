"use client";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Logo } from "@/components/logo";
import Link from "next/link";
import Image from "next/image";
import { loginAction } from "../actions";
import { useActionState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/contexts/user.store";

export function LoginForm3({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const router = useRouter();
    const formRef = useRef<HTMLFormElement>(null);
    const setUser = useUserStore((state) => state.setUser);
    const [state, formAction, isPending] = useActionState(
        async (prevState: any, formData: FormData) => {
            const result = await loginAction(formData);
            return result;
        },
        { success: false, message: "" }
    );

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card className="p-0 overflow-hidden">
                <CardContent className="grid md:grid-cols-2 p-0">
                    <form
                        ref={formRef}
                        className="p-6 md:p-8"
                        action={formAction}
                    >
                        <div className="flex flex-col gap-6 pb-6">
                            <div className="flex justify-center mb-2">
                                <Link
                                    href="/"
                                    className="flex items-center gap-2 font-medium"
                                >
                                    {/* <div className="flex justify-center items-center bg-primary rounded-md size-8 text-primary-foreground">
                                        <Logo size={24} />
                                    </div> */}
                                    {/* <span className="text-xl">
                                        EV Management
                                    </span> */}
                                </Link>
                            </div>
                            <div className="flex flex-col items-center text-center">
                                <h1 className="font-bold text-2xl">
                                    Welcome back
                                </h1>
                                <p className="text-muted-foreground text-balance">
                                    Login to your EV Management account
                                </p>
                            </div>
                            <div className="gap-3 grid">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    name="email"
                                    type="email"
                                    placeholder="test@example.com"
                                    tabIndex={1}
                                    required
                                    disabled={isPending}
                                />
                            </div>
                            <div className="gap-3 grid">
                                <div className="flex items-center">
                                    <Label htmlFor="password">Password</Label>
                                    <Link
                                        href="#"
                                        className="ml-auto text-sm hover:underline underline-offset-2"
                                    >
                                        Forgot your password?
                                    </Link>
                                </div>
                                <Input
                                    id="password"
                                    name="password"
                                    type="password"
                                    placeholder="********"
                                    tabIndex={2}
                                    required
                                    disabled={isPending}
                                />
                            </div>

                            {state.message && (
                                <div
                                    className={cn(
                                        "p-3 rounded-md text-sm",
                                        state.success
                                            ? "bg-green-50 text-green-800 dark:bg-green-900/20 dark:text-green-400"
                                            : "bg-red-50 text-red-800 dark:bg-red-900/20 dark:text-red-400"
                                    )}
                                >
                                    {state.message}
                                </div>
                            )}

                            <Button
                                type="submit"
                                className="w-full cursor-pointer"
                                disabled={isPending}
                            >
                                {isPending ? "Logging in..." : "Login"}
                            </Button>
                        </div>
                    </form>
                    <div className="hidden md:block relative bg-muted">
                        <Image
                            src="https://ui.shadcn.com/placeholder.svg"
                            alt="Image"
                            fill
                            className="dark:brightness-[0.95] dark:invert object-cover"
                        />
                    </div>
                </CardContent>
            </Card>
            <div className="text-muted-foreground *:[a]:hover:text-primary text-xs text-center *:[a]:underline *:[a]:underline-offset-4 text-balance">
                By clicking continue, you agree to our{" "}
                <a href="#">Terms of Service</a> and{" "}
                <a href="#">Privacy Policy</a>.
            </div>
        </div>
    );
}
