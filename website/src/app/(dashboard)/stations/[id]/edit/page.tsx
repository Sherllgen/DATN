"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import axios from "axios";
import { toast } from "react-toastify";
import { ArrowLeft, Loader2 } from "lucide-react";

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
import {
    OpeningHoursEditor,
    OpeningHoursEntry,
    DayOfWeek,
    getDefaultOpeningHours,
} from "@/components/station/opening-hours-editor";
import { PhotoManager, StationPhoto } from "@/components/station/photo-manager";
import { LocationPicker } from "@/components/station/location-picker";

const stationSchema = z.object({
    name: z.string().min(1, "Station name is required").max(200),
    address: z.string().min(1, "Address is required").max(500),
    latitude: z.string().min(1, "Latitude is required"),
    longitude: z.string().min(1, "Longitude is required"),
    description: z.string().max(1000).optional(),
});

type StationFormValues = z.infer<typeof stationSchema>;

interface StationOpeningHoursResponse {
    id: number;
    dayOfWeek: DayOfWeek;
    openTime: string | null;
    closeTime: string | null;
    isOpen: boolean;
}

export default function EditStationPage() {
    const router = useRouter();
    const params = useParams();
    const stationId = params.id as string;
    
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [photos, setPhotos] = useState<StationPhoto[]>([]);
    const [is24_7, setIs24_7] = useState(true);
    const [openingHours, setOpeningHours] = useState<OpeningHoursEntry[]>(
        getDefaultOpeningHours()
    );

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
                    const photosRes = await axios.get(`/api/stations/${stationId}/photos`, {
                        withCredentials: true,
                    });
                    form.reset({
                        name: station.name || "",
                        address: station.address || "",
                        latitude: String(station.latitude || 0),
                        longitude: String(station.longitude || 0),
                        description: station.description || "",
                    });

                    // Load opening hours if present
                    if (station.openingHours && station.openingHours.length > 0) {
                        setIs24_7(false);
                        const loadedHours: OpeningHoursEntry[] = station.openingHours.map(
                            (oh: StationOpeningHoursResponse) => ({
                                dayOfWeek: oh.dayOfWeek,
                                openTime: oh.openTime,
                                closeTime: oh.closeTime,
                                isOpen: oh.isOpen ?? true,
                            })
                        );
                        // Ensure all 7 days are present
                        const defaultHours = getDefaultOpeningHours();
                        const mergedHours = defaultHours.map((dh) => {
                            const found = loadedHours.find((lh) => lh.dayOfWeek === dh.dayOfWeek);
                            return found || dh;
                        });
                        setOpeningHours(mergedHours);
                    } else {
                        setIs24_7(true);
                        setOpeningHours(getDefaultOpeningHours());
                    }
                    setPhotos(photosRes.data?.data || []);
                }
            } catch (error) {
                console.error("Failed to fetch station:", error);
                toast.error("Failed to load station details");
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

            const payload: Record<string, unknown> = {
                ...data,
                latitude: parseFloat(data.latitude),
                longitude: parseFloat(data.longitude),
            };

            // Only include openingHours if not 24/7
            if (!is24_7) {
                payload.openingHours = openingHours.map((entry) => ({
                    dayOfWeek: entry.dayOfWeek,
                    openTime: entry.isOpen ? entry.openTime : null,
                    closeTime: entry.isOpen ? entry.closeTime : null,
                    isOpen: entry.isOpen,
                }));
            } else {
                payload.openingHours = null;
            }

            await axios.put(`/api/stations/${stationId}`, payload, {
                withCredentials: true,
            });

            toast.success("Station updated successfully!");
            router.push("/stations");
        } catch (error: any) {
            console.error("Failed to update station:", error);
            toast.error(
                error.response?.data?.message ||
                    "Failed to update station. Please try again."
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

    function refreshPhotos() {
        axios.get(`/api/stations/${stationId}/photos`, { withCredentials: true })
            .then((res) => setPhotos(res.data?.data || []))
            .catch(console.error);
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
                    <h1 className="text-3xl font-bold">Edit Station</h1>
                    <p className="text-muted-foreground">
                        Update station information
                    </p>
                </div>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Basic Information</CardTitle>
                            <CardDescription>
                                Update the basic details of your charging station
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Station Name *</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="e.g. EV-Go Station District 1"
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
                                        <FormLabel>Address *</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="e.g. 123 Nguyen Hue, District 1, HCMC"
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
                                        <FormLabel>Description</FormLabel>
                                        <FormControl>
                                            <Textarea
                                                placeholder="Description of the station, directions..."
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

                    <LocationPicker
                        latitude={form.watch("latitude")}
                        longitude={form.watch("longitude")}
                        onLocationChange={(lat, lng) => {
                            form.setValue("latitude", lat, { shouldValidate: true });
                            form.setValue("longitude", lng, { shouldValidate: true });
                        }}
                    />

                    {/* Station Photos */}
                    <PhotoManager
                        stationId={parseInt(stationId)}
                        photos={photos}
                        onPhotosChange={refreshPhotos}
                    />

                    <OpeningHoursEditor
                        value={openingHours}
                        onChange={setOpeningHours}
                        is24_7={is24_7}
                        onToggle24_7={setIs24_7}
                    />

                    <div className="flex justify-start gap-3">
                        <Button
                            type="submit"
                            disabled={submitting}
                            className="cursor-pointer"
                        >
                            {submitting && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            Save Changes
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => router.back()}
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
