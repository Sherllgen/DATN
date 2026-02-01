"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
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

const stationSchema = z.object({
    name: z.string().min(1, "Tên trạm là bắt buộc").max(200),
    address: z.string().min(1, "Địa chỉ là bắt buộc").max(500),
    latitude: z.string().min(1, "Vĩ độ là bắt buộc"),
    longitude: z.string().min(1, "Kinh độ là bắt buộc"),
    description: z.string().max(1000).optional(),
});

type StationFormValues = z.infer<typeof stationSchema>;

export default function CreateStationPage() {
    const router = useRouter();
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

    async function onSubmit(data: StationFormValues) {
        try {
            setSubmitting(true);
            await axios.post("/api/stations", {
                ...data,
                latitude: parseFloat(data.latitude),
                longitude: parseFloat(data.longitude),
            }, {
                withCredentials: true,
            });

            toast.success("Tạo trạm sạc thành công!");
            router.push("/stations");
        } catch (error: any) {
            console.error("Failed to create station:", error);
            toast.error(
                error.response?.data?.message || "Không thể tạo trạm sạc. Vui lòng thử lại."
            );
        } finally {
            setSubmitting(false);
        }
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
                    <h1 className="text-3xl font-bold">Thêm trạm sạc mới</h1>
                    <p className="text-muted-foreground">
                        Điền thông tin để tạo trạm sạc mới
                    </p>
                </div>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Thông tin cơ bản</CardTitle>
                            <CardDescription>
                                Nhập thông tin cơ bản của trạm sạc
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
                                Nhập tọa độ GPS của trạm sạc
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
                            Tạo trạm sạc
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
