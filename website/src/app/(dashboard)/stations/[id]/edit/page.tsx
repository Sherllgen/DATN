"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import axios from "axios";
import { toast } from "react-toastify";
import { ArrowLeft, Loader2, MapPin } from "lucide-react";

import { Button } from "@/components/ui/button";
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
    FormDescription,
} from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import { Skeleton } from "@/components/ui/skeleton";

const stationSchema = z.object({
    name: z.string().min(1, "Tên trạm là bắt buộc").max(200),
    address: z.string().min(1, "Địa chỉ là bắt buộc").max(500),
    latitude: z.string().min(1, "Vĩ độ là bắt buộc"),
    longitude: z.string().min(1, "Kinh độ là bắt buộc"),
    description: z.string().max(1000).optional(),
});

type StationFormValues = z.infer<typeof stationSchema>;

export default function EditStationPage() {
    const router = useRouter();
    const params = useParams();
    const stationId = params.id as string;

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const form = useForm<StationFormValues>({
        resolver: zodResolver(stationSchema),
        defaultValues: {
            name: "",
            address: "",
            latitude: "",
            longitude: "",
            description: "",
        },
    });

    useEffect(() => {
        async function fetchStation() {
            try {
                setLoading(true);
                const response = await axios.get(`/api/stations/${stationId}`, {
                    withCredentials: true,
                });

                if (response.data?.data) {
                    const station = response.data.data;
                    form.reset({
                        name: station.name || "",
                        address: station.address || "",
                        latitude: String(station.latitude || 0),
                        longitude: String(station.longitude || 0),
                        description: station.description || "",
                    });
                }
            } catch (error) {
                console.error("Failed to fetch station:", error);
                toast.error("Không thể tải thông tin trạm sạc");
                router.push("/stations");
            } finally {
                setLoading(false);
            }
        }

        if (stationId) {
            fetchStation();
        }
    }, [stationId, form, router]);

    async function onSubmit(data: StationFormValues) {
        try {
            setSubmitting(true);
            await axios.put(`/api/stations/${stationId}`, {
                ...data,
                latitude: parseFloat(data.latitude),
                longitude: parseFloat(data.longitude),
            }, {
                withCredentials: true,
            });

            toast.success("Cập nhật trạm sạc thành công!");
            router.push("/stations");
        } catch (error: any) {
            console.error("Failed to update station:", error);
            toast.error(
                error.response?.data?.message ||
                    "Không thể cập nhật trạm sạc. Vui lòng thử lại."
            );
        } finally {
            setSubmitting(false);
        }
    }

    if (loading) {
        return (
            <div className="px-4 lg:px-6 space-y-6">
                <div className="flex items-center gap-4">
                    <Skeleton className="h-10 w-10 rounded" />
                    <div>
                        <Skeleton className="h-8 w-64 mb-2" />
                        <Skeleton className="h-4 w-48" />
                    </div>
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
        <div className="px-4 lg:px-6 space-y-6">
            <div className="flex items-center gap-4">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => router.back()}
                    className="cursor-pointer"
                >
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold">Chỉnh sửa trạm sạc</h1>
                    <p className="text-muted-foreground">
                        Cập nhật thông tin trạm sạc
                    </p>
                </div>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Thông tin cơ bản</CardTitle>
                            <CardDescription>
                                Cập nhật thông tin cơ bản của trạm sạc
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Tên trạm sạc *</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="VD: Trạm sạc EV-Go Quận 1"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <FormField
                                control={form.control}
                                name="address"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Địa chỉ *</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="VD: 123 Nguyễn Huệ, Quận 1, TP.HCM"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <FormField
                                control={form.control}
                                name="description"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Mô tả</FormLabel>
                                        <FormControl>
                                            <Textarea
                                                placeholder="Mô tả về trạm sạc, hướng dẫn tìm đường..."
                                                className="min-h-[100px]"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <MapPin className="h-5 w-5" />
                                Vị trí trên bản đồ
                            </CardTitle>
                            <CardDescription>
                                Cập nhật tọa độ GPS của trạm sạc
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="latitude"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Vĩ độ (Latitude) *</FormLabel>
                                            <FormControl>
                                                <Input
                                                    type="number"
                                                    step="any"
                                                    placeholder="VD: 10.7769"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormDescription>
                                                Giá trị từ -90 đến 90
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="longitude"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Kinh độ (Longitude) *</FormLabel>
                                            <FormControl>
                                                <Input
                                                    type="number"
                                                    step="any"
                                                    placeholder="VD: 106.7009"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormDescription>
                                                Giá trị từ -180 đến 180
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </CardContent>
                    </Card>

                    <div className="flex justify-start gap-3">
                        <Button
                            type="submit"
                            disabled={submitting}
                            className="cursor-pointer"
                        >
                            {submitting && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            Lưu thay đổi
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => router.back()}
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
