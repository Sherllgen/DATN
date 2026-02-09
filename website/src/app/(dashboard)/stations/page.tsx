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
    Settings2,
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

type StationStatus = "PENDING" | "ACTIVE" | "INACTIVE" | "SUSPENDED";

interface Station {
    id: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
    totalPorts?: number;
    availablePorts?: number;
}

const statusVariantMap: Record<
    StationStatus,
    "default" | "secondary" | "destructive" | "outline"
> = {
    ACTIVE: "default",
    INACTIVE: "secondary",
    PENDING: "outline",
    SUSPENDED: "destructive",
};

const statusLabelMap: Record<StationStatus, string> = {
    ACTIVE: "Active",
    INACTIVE: "Inactive",
    PENDING: "Pending Approval",
    SUSPENDED: "Suspended",
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
            toast.error("Failed to load stations");
        } finally {
            setLoading(false);
        }
    }

    async function handleToggleStatus(station: Station) {
        // Don't allow toggle for PENDING or SUSPENDED stations
        if (station.status === "PENDING" || station.status === "SUSPENDED") {
            return;
        }

        const newStatus = station.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
        try {
            await axios.patch(
                `/api/stations/${station.id}/status?status=${newStatus}`,
                {},
                { withCredentials: true }
            );
            toast.success(
                `Station "${station.name}" is now ${newStatus === "ACTIVE" ? "active" : "inactive"}`
            );
            fetchStations();
        } catch (error) {
            console.error("Failed to update status:", error);
            toast.error("Failed to update status");
        }
    }

    async function handleDelete() {
        if (!stationToDelete) return;

        try {
            setDeleting(true);
            await axios.delete(`/api/stations/${stationToDelete.id}`, {
                withCredentials: true,
            });
            toast.success(`Station "${stationToDelete.name}" deleted`);
            setDeleteDialogOpen(false);
            setStationToDelete(null);
            fetchStations();
        } catch (error) {
            console.error("Failed to delete station:", error);
            toast.error("Failed to delete station");
        } finally {
            setDeleting(false);
        }
    }

    function confirmDelete(station: Station) {
        setStationToDelete(station);
        setDeleteDialogOpen(true);
    }

    function canToggleStatus(station: Station): boolean {
        return station.status === "ACTIVE" || station.status === "INACTIVE";
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
                    <h1 className="text-3xl font-bold">My Stations</h1>
                    <p className="text-muted-foreground">
                        Manage your EV charging stations
                    </p>
                </div>
                <Button
                    onClick={() => router.push("/stations/create")}
                    className="cursor-pointer"
                >
                    <Plus className="mr-2 h-4 w-4" />
                    Add Station
                </Button>
            </div>

            {stations.length === 0 ? (
                <Card>
                    <CardContent className="flex flex-col items-center justify-center py-16">
                        <Zap className="h-16 w-16 text-muted-foreground mb-4" />
                        <h3 className="text-lg font-semibold mb-2">
                            No stations yet
                        </h3>
                        <p className="text-muted-foreground text-center mb-4">
                            Get started by adding your first charging station
                        </p>
                        <Button
                            onClick={() => router.push("/stations/create")}
                            className="cursor-pointer"
                        >
                            <Plus className="mr-2 h-4 w-4" />
                            Add Station
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <Card>
                    <CardHeader>
                        <CardTitle>Station List</CardTitle>
                        <CardDescription>
                            You have {stations.length} station{stations.length !== 1 ? "s" : ""}
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Station Name</TableHead>
                                    <TableHead>Address</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead className="text-center">
                                        Ports
                                    </TableHead>
                                    <TableHead className="text-right">
                                        Actions
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
                                                <Badge variant="secondary">Unknown</Badge>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-center">
                                            <span className="text-green-600 font-medium">
                                                {station.availablePorts ?? 0}
                                            </span>
                                            <span className="text-muted-foreground">
                                                /{station.totalPorts ?? 0}
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
                                                                `/stations/${station.id}`
                                                            )
                                                        }
                                                        className="cursor-pointer"
                                                    >
                                                        <Settings2 className="mr-2 h-4 w-4" />
                                                        Manage Chargers
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        onClick={() =>
                                                            router.push(
                                                                `/stations/${station.id}/edit`
                                                            )
                                                        }
                                                        className="cursor-pointer"
                                                    >
                                                        <Edit2 className="mr-2 h-4 w-4" />
                                                        Edit
                                                    </DropdownMenuItem>
                                                    {canToggleStatus(station) && (
                                                        <DropdownMenuItem
                                                            onClick={() =>
                                                                handleToggleStatus(station)
                                                            }
                                                            className="cursor-pointer"
                                                        >
                                                            {station.status === "ACTIVE" ? (
                                                                <>
                                                                    <PowerOff className="mr-2 h-4 w-4" />
                                                                    Deactivate
                                                                </>
                                                            ) : (
                                                                <>
                                                                    <Power className="mr-2 h-4 w-4" />
                                                                    Activate
                                                                </>
                                                            )}
                                                        </DropdownMenuItem>
                                                    )}
                                                    {station.status === "PENDING" && (
                                                        <DropdownMenuItem
                                                            disabled
                                                            className="text-muted-foreground"
                                                        >
                                                            <Power className="mr-2 h-4 w-4" />
                                                            Awaiting Approval
                                                        </DropdownMenuItem>
                                                    )}
                                                    <DropdownMenuSeparator />
                                                    <DropdownMenuItem
                                                        onClick={() =>
                                                            confirmDelete(station)
                                                        }
                                                        className="text-destructive cursor-pointer"
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" />
                                                        Delete
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
                        <AlertDialogTitle>Delete Station?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete{" "}
                            <strong>{stationToDelete?.name}</strong>? This action
                            cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">
                            Cancel
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={handleDelete}
                            disabled={deleting}
                            className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                        >
                            {deleting && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
