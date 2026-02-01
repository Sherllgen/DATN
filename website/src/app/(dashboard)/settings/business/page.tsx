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
                        "Không tìm thấy hồ sơ kinh doanh. Chỉ Station Owner đã được phê duyệt mới có hồ sơ này."
                    );
                } else {
                    setError("Không thể tải thông tin kinh doanh.");
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
                toast.success("Cập nhật thông tin kinh doanh thành công!");
            }
        } catch (err: any) {
            console.error("Failed to update business profile:", err);
            toast.error(
                err.response?.data?.message || "Cập nhật thất bại. Vui lòng thử lại."
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
                        <CardTitle>Truy cập bị từ chối</CardTitle>
                        <CardDescription>
                            Trang này chỉ dành cho Station Owner
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
                        <CardTitle className="text-destructive">Lỗi</CardTitle>
                        <CardDescription>{error}</CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

    return (
        <div className="px-4 lg:px-6 space-y-6">
            <div>
                <h1 className="text-3xl font-bold">Thông tin kinh doanh</h1>
                <p className="text-muted-foreground">
                    Quản lý thông tin kinh doanh và tài khoản thanh toán của bạn
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
                                        Thông tin đăng ký
                                    </CardTitle>
                                    <CardDescription>
                                        Thông tin này được lấy từ hồ sơ đăng ký (không thể
                                        chỉnh sửa)
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
                                        ? "Doanh nghiệp"
                                        : "Cá nhân"}
                                </Badge>
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="text-sm font-medium text-muted-foreground">
                                        Họ và tên
                                    </label>
                                    <p className="text-base">{profile?.fullName || "-"}</p>
                                </div>
                                <div>
                                    <label className="text-sm font-medium text-muted-foreground">
                                        Email liên hệ
                                    </label>
                                    <p className="text-base">{profile?.contactEmail || "-"}</p>
                                </div>
                                {profile?.idNumber && (
                                    <div>
                                        <label className="text-sm font-medium text-muted-foreground">
                                            CCCD/CMND
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
                                Thông tin doanh nghiệp
                            </CardTitle>
                            <CardDescription>
                                Cập nhật thông tin doanh nghiệp của bạn
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="businessName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Tên doanh nghiệp</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="Nhập tên doanh nghiệp"
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
                                            <FormLabel>Mã số thuế</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Nhập mã số thuế" {...field} />
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
                                            <FormLabel>Số điện thoại liên hệ</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="0912345678"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormDescription>
                                                Định dạng: +84 hoặc 0 + 9 số
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
                                Thông tin ngân hàng
                            </CardTitle>
                            <CardDescription>
                                Thông tin tài khoản nhận thanh toán từ các giao dịch sạc
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="bankName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Tên ngân hàng</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="VD: Vietcombank, MB Bank..."
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
                                            <FormLabel>Số tài khoản</FormLabel>
                                            <FormControl>
                                                <Input
                                                    placeholder="Nhập số tài khoản"
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
                            Lưu thay đổi
                        </Button>
                        <Button
                            variant="outline"
                            type="button"
                            onClick={() => form.reset()}
                            className="cursor-pointer"
                        >
                            Hủy
                        </Button>
                    </div>
                </form>
            </Form>
        </div>
    );
}
