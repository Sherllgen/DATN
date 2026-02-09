"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { Building2, Phone, CreditCard, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardHeader,
    CardDescription,
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
    FormDescription,
} from "@/components/ui/form";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { useUserStore } from "@/contexts/user.store";

const businessProfileSchema = z.object({
    businessName: z
        .string()
        .max(200, "Business name must not exceed 200 characters")
        .optional(),
    taxCode: z.string().max(20, "Tax code must not exceed 20 characters").optional(),
    bankAccount: z
        .string()
        .max(30, "Bank account must not exceed 30 characters")
        .optional(),
    bankName: z
        .string()
        .max(100, "Bank name must not exceed 100 characters")
        .optional(),
    contactPhone: z
        .string()
        .regex(/^(\+84|0)[0-9]{9}$/, "Invalid phone number format")
        .optional()
        .or(z.literal("")),
});

type BusinessProfileFormValues = z.infer<typeof businessProfileSchema>;

interface BusinessProfile {
    id: number;
    ownerType: "INDIVIDUAL" | "ENTERPRISE";
    fullName: string;
    idNumber: string;
    businessName: string | null;
    taxCode: string | null;
    contactEmail: string;
    contactPhone: string;
    bankAccount: string | null;
    bankName: string | null;
}

export default function BusinessProfilePage() {
    const user = useUserStore((state) => state.user);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [profile, setProfile] = useState<BusinessProfile | null>(null);
    const [error, setError] = useState<string | null>(null);

    const form = useForm<BusinessProfileFormValues>({
        resolver: zodResolver(businessProfileSchema),
        defaultValues: {
            businessName: "",
            taxCode: "",
            bankAccount: "",
            bankName: "",
            contactPhone: "",
        },
    });

    // Fetch business profile on mount
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setLoading(true);
                const response = await axios.get("/api/user/business-profile", {
                    withCredentials: true,
                });

                if (response.data?.data) {
                    const data = response.data.data as BusinessProfile;
                    setProfile(data);
                    form.reset({
                        businessName: data.businessName || "",
                        taxCode: data.taxCode || "",
                        bankAccount: data.bankAccount || "",
                        bankName: data.bankName || "",
                        contactPhone: data.contactPhone || "",
                    });
                }
            } catch (err: any) {
                console.error("Failed to fetch business profile:", err);
                if (err.response?.status === 404) {
                    setError(
                        "Business profile not found. Only approved Station Owners have this profile."
                    );
                } else {
                    setError("Failed to load business information.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [form]);

    async function onSubmit(data: BusinessProfileFormValues) {
        try {
            setSubmitting(true);
            const response = await axios.put("/api/user/business-profile", data, {
                withCredentials: true,
            });

            if (response.data?.data) {
                setProfile(response.data.data);
                toast.success("Business information updated successfully!");
            }
        } catch (err: any) {
            console.error("Failed to update business profile:", err);
            toast.error(
                err.response?.data?.message || "Update failed. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    }

    // If not a station owner role, show access denied
    if (user && user.role !== "STATION_OWNER") {
        return (
            <div className="px-4 lg:px-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Access Denied</CardTitle>
                        <CardDescription>
                            This page is only for Station Owners
                        </CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="px-4 lg:px-6 space-y-6">
                <div>
                    <Skeleton className="h-8 w-64 mb-2" />
                    <Skeleton className="h-4 w-96" />
                </div>
                <Card>
                    <CardHeader>
                        <Skeleton className="h-6 w-48" />
                        <Skeleton className="h-4 w-72" />
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="grid grid-cols-2 gap-6">
                            {[1, 2, 3, 4].map((i) => (
                                <div key={i} className="space-y-2">
                                    <Skeleton className="h-4 w-24" />
                                    <Skeleton className="h-10 w-full" />
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (error) {
        return (
            <div className="px-4 lg:px-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-destructive">Error</CardTitle>
                        <CardDescription>{error}</CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

    return (
        <div className="px-4 lg:px-6 space-y-6">
            <div>
                <h1 className="text-3xl font-bold">Business Profile</h1>
                <p className="text-muted-foreground">
                    Manage your business information and payment account
                </p>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    {/* Read-only Info */}
                    <Card>
                        <CardHeader>
                            <div className="flex items-center justify-between">
                                <div>
                                    <CardTitle className="flex items-center gap-2">
                                        <Building2 className="h-5 w-5" />
                                        Registration Information
                                    </CardTitle>
                                    <CardDescription>
                                        This information is retrieved from your registration profile (cannot be edited)
                                    </CardDescription>
                                </div>
                                <Badge
                                    variant={
                                        profile?.ownerType === "ENTERPRISE"
                                            ? "default"
                                            : "secondary"
                                    }
                                >
                                    {profile?.ownerType === "ENTERPRISE"
                                        ? "Enterprise"
                                        : "Individual"}
                                </Badge>
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="text-sm font-medium text-muted-foreground">
                                        Full Name
                                    </label>
                                    <p className="text-base">{profile?.fullName || "-"}</p>
                                </div>
                                <div>
                                    <label className="text-sm font-medium text-muted-foreground">
                                        Contact Email
                                    </label>
                                    <p className="text-base">{profile?.contactEmail || "-"}</p>
                                </div>
                                {profile?.idNumber && (
                                    <div>
                                        <label className="text-sm font-medium text-muted-foreground">
                                            ID Number / Citizen ID
                                        </label>
                                        <p className="text-base">{profile.idNumber}</p>
                                    </div>
                                )}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Editable Business Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Building2 className="h-5 w-5" />
                                Business Details
                            </CardTitle>
                            <CardDescription>
                                Update your business information
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="businessName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Business Name</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="Enter business name"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="taxCode"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Tax Code</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Enter tax code" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="contactPhone"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Contact Phone</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="0912345678"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormDescription>
                                                Format: +84 or 0 + 9 digits
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </CardContent>
                    </Card>

                    {/* Bank Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <CreditCard className="h-5 w-5" />
                                Banking Information
                            </CardTitle>
                            <CardDescription>
                                Account information for receiving payments from charging transactions
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="bankName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Bank Name</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="e.g. Vietcombank, MB Bank..."
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="bankAccount"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Account Number</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="Enter account number"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </CardContent>
                    </Card>

                    {/* Submit Button */}
                    <div className="flex justify-start gap-3">
                        <Button
                            type="submit"
                            disabled={submitting}
                            className="cursor-pointer"
                        >
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
        </div>
    );
}
