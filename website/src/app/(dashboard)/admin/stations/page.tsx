"use client";

import { useEffect, useState, useCallback } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import {
    CheckCircle,
    XCircle,
    Ban,
    Unlock,
    Loader2,
    MapPin,
    Building2,
    Eye,
    Clock,
    MoreHorizontal,
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
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

type StationStatus = "PENDING" | "ACTIVE" | "INACTIVE" | "SUSPENDED";

interface Station {
    id: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
    ownerId: number;
    ownerName?: string;
    createdAt: string;
}

const statusVariantMap: Record<StationStatus, "default" | "secondary" | "destructive" | "outline"> = {
    ACTIVE: "default",
    INACTIVE: "secondary",
    PENDING: "outline",
    SUSPENDED: "destructive",
};

const statusLabelMap: Record<StationStatus, string> = {
    ACTIVE: "Active",
    INACTIVE: "Inactive",
    PENDING: "Pending",
    SUSPENDED: "Suspended",
};

export default function AdminStationsPage() {
    const [stations, setStations] = useState<Station[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<string>("PENDING");
    const [processing, setProcessing] = useState<number | null>(null);

    // Dialog states
    const [detailsDialog, setDetailsDialog] = useState(false);
    const [selectedStation, setSelectedStation] = useState<Station | null>(null);
    const [rejectDialog, setRejectDialog] = useState(false);
    const [suspendDialog, setSuspendDialog] = useState(false);
    const [reason, setReason] = useState("");

    const fetchStations = useCallback(async (status?: string) => {
        try {
            setLoading(true);
            const url = status && status !== "ALL" 
                ? `/api/admin/stations?status=${status}` 
                : "/api/admin/stations";
            const response = await axios.get(url, { withCredentials: true });
            
            if (response.data?.data?.content) {
                setStations(response.data.data.content);
            } else if (response.data?.data) {
                setStations(Array.isArray(response.data.data) ? response.data.data : []);
            }
        } catch (error) {
            console.error("Failed to fetch stations:", error);
            toast.error("Failed to load stations");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchStations(activeTab);
    }, [activeTab, fetchStations]);

    async function handleApprove(station: Station) {
        try {
            setProcessing(station.id);
            await axios.post(`/api/admin/stations/${station.id}/approve`, {}, {
                withCredentials: true,
            });
            toast.success(`Station "${station.name}" approved`);
            fetchStations(activeTab);
        } catch (error: any) {
            console.error("Failed to approve:", error);
            toast.error(error.response?.data?.message || "Failed to approve station");
        } finally {
            setProcessing(null);
        }
    }

    async function handleReject() {
        if (!selectedStation || !reason.trim()) {
            toast.error("Please provide a reason for rejection");
            return;
        }

        try {
            setProcessing(selectedStation.id);
            await axios.post(`/api/admin/stations/${selectedStation.id}/reject`, {
                reason: reason.trim(),
            }, { withCredentials: true });
            toast.success(`Station "${selectedStation.name}" rejected`);
            setRejectDialog(false);
            setReason("");
            fetchStations(activeTab);
        } catch (error: any) {
            console.error("Failed to reject:", error);
            toast.error(error.response?.data?.message || "Failed to reject station");
        } finally {
            setProcessing(null);
        }
    }

    async function handleSuspend() {
        if (!selectedStation || !reason.trim()) {
            toast.error("Please provide a reason for suspension");
            return;
        }

        try {
            setProcessing(selectedStation.id);
            await axios.post(`/api/admin/stations/${selectedStation.id}/suspend`, {
                reason: reason.trim(),
            }, { withCredentials: true });
            toast.success(`Station "${selectedStation.name}" suspended`);
            setSuspendDialog(false);
            setReason("");
            fetchStations(activeTab);
        } catch (error: any) {
            console.error("Failed to suspend:", error);
            toast.error(error.response?.data?.message || "Failed to suspend station");
        } finally {
            setProcessing(null);
        }
    }

    async function handleUnsuspend(station: Station) {
        try {
            setProcessing(station.id);
            await axios.post(`/api/admin/stations/${station.id}/unsuspend`, {}, {
                withCredentials: true,
            });
            toast.success(`Station "${station.name}" unsuspended`);
            fetchStations(activeTab);
        } catch (error: any) {
            console.error("Failed to unsuspend:", error);
            toast.error(error.response?.data?.message || "Failed to unsuspend station");
        } finally {
            setProcessing(null);
        }
    }

    function openRejectDialog(station: Station) {
        setSelectedStation(station);
        setReason("");
        setRejectDialog(true);
    }

    function openSuspendDialog(station: Station) {
        setSelectedStation(station);
        setReason("");
        setSuspendDialog(true);
    }

    function openDetailsDialog(station: Station) {
        setSelectedStation(station);
        setDetailsDialog(true);
    }

    function formatDate(dateStr: string) {
        return new Date(dateStr).toLocaleDateString("en-US", {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    function renderDropdownActions(station: Station) {
        const isProcessing = processing === station.id;

        return (
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" className="cursor-pointer" disabled={isProcessing}>
                        {isProcessing ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <MoreHorizontal className="h-4 w-4" />
                        )}
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                    <DropdownMenuItem
                        onClick={() => openDetailsDialog(station)}
                        className="cursor-pointer"
                    >
                        <Eye className="mr-2 h-4 w-4" />
                        View Details
                    </DropdownMenuItem>
                    {station.status === "PENDING" && (
                        <>
                            <DropdownMenuItem
                                onClick={() => handleApprove(station)}
                                className="cursor-pointer"
                                disabled={isProcessing}
                            >
                                <CheckCircle className="mr-2 h-4 w-4" />
                                Approve
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => openRejectDialog(station)}
                                className="cursor-pointer text-destructive"
                                disabled={isProcessing}
                            >
                                <XCircle className="mr-2 h-4 w-4" />
                                Reject
                            </DropdownMenuItem>
                        </>
                    )}
                    {(station.status === "ACTIVE" || station.status === "INACTIVE") && (
                        <DropdownMenuItem
                            onClick={() => openSuspendDialog(station)}
                            className="cursor-pointer text-destructive"
                            disabled={isProcessing}
                        >
                            <Ban className="mr-2 h-4 w-4" />
                            Suspend
                        </DropdownMenuItem>
                    )}
                    {station.status === "SUSPENDED" && (
                        <DropdownMenuItem
                            onClick={() => handleUnsuspend(station)}
                            className="cursor-pointer"
                            disabled={isProcessing}
                        >
                            <Unlock className="mr-2 h-4 w-4" />
                            Unsuspend
                        </DropdownMenuItem>
                    )}
                </DropdownMenuContent>
            </DropdownMenu>
        );
    }

    return (
        <div className="px-4 lg:px-6 space-y-6">
            <div>
                <h1 className="text-3xl font-bold">Station Review</h1>
                <p className="text-muted-foreground">
                    Review and manage charging station submissions
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Stations</CardTitle>
                    <CardDescription>
                        Approve, reject, or manage station statuses
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <Tabs value={activeTab} onValueChange={setActiveTab}>
                        <TabsList className="mb-4">
                            <TabsTrigger value="PENDING" className="cursor-pointer">
                                <Clock className="h-4 w-4 mr-1" />
                                Pending
                            </TabsTrigger>
                            <TabsTrigger value="ACTIVE" className="cursor-pointer">
                                Active
                            </TabsTrigger>
                            <TabsTrigger value="SUSPENDED" className="cursor-pointer">
                                Suspended
                            </TabsTrigger>
                            <TabsTrigger value="ALL" className="cursor-pointer">
                                All
                            </TabsTrigger>
                        </TabsList>

                        <TabsContent value={activeTab}>
                            {loading ? (
                                <div className="space-y-3">
                                    {[1, 2, 3].map((i) => (
                                        <Skeleton key={i} className="h-16 w-full" />
                                    ))}
                                </div>
                            ) : stations.length === 0 ? (
                                <div className="text-center py-12">
                                    <Building2 className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                                    <p className="text-muted-foreground">
                                        No {activeTab.toLowerCase()} stations found
                                    </p>
                                </div>
                            ) : (
                                <Table>
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead>Station</TableHead>
                                            <TableHead>Owner</TableHead>
                                            <TableHead>Status</TableHead>
                                            <TableHead>Submitted</TableHead>
                                            <TableHead className="text-right">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {stations.map((station) => (
                                            <TableRow key={station.id}>
                                                <TableCell>
                                                    <div>
                                                        <div className="font-medium">{station.name}</div>
                                                        <div className="flex items-center gap-1 text-sm text-muted-foreground">
                                                            <MapPin className="h-3 w-3" />
                                                            <span className="line-clamp-1">{station.address}</span>
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    {station.ownerName || `Owner #${station.ownerId}`}
                                                </TableCell>
                                                <TableCell>
                                                    <Badge variant={statusVariantMap[station.status]}>
                                                        {statusLabelMap[station.status]}
                                                    </Badge>
                                                </TableCell>
                                                <TableCell className="text-sm text-muted-foreground">
                                                    {formatDate(station.createdAt)}
                                                </TableCell>
                                                <TableCell className="text-right">
                                                    {renderDropdownActions(station)}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            )}
                        </TabsContent>
                    </Tabs>
                </CardContent>
            </Card>

            {/* Station Details Dialog */}
            <Dialog open={detailsDialog} onOpenChange={setDetailsDialog}>
                <DialogContent className="max-w-lg">
                    <DialogHeader>
                        <DialogTitle>Station Details</DialogTitle>
                    </DialogHeader>
                    {selectedStation && (
                        <div className="space-y-4">
                            <div>
                                <Label className="text-muted-foreground">Name</Label>
                                <p className="font-medium">{selectedStation.name}</p>
                            </div>
                            <div>
                                <Label className="text-muted-foreground">Address</Label>
                                <p>{selectedStation.address}</p>
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label className="text-muted-foreground">Latitude</Label>
                                    <p>{selectedStation.latitude}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Longitude</Label>
                                    <p>{selectedStation.longitude}</p>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label className="text-muted-foreground">Status</Label>
                                    <div className="mt-1">
                                        <Badge variant={statusVariantMap[selectedStation.status]}>
                                            {statusLabelMap[selectedStation.status]}
                                        </Badge>
                                    </div>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Submitted</Label>
                                    <p>{formatDate(selectedStation.createdAt)}</p>
                                </div>
                            </div>
                        </div>
                    )}
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setDetailsDialog(false)} className="cursor-pointer">
                            Close
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Reject Dialog */}
            <Dialog open={rejectDialog} onOpenChange={setRejectDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Reject Station</DialogTitle>
                        <DialogDescription>
                            Please provide a reason for rejecting "{selectedStation?.name}"
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="reject-reason">Reason *</Label>
                            <Textarea
                                id="reject-reason"
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Explain why this station is being rejected..."
                                className="min-h-[100px]"
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setRejectDialog(false)} className="cursor-pointer">
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={handleReject}
                            disabled={processing !== null}
                            className="cursor-pointer"
                        >
                            {processing !== null && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Reject Station
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Suspend Dialog */}
            <Dialog open={suspendDialog} onOpenChange={setSuspendDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Suspend Station</DialogTitle>
                        <DialogDescription>
                            Please provide a reason for suspending "{selectedStation?.name}"
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="suspend-reason">Reason *</Label>
                            <Textarea
                                id="suspend-reason"
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Explain why this station is being suspended..."
                                className="min-h-[100px]"
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setSuspendDialog(false)} className="cursor-pointer">
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={handleSuspend}
                            disabled={processing !== null}
                            className="cursor-pointer"
                        >
                            {processing !== null && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Suspend Station
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
