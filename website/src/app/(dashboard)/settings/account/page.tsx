"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { Loader2, User } from "lucide-react";

import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";

const accountFormSchema = z.object({
    fullName: z.string().min(1, "Full name is required").max(100, "Name is too long"),
    email: z.string().email("Invalid email address"),
    phoneNumber: z
        .string()
        .regex(/^(\+84|0)[0-9]{9}$/, "Invalid phone number format (e.g., 0912345678)")
        .optional()
        .or(z.literal("")),
});

const passwordFormSchema = z.object({
    currentPassword: z.string().min(1, "Current password is required"),
    newPassword: z.string().min(8, "Password must be at least 8 characters"),
    confirmPassword: z.string().min(1, "Please confirm your password"),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
});

type AccountFormValues = z.infer<typeof accountFormSchema>;
type PasswordFormValues = z.infer<typeof passwordFormSchema>;

interface UserProfile {
    id: number;
    email: string;
    fullName: string | null;
    phoneNumber: string | null;
    role: string;
    status: string;
}

export default function AccountSettings() {
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [changingPassword, setChangingPassword] = useState(false);
    const [profile, setProfile] = useState<UserProfile | null>(null);

    const form = useForm<AccountFormValues>({
        resolver: zodResolver(accountFormSchema),
        defaultValues: {
            fullName: "",
            email: "",
            phoneNumber: "",
        },
    });

    const passwordForm = useForm<PasswordFormValues>({
        resolver: zodResolver(passwordFormSchema),
        defaultValues: {
            currentPassword: "",
            newPassword: "",
            confirmPassword: "",
        },
    });

    // Fetch profile on mount
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setLoading(true);
                const response = await axios.get("/api/user/profile", {
                    withCredentials: true,
                });

                if (response.data?.data) {
                    const data = response.data.data as UserProfile;
                    setProfile(data);
                    form.reset({
                        fullName: data.fullName || "",
                        email: data.email || "",
                        phoneNumber: data.phoneNumber || "",
                    });
                }
            } catch (error) {
                console.error("Failed to fetch profile:", error);
                toast.error("Failed to load profile");
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [form]);

    async function onSubmit(data: AccountFormValues) {
        try {
            setSubmitting(true);
            const response = await axios.put("/api/user/profile", {
                fullName: data.fullName,
                phoneNumber: data.phoneNumber || null,
            }, { withCredentials: true });

            if (response.data?.data) {
                setProfile(response.data.data);
                toast.success("Profile updated successfully!");
            }
        } catch (error: any) {
            console.error("Failed to update profile:", error);
            toast.error(error.response?.data?.message || "Failed to update profile");
        } finally {
            setSubmitting(false);
        }
    }

    async function onPasswordSubmit(data: PasswordFormValues) {
        try {
            setChangingPassword(true);
            await axios.put("/api/user/change-password", {
                currentPassword: data.currentPassword,
                newPassword: data.newPassword,
                confirmPassword: data.confirmPassword,
            }, { withCredentials: true });

            toast.success("Password changed successfully!");
            passwordForm.reset();
        } catch (error: any) {
            console.error("Failed to change password:", error);
            toast.error(error.response?.data?.message || "Failed to change password");
        } finally {
            setChangingPassword(false);
        }
    }

    if (loading) {
        return (
            <div className="space-y-6 px-4 lg:px-6">
                <div>
                    <Skeleton className="h-8 w-64 mb-2" />
                    <Skeleton className="h-4 w-96" />
                </div>
                <Card>
                    <CardHeader>
                        <Skeleton className="h-6 w-48" />
                        <Skeleton className="h-4 w-72" />
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {[1, 2, 3].map((i) => (
                            <div key={i} className="space-y-2">
                                <Skeleton className="h-4 w-24" />
                                <Skeleton className="h-10 w-full" />
                            </div>
                        ))}
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="space-y-6 px-4 lg:px-6">
            <div>
                <h1 className="text-3xl font-bold flex items-center gap-2">
                    <User className="h-8 w-8" />
                    Account Settings
                </h1>
                <p className="text-muted-foreground">
                    Manage your account settings and preferences.
                </p>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Personal Information</CardTitle>
                            <CardDescription>
                                Update your personal information that will be displayed on your profile.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FormField
                                control={form.control}
                                name="fullName"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Full Name</FormLabel>
                                        <FormControl>
                                            <Input placeholder="Enter your full name" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="email"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Email Address</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="email"
                                                placeholder="Enter your email"
                                                {...field}
                                                disabled
                                            />
                                        </FormControl>
                                        <FormMessage />
                                        <p className="text-xs text-muted-foreground">
                                            Email cannot be changed.
                                        </p>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="phoneNumber"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Phone Number</FormLabel>
                                        <FormControl>
                                            <Input placeholder="0912345678" {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </CardContent>
                    </Card>

                    <div className="flex space-x-2">
                        <Button type="submit" disabled={submitting} className="cursor-pointer">
                            {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Save Changes
                        </Button>
                        <Button
                            variant="outline"
                            type="button"
                            onClick={() => form.reset()}
                            className="cursor-pointer"
                        >
                            Cancel
                        </Button>
                    </div>
                </form>
            </Form>

            <Form {...passwordForm}>
                <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Change Password</CardTitle>
                            <CardDescription>
                                Update your password to keep your account secure.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FormField
                                control={passwordForm.control}
                                name="currentPassword"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Current Password</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="password"
                                                placeholder="Enter current password"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={passwordForm.control}
                                name="newPassword"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>New Password</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="password"
                                                placeholder="Enter new password"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={passwordForm.control}
                                name="confirmPassword"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Confirm New Password</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="password"
                                                placeholder="Confirm new password"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </CardContent>
                    </Card>

                    <Button type="submit" disabled={changingPassword} className="cursor-pointer">
                        {changingPassword && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Change Password
                    </Button>
                </form>
            </Form>

            <Card>
                <CardHeader>
                    <CardTitle>Danger Zone</CardTitle>
                    <CardDescription>
                        Irreversible and destructive actions.
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    <Separator />
                    <div className="flex flex-wrap gap-2 items-center justify-between">
                        <div>
                            <h4 className="font-semibold">Delete Account</h4>
                            <p className="text-sm text-muted-foreground">
                                Permanently delete your account and all associated data.
                            </p>
                        </div>
                        <Button variant="destructive" type="button" className="cursor-pointer">
                            Delete Account
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
