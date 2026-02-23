"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import axios from "axios";
import { toast } from "react-toastify";
import {
    ArrowLeft,
    Plus,
    Edit2,
    Trash2,
    Loader2,
    MapPin,
    Zap,
    ChevronDown,
    ChevronRight,
    Plug,
    Clock,
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
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { PhotoManager, StationPhoto } from "@/components/station/photo-manager";
import { PricingEditor, PriceSetting } from "@/components/station/pricing-editor";
import { PricingHistory } from "@/components/station/pricing-history";

type StationStatus = "PENDING" | "ACTIVE" | "INACTIVE" | "SUSPENDED";
type ConnectorType = "SOCKET_220V" | "VINFAST_STD" | "DATBIKE_FAST" | "IEC_TYPE_2" | "OTHER";
type PortStatus = "AVAILABLE" | "RESERVED" | "CHARGING" | "MAINTENANCE";
type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

interface OpeningHoursEntry {
    id?: number;
    dayOfWeek: DayOfWeek;
    openTime: string | null;
    closeTime: string | null;
    isOpen: boolean;
}

interface Station {
    id: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
    description?: string;
    openingHours?: OpeningHoursEntry[];
}

interface Port {
    id: number;
    chargerId: number;
    portNumber: string;
    status: PortStatus;
}

interface Charger {
    id: number;
    stationId: number;
    name: string;
    maxPower: number;
    connectorType: ConnectorType;
    status: string;
    totalPorts: number;
    availablePorts: number;
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
    PENDING: "Pending Approval",
    SUSPENDED: "Suspended",
};

const connectorTypeLabels: Record<ConnectorType, string> = {
    SOCKET_220V: "Socket 220V",
    VINFAST_STD: "VinFast Standard",
    DATBIKE_FAST: "Dat Bike Fast",
    IEC_TYPE_2: "IEC Type 2",
    OTHER: "Other",
};

const portStatusLabels: Record<PortStatus, string> = {
    AVAILABLE: "Available",
    RESERVED: "Reserved",
    CHARGING: "Charging",
    MAINTENANCE: "Maintenance",
};

const portStatusVariants: Record<PortStatus, "default" | "secondary" | "destructive" | "outline"> = {
    AVAILABLE: "default",
    RESERVED: "secondary",
    CHARGING: "outline",
    MAINTENANCE: "destructive",
};

export default function StationDetailPage() {
    const router = useRouter();
    const params = useParams();
    const stationId = params.id as string;

    const [station, setStation] = useState<Station | null>(null);
    const [chargers, setChargers] = useState<Charger[]>([]);
    const [chargerPorts, setChargerPorts] = useState<Record<number, Port[]>>({});
    const [expandedChargers, setExpandedChargers] = useState<Set<number>>(new Set());
    const [loading, setLoading] = useState(true);

    // Charger Dialog State
    const [chargerDialogOpen, setChargerDialogOpen] = useState(false);
    const [editingCharger, setEditingCharger] = useState<Charger | null>(null);
    const [chargerForm, setChargerForm] = useState({
        name: "",
        maxPower: "",
        connectorType: "SOCKET_220V" as ConnectorType,
    });
    const [savingCharger, setSavingCharger] = useState(false);

    // Port Dialog State
    const [portDialogOpen, setPortDialogOpen] = useState(false);
    const [selectedChargerId, setSelectedChargerId] = useState<number | null>(null);
    const [portNumber, setPortNumber] = useState("");
    const [savingPort, setSavingPort] = useState(false);
    const [editingPort, setEditingPort] = useState<Port | null>(null);
    const [portStatus, setPortStatus] = useState<PortStatus>("AVAILABLE");

    // Delete States
    const [deleteChargerDialog, setDeleteChargerDialog] = useState(false);
    const [chargerToDelete, setChargerToDelete] = useState<Charger | null>(null);
    const [deletePortDialog, setDeletePortDialog] = useState(false);
    const [portToDelete, setPortToDelete] = useState<Port | null>(null);
    const [deleting, setDeleting] = useState(false);

    // Photos & Pricing State
    const [photos, setPhotos] = useState<StationPhoto[]>([]);
    const [activePricing, setActivePricing] = useState<PriceSetting | null>(null);
    const [pricingRefreshKey, setPricingRefreshKey] = useState(0);

    useEffect(() => {
        fetchData();
    }, [stationId]);

    async function fetchData() {
        try {
            setLoading(true);
            const [stationRes, chargersRes, photosRes, pricingRes] = await Promise.all([
                axios.get(`/api/stations/${stationId}`, { withCredentials: true }),
                axios.get(`/api/chargers?stationId=${stationId}`, { withCredentials: true }),
                axios.get(`/api/stations/${stationId}/photos`, { withCredentials: true }).catch(() => ({ data: { data: [] } })),
                axios.get(`/api/stations/${stationId}/pricing`, { withCredentials: true }).catch(() => ({ data: { data: null } })),
            ]);

            if (stationRes.data?.data) {
                setStation(stationRes.data.data);
            }
            if (chargersRes.data?.data) {
                setChargers(chargersRes.data.data);
            }
            setPhotos(photosRes.data?.data || []);
            setActivePricing(pricingRes.data?.data || null);
        } catch (error) {
            console.error("Failed to fetch data:", error);
            toast.error("Failed to load station data");
            router.push("/stations");
        } finally {
            setLoading(false);
        }
    }

    function refreshPhotos() {
        axios.get(`/api/stations/${stationId}/photos`, { withCredentials: true })
            .then((res) => setPhotos(res.data?.data || []))
            .catch(console.error);
    }

    function refreshPricing() {
        axios.get(`/api/stations/${stationId}/pricing`, { withCredentials: true })
            .then((res) => setActivePricing(res.data?.data || null))
            .catch(console.error);
        setPricingRefreshKey((k) => k + 1);
    }

    async function fetchPorts(chargerId: number) {
        try {
            const response = await axios.get(`/api/chargers/${chargerId}/ports`, {
                withCredentials: true,
            });
            if (response.data?.data) {
                setChargerPorts((prev) => ({
                    ...prev,
                    [chargerId]: response.data.data,
                }));
            }
        } catch (error) {
            console.error("Failed to fetch ports:", error);
        }
    }

    function toggleChargerExpand(chargerId: number) {
        const newExpanded = new Set(expandedChargers);
        if (newExpanded.has(chargerId)) {
            newExpanded.delete(chargerId);
        } else {
            newExpanded.add(chargerId);
            if (!chargerPorts[chargerId]) {
                fetchPorts(chargerId);
            }
        }
        setExpandedChargers(newExpanded);
    }

    // Charger CRUD
    function openAddCharger() {
        setEditingCharger(null);
        setChargerForm({ name: "", maxPower: "", connectorType: "SOCKET_220V" });
        setChargerDialogOpen(true);
    }

    function openEditCharger(charger: Charger) {
        setEditingCharger(charger);
        setChargerForm({
            name: charger.name,
            maxPower: String(charger.maxPower),
            connectorType: charger.connectorType,
        });
        setChargerDialogOpen(true);
    }

    async function saveCharger() {
        if (!chargerForm.name || !chargerForm.maxPower) {
            toast.error("Please fill in all required fields");
            return;
        }

        try {
            setSavingCharger(true);

            if (editingCharger) {
                // UpdateChargerRequest: name, maxPower, connectorType
                const updatePayload = {
                    name: chargerForm.name,
                    maxPower: parseFloat(chargerForm.maxPower),
                    connectorType: chargerForm.connectorType,
                };
                await axios.put(`/api/chargers/${editingCharger.id}`, updatePayload, {
                    withCredentials: true,
                });
                toast.success("Charger updated successfully");
            } else {
                // CreateChargerRequest: name, maxPower, stationId
                const createPayload = {
                    stationId: parseInt(stationId),
                    name: chargerForm.name,
                    maxPower: parseFloat(chargerForm.maxPower),
                };
                await axios.post("/api/chargers", createPayload, {
                    withCredentials: true,
                });
                toast.success("Charger created successfully");
            }

            setChargerDialogOpen(false);
            fetchData();
        } catch (error: any) {
            console.error("Failed to save charger:", error);
            toast.error(error.response?.data?.message || "Failed to save charger");
        } finally {
            setSavingCharger(false);
        }
    }

    async function deleteCharger() {
        if (!chargerToDelete) return;

        try {
            setDeleting(true);
            await axios.delete(`/api/chargers/${chargerToDelete.id}`, {
                withCredentials: true,
            });
            toast.success("Charger deleted");
            setDeleteChargerDialog(false);
            setChargerToDelete(null);
            fetchData();
        } catch (error) {
            console.error("Failed to delete charger:", error);
            toast.error("Failed to delete charger");
        } finally {
            setDeleting(false);
        }
    }

    // Port CRUD
    function openAddPort(chargerId: number) {
        setEditingPort(null);
        setSelectedChargerId(chargerId);
        setPortNumber("");
        setPortStatus("AVAILABLE");
        setPortDialogOpen(true);
    }

    function openEditPort(port: Port) {
        setEditingPort(port);
        setSelectedChargerId(port.chargerId);
        setPortNumber(port.portNumber);
        setPortStatus(port.status);
        setPortDialogOpen(true);
    }

    async function savePort() {
        if (!selectedChargerId) return;

        if (!editingPort && !portNumber) {
            toast.error("Please enter a port number");
            return;
        }

        try {
            setSavingPort(true);

            if (editingPort) {
                // Update Port Status
                await axios.patch(`/api/ports/${editingPort.id}/status`, {
                    status: portStatus,
                }, { withCredentials: true });

                toast.success("Port updated successfully");
            } else {
                // Create New Port
                await axios.post(`/api/chargers/${selectedChargerId}/ports`, {
                    portNumber,
                }, { withCredentials: true });

                toast.success("Port created successfully");
            }

            setPortDialogOpen(false);
            fetchPorts(selectedChargerId);
            fetchData(); // Refresh charger counts
        } catch (error: any) {
            console.error("Failed to save port:", error);
            toast.error(error.response?.data?.message || "Failed to save port");
        } finally {
            setSavingPort(false);
        }
    }

    async function deletePort() {
        if (!portToDelete) return;

        try {
            setDeleting(true);
            await axios.delete(`/api/ports/${portToDelete.id}`, {
                withCredentials: true,
            });
            toast.success("Port deleted");
            setDeletePortDialog(false);

            // Refresh ports for the charger
            if (portToDelete.chargerId) {
                fetchPorts(portToDelete.chargerId);
            }
            setPortToDelete(null);
            fetchData();
        } catch (error) {
            console.error("Failed to delete port:", error);
            toast.error("Failed to delete port");
        } finally {
            setDeleting(false);
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
                    <CardContent className="p-6 space-y-4">
                        {[1, 2, 3].map((i) => (
                            <Skeleton key={i} className="h-20 w-full" />
                        ))}
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (!station) {
        return null;
    }

    const isPending = station.status === "PENDING";

    return (
        <div className="px-4 lg:px-6 space-y-6">
            {/* Header */}
            <div className="flex items-start justify-between">
                <div className="flex items-center gap-4">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => router.push("/stations")}
                        className="cursor-pointer"
                    >
                        <ArrowLeft className="h-5 w-5" />
                    </Button>
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-3xl font-bold">{station.name}</h1>
                            <Badge variant={statusVariantMap[station.status]}>
                                {statusLabelMap[station.status]}
                            </Badge>
                        </div>
                        <div className="flex items-center gap-1 text-muted-foreground mt-1">
                            <MapPin className="h-4 w-4" />
                            <span>{station.address}</span>
                        </div>
                    </div>
                </div>
                <Button
                    variant="outline"
                    onClick={() => router.push(`/stations/${stationId}/edit`)}
                    className="cursor-pointer"
                >
                    <Edit2 className="mr-2 h-4 w-4" />
                    Edit Station
                </Button>
            </div>

            {/* Pending Warning */}
            {isPending && (
                <Card className="border-yellow-500/50 bg-yellow-500/10">
                    <CardContent className="py-4">
                        <p className="text-yellow-600 dark:text-yellow-400 flex items-center gap-2">
                            <Zap className="h-5 w-5" />
                            This station is pending admin approval. You can configure chargers, but they won&apos;t be visible to users until approved.
                        </p>
                    </CardContent>
                </Card>
            )}

            {/* Opening Hours Section */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Clock className="h-5 w-5" />
                        Opening Hours
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {!station.openingHours || station.openingHours.length === 0 ? (
                        <p className="text-muted-foreground">Open 24/7</p>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                            {["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"].map((day) => {
                                const entry = station.openingHours?.find((e) => e.dayOfWeek === day);
                                const dayLabel = day.charAt(0) + day.slice(1).toLowerCase();
                                return (
                                    <div key={day} className="flex justify-between py-1 border-b last:border-0">
                                        <span className="font-medium">{dayLabel}</span>
                                        <span className="text-muted-foreground">
                                            {entry?.isOpen === false
                                                ? "Closed"
                                                : entry?.openTime && entry?.closeTime
                                                ? `${entry.openTime} - ${entry.closeTime}`
                                                : "24 hours"}
                                        </span>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Station Photos */}
            <PhotoManager
                stationId={parseInt(stationId)}
                photos={photos}
                onPhotosChange={refreshPhotos}
            />

            {/* Pricing Configuration */}
            <PricingEditor
                stationId={parseInt(stationId)}
                activePricing={activePricing}
                onPricingChange={refreshPricing}
            />

            {/* Pricing History */}
            <PricingHistory
                stationId={parseInt(stationId)}
                refreshKey={pricingRefreshKey}
            />

            {/* Chargers Section */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle>Chargers</CardTitle>
                        <CardDescription>
                            Manage charging equipment for this station
                        </CardDescription>
                    </div>
                    <Button onClick={openAddCharger} className="cursor-pointer">
                        <Plus className="mr-2 h-4 w-4" />
                        Add Charger
                    </Button>
                </CardHeader>
                <CardContent>
                    {chargers.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-12 text-center">
                            <Zap className="h-12 w-12 text-muted-foreground mb-4" />
                            <h3 className="text-lg font-semibold mb-2">No chargers yet</h3>
                            <p className="text-muted-foreground mb-4">
                                Add your first charger to this station
                            </p>
                            <Button onClick={openAddCharger} className="cursor-pointer">
                                <Plus className="mr-2 h-4 w-4" />
                                Add Charger
                            </Button>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {chargers.map((charger) => (
                                <Collapsible
                                    key={charger.id}
                                    open={expandedChargers.has(charger.id)}
                                    onOpenChange={() => toggleChargerExpand(charger.id)}
                                >
                                    <div className="border rounded-lg">
                                        <CollapsibleTrigger asChild className="w-full">
                                            <div className="flex items-center justify-between p-4 hover:bg-muted/50 cursor-pointer">
                                                <div className="flex items-center gap-3">
                                                    {expandedChargers.has(charger.id) ? (
                                                        <ChevronDown className="h-5 w-5 text-muted-foreground" />
                                                    ) : (
                                                        <ChevronRight className="h-5 w-5 text-muted-foreground" />
                                                    )}
                                                    <div className="text-left">
                                                        <div className="font-medium">{charger.name}</div>
                                                        <div className="text-sm text-muted-foreground">
                                                            {connectorTypeLabels[charger.connectorType]} • {charger.maxPower}kW
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="flex items-center gap-4">
                                                    <div className="text-right">
                                                        <div className="text-sm font-medium">
                                                            <span className="text-green-600">{charger.availablePorts}</span>
                                                            <span className="text-muted-foreground">/{charger.totalPorts} ports</span>
                                                        </div>
                                                    </div>
                                                    <div className="flex gap-1" onClick={(e) => e.stopPropagation()}>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            onClick={() => openEditCharger(charger)}
                                                            className="cursor-pointer"
                                                        >
                                                            <Edit2 className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            onClick={() => {
                                                                setChargerToDelete(charger);
                                                                setDeleteChargerDialog(true);
                                                            }}
                                                            className="cursor-pointer text-destructive"
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </div>
                                                </div>
                                            </div>
                                        </CollapsibleTrigger>
                                        <CollapsibleContent>
                                            <div className="border-t px-4 py-3 bg-muted/30">
                                                <div className="flex items-center justify-between mb-3">
                                                    <span className="text-sm font-medium">Ports</span>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => openAddPort(charger.id)}
                                                        className="cursor-pointer"
                                                    >
                                                        <Plus className="mr-1 h-3 w-3" />
                                                        Add Port
                                                    </Button>
                                                </div>
                                                {chargerPorts[charger.id]?.length ? (
                                                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
                                                        {chargerPorts[charger.id].map((port) => (
                                                            <div
                                                                key={port.id}
                                                                className="flex items-center justify-between p-3 rounded-md bg-background border"
                                                            >
                                                                <div className="flex items-center gap-2">
                                                                    <Plug className="h-4 w-4 text-muted-foreground" />
                                                                    <span className="font-medium">{port.portNumber}</span>
                                                                    <Badge variant={portStatusVariants[port.status]} className="text-xs">
                                                                        {portStatusLabels[port.status]}
                                                                    </Badge>
                                                                </div>
                                                                    <div className="flex gap-1">
                                                                        <Button
                                                                            variant="ghost"
                                                                            size="icon"
                                                                            onClick={() => openEditPort(port)}
                                                                            className="cursor-pointer"
                                                                        >
                                                                            <Edit2 className="h-3 w-3" />
                                                                        </Button>
                                                                        <Button
                                                                            variant="ghost"
                                                                            size="icon"
                                                                            className="h-7 w-7 cursor-pointer text-destructive"
                                                                            onClick={() => {
                                                                                setPortToDelete(port);
                                                                                setDeletePortDialog(true);
                                                                            }}
                                                                        >
                                                                            <Trash2 className="h-3 w-3" />
                                                                        </Button>
                                                                    </div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                ) : (
                                                    <p className="text-sm text-muted-foreground text-center py-4">
                                                        No ports configured for this charger
                                                    </p>
                                                )}
                                            </div>
                                        </CollapsibleContent>
                                    </div>
                                </Collapsible>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Charger Dialog */}
            <Dialog open={chargerDialogOpen} onOpenChange={setChargerDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {editingCharger ? "Edit Charger" : "Add New Charger"}
                        </DialogTitle>
                        <DialogDescription>
                            Configure the charger details below
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="charger-name">Name *</Label>
                            <Input
                                id="charger-name"
                                value={chargerForm.name}
                                onChange={(e) =>
                                    setChargerForm((prev) => ({ ...prev, name: e.target.value }))
                                }
                                placeholder="e.g. Charger A1"
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="max-power">Max Power (kW) *</Label>
                            <Input
                                id="max-power"
                                type="number"
                                value={chargerForm.maxPower}
                                onChange={(e) =>
                                    setChargerForm((prev) => ({ ...prev, maxPower: e.target.value }))
                                }
                                placeholder="e.g. 7.2"
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="connector-type">Connector Type *</Label>
                            <Select
                                value={chargerForm.connectorType}
                                onValueChange={(value: ConnectorType) =>
                                    setChargerForm((prev) => ({ ...prev, connectorType: value }))
                                }
                            >
                                <SelectTrigger>
                                    <SelectValue placeholder="Select connector type" />
                                </SelectTrigger>
                                <SelectContent>
                                    {Object.entries(connectorTypeLabels).map(([key, label]) => (
                                        <SelectItem key={key} value={key}>
                                            {label}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => setChargerDialogOpen(false)}
                            className="cursor-pointer"
                        >
                            Cancel
                        </Button>
                        <Button onClick={saveCharger} disabled={savingCharger} className="cursor-pointer">
                            {savingCharger && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            {editingCharger ? "Save Changes" : "Add Charger"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Port Dialog */}
            <Dialog open={portDialogOpen} onOpenChange={setPortDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{editingPort ? "Edit Port" : "Add New Port"}</DialogTitle>
                        <DialogDescription>
                            {editingPort ? "Update port status" : "Enter the port identifier"}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="port-number">Port Number {editingPort ? "(Read-only)" : "*"}</Label>
                            <Input
                                id="port-number"
                                value={portNumber}
                                onChange={(e) => setPortNumber(e.target.value)}
                                disabled={!!editingPort}
                                placeholder="e.g. P1, Port-A, etc."
                            />
                        </div>
                        {editingPort && (
                            <div className="space-y-2">
                                <Label htmlFor="port-status">Status</Label>
                                <Select
                                    value={portStatus}
                                    onValueChange={(value: PortStatus) => setPortStatus(value)}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="Select status" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {Object.entries(portStatusLabels).map(([key, label]) => (
                                            <SelectItem key={key} value={key}>
                                                {label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        )}
                    </div>
                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => setPortDialogOpen(false)}
                            className="cursor-pointer"
                        >
                            Cancel
                        </Button>
                        <Button onClick={savePort} disabled={savingPort} className="cursor-pointer">
                            {savingPort && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            {editingPort ? "Save Changes" : "Add Port"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Delete Charger Dialog */}
            <AlertDialog open={deleteChargerDialog} onOpenChange={setDeleteChargerDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Charger?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete <strong>{chargerToDelete?.name}</strong>?
                            This will also delete all ports associated with this charger.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={deleteCharger}
                            disabled={deleting}
                            className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                        >
                            {deleting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Delete Port Dialog */}
            <AlertDialog open={deletePortDialog} onOpenChange={setDeletePortDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Port?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete port <strong>{portToDelete?.portNumber}</strong>?
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={deletePort}
                            disabled={deleting}
                            className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                        >
                            {deleting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
