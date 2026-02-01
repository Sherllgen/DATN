"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import { toast } from "react-toastify";
import {
    Plus,
    Edit2,
    Trash2,
    MapPin,
    Zap,
    Power,
    PowerOff,
    Loader2,
    MoreVertical,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

interface Station {
    id: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    status: "ACTIVE" | "INACTIVE" | "MAINTENANCE" | "CLOSED" | "PENDING";
    totalPorts: number;
    availablePorts: number;
}

const statusVariantMap: Record<
    Station["status"],
    "default" | "secondary" | "destructive" | "outline"
> = {
    ACTIVE: "default",
    INACTIVE: "secondary",
    MAINTENANCE: "outline",
    CLOSED: "destructive",
    PENDING: "outline",
};

const statusLabelMap: Record<Station["status"], string> = {
    ACTIVE: "Hoạt động",
    INACTIVE: "Tạm ngưng",
    MAINTENANCE: "Bảo trì",
    CLOSED: "Đã đóng",
    PENDING: "Chờ duyệt",
};

export default function StationsPage() {
    const router = useRouter();
    const [stations, setStations] = useState<Station[]>([]);
    const [loading, setLoading] = useState(true);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [stationToDelete, setStationToDelete] = useState<Station | null>(null);
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        fetchStations();
    }, []);

    async function fetchStations() {
        try {
            setLoading(true);
            const response = await axios.get("/api/stations", {
                withCredentials: true,
            });

            if (response.data?.data) {
                setStations(response.data.data);
            }
        } catch (error) {
            console.error("Failed to fetch stations:", error);
            toast.error("Không thể tải danh sách trạm sạc");
        } finally {
            setLoading(false);
        }
    }

    async function handleToggleStatus(station: Station) {
        const newStatus = station.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
        try {
            await axios.patch(
                `/api/stations/${station.id}/status?status=${newStatus}`,
                {},
                { withCredentials: true }
            );
            toast.success(
                `Trạm ${station.name} đã ${newStatus === "ACTIVE" ? "kích hoạt" : "tạm ngưng"}`
            );
            fetchStations();
        } catch (error) {
            console.error("Failed to update status:", error);
            toast.error("Không thể cập nhật trạng thái");
        }
    }

    async function handleDelete() {
        if (!stationToDelete) return;

        try {
            setDeleting(true);
            await axios.delete(`/api/stations/${stationToDelete.id}`, {
                withCredentials: true,
            });
            toast.success(`Đã xóa trạm ${stationToDelete.name}`);
            setDeleteDialogOpen(false);
            setStationToDelete(null);
            fetchStations();
        } catch (error) {
            console.error("Failed to delete station:", error);
            toast.error("Không thể xóa trạm sạc");
        } finally {
            setDeleting(false);
        }
    }

    function confirmDelete(station: Station) {
        setStationToDelete(station);
        setDeleteDialogOpen(true);
    }

    if (loading) {
        return (
            <div className="px-4 lg:px-6 space-y-6">
                <div className="flex justify-between items-center">
                    <div>
                        <Skeleton className="h-8 w-48 mb-2" />
                        <Skeleton className="h-4 w-72" />
                    </div>
                    <Skeleton className="h-10 w-32" />
                </div>
                <Card>
                    <CardContent className="p-6">
                        <div className="space-y-4">
                            {[1, 2, 3].map((i) => (
                                <Skeleton key={i} className="h-16 w-full" />
                            ))}
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="px-4 lg:px-6 space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-bold">Trạm sạc của tôi</h1>
                    <p className="text-muted-foreground">
                        Quản lý các trạm sạc EV của bạn
                    </p>
                </div>
                <Button
                    onClick={() => router.push("/stations/create")}
                    className="cursor-pointer"
                >
                    <Plus className="mr-2 h-4 w-4" />
                    Thêm trạm sạc
                </Button>
            </div>

            {stations.length === 0 ? (
                <Card>
                    <CardContent className="flex flex-col items-center justify-center py-16">
                        <Zap className="h-16 w-16 text-muted-foreground mb-4" />
                        <h3 className="text-lg font-semibold mb-2">
                            Chưa có trạm sạc nào
                        </h3>
                        <p className="text-muted-foreground text-center mb-4">
                            Bắt đầu bằng cách thêm trạm sạc đầu tiên của bạn
                        </p>
                        <Button
                            onClick={() => router.push("/stations/create")}
                            className="cursor-pointer"
                        >
                            <Plus className="mr-2 h-4 w-4" />
                            Thêm trạm sạc
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <Card>
                    <CardHeader>
                        <CardTitle>Danh sách trạm sạc</CardTitle>
                        <CardDescription>
                            Bạn có {stations.length} trạm sạc
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Tên trạm</TableHead>
                                    <TableHead>Địa chỉ</TableHead>
                                    <TableHead>Trạng thái</TableHead>
                                    <TableHead className="text-center">
                                        Cổng sạc
                                    </TableHead>
                                    <TableHead className="text-right">
                                        Thao tác
                                    </TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {stations.map((station) => (
                                    <TableRow key={station.id}>
                                        <TableCell className="font-medium">
                                            {station.name}
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-1 text-muted-foreground">
                                                <MapPin className="h-4 w-4" />
                                                <span className="line-clamp-1">
                                                    {station.address}
                                                </span>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {station.status ? (
                                                <Badge
                                                    variant={
                                                        statusVariantMap[station.status] || "secondary"
                                                    }
                                                >
                                                    {statusLabelMap[station.status] || station.status}
                                                </Badge>
                                            ) : (
                                                <Badge variant="secondary">Chưa xác định</Badge>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-center">
                                            <span className="text-green-600 font-medium">
                                                {station.availablePorts}
                                            </span>
                                            <span className="text-muted-foreground">
                                                /{station.totalPorts}
                                            </span>
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="cursor-pointer"
                                                    >
                                                        <MoreVertical className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem
                                                        onClick={() =>
                                                            router.push(
                                                                `/stations/${station.id}/edit`
                                                            )
                                                        }
                                                        className="cursor-pointer"
                                                    >
                                                        <Edit2 className="mr-2 h-4 w-4" />
                                                        Chỉnh sửa
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        onClick={() =>
                                                            handleToggleStatus(station)
                                                        }
                                                        className="cursor-pointer"
                                                    >
                                                        {station.status === "ACTIVE" ? (
                                                            <>
                                                                <PowerOff className="mr-2 h-4 w-4" />
                                                                Tạm ngưng
                                                            </>
                                                        ) : (
                                                            <>
                                                                <Power className="mr-2 h-4 w-4" />
                                                                Kích hoạt
                                                            </>
                                                        )}
                                                    </DropdownMenuItem>
                                                    <DropdownMenuSeparator />
                                                    <DropdownMenuItem
                                                        onClick={() =>
                                                            confirmDelete(station)
                                                        }
                                                        className="text-destructive cursor-pointer"
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" />
                                                        Xóa trạm
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </CardContent>
                </Card>
            )}

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Xác nhận xóa trạm sạc?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Bạn có chắc chắn muốn xóa trạm{" "}
                            <strong>{stationToDelete?.name}</strong>? Hành động này
                            không thể hoàn tác.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">
                            Hủy
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={handleDelete}
                            disabled={deleting}
                            className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                        >
                            {deleting && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            Xóa
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
