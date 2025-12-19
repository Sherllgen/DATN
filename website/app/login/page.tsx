import { Button } from "@/components/ui/button";
import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldSeparator,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import { GalleryVerticalEnd } from "lucide-react";
import Link from "next/link";

export default function LoginPage() {
    return (
        <div className="grid lg:grid-cols-2 min-h-svh">
            <div className="flex flex-col gap-4 p-6 md:p-10">
                <div className="flex flex-1 justify-center items-center">
                    <div className="w-full max-w-xs">
                        <form className={cn("flex flex-col gap-6")}>
                            <FieldGroup>
                                <div className="flex flex-col items-center gap-1 text-center">
                                    <h1 className="font-bold text-2xl">
                                        Login to your account
                                    </h1>
                                    <p className="text-muted-foreground text-sm text-balance">
                                        Enter your email below to login to your
                                        account
                                    </p>
                                </div>
                                <Field>
                                    <FieldLabel htmlFor="email">
                                        Email
                                    </FieldLabel>
                                    <Input
                                        id="email"
                                        type="email"
                                        placeholder="m@example.com"
                                        required
                                    />
                                </Field>
                                <Field>
                                    <div className="flex items-center">
                                        <FieldLabel htmlFor="password">
                                            Password
                                        </FieldLabel>
                                    </div>
                                    <Input
                                        id="password"
                                        type="password"
                                        required
                                    />
                                </Field>
                                <Button
                                    type="button"
                                    variant="link"
                                    size={"sm"}
                                    className="self-end"
                                >
                                    <Link href="/forgot-password">
                                        Quên mật khẩu?
                                    </Link>
                                </Button>
                                <FieldSeparator />
                                <Field>
                                    <Button type="submit">Login</Button>
                                </Field>
                            </FieldGroup>
                        </form>
                    </div>
                </div>
            </div>
            <div className="hidden lg:block relative bg-muted">
                <img
                    src="/placeholder.svg"
                    alt="Image"
                    className="absolute inset-0 dark:brightness-[0.2] dark:grayscale w-full h-full object-cover"
                />
            </div>
        </div>
    );
}
