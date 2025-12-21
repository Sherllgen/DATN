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

export default function LoginPage() {
    return (
        <div className="grid lg:grid-cols-2 min-h-svh">
            <div className="flex flex-col gap-4 p-6 md:p-10">
                <div className="flex flex-1 justify-center items-center">
                    <div className="shadow-lg px-6 py-8 border border-gray-200 rounded-xl w-full max-w-sm">
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
                                        tabIndex={1}
                                        required
                                    />
                                </Field>
                                <Field>
                                    <div className="flex items-center">
                                        <FieldLabel htmlFor="password">
                                            Password
                                        </FieldLabel>
                                        <a
                                            href="#"
                                            className="ml-auto text-sm hover:underline underline-offset-4"
                                        >
                                            Forgot your password?
                                        </a>
                                    </div>
                                    <Input
                                        id="password"
                                        type="password"
                                        placeholder="********"
                                        tabIndex={2}
                                        required
                                    />
                                </Field>
                                <Field>
                                    <Button type="submit">Login</Button>
                                </Field>
                            </FieldGroup>
                        </form>
                    </div>
                </div>
            </div>
            <div className="hidden relative lg:flex justify-center items-center overflow-hidden">
                <img
                    src="/images/bannerLogin.png"
                    alt="Image"
                    className="top-1/2 left-1/2 absolute w-full object-cover -translate-x-1/2 -translate-y-1/2"
                />
            </div>
        </div>
    );
}
