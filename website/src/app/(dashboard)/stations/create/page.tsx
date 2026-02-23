"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
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
import {
    OpeningHoursEditor,
    OpeningHoursEntry,
    getDefaultOpeningHours,
} from "@/components/station/opening-hours-editor";
import { LocationPicker } from "@/components/station/location-picker";

const stationSchema = z.object({
    name: z.string().min(1, "Station name is required").max(200),
    address: z.string().min(1, "Address is required").max(500),
    latitude: z.string().min(1, "Latitude is required"),
    longitude: z.string().min(1, "Longitude is required"),
    description: z.string().max(1000).optional(),
});

type StationFormValues = z.infer<typeof stationSchema>;

export default function CreateStationPage() {
    const router = useRouter();
    const [submitting, setSubmitting] = useState(false);
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
            }

            await axios.post("/api/stations", payload, {
                withCredentials: true,
            });

            toast.success("Station created successfully!");
            router.push("/stations");
        } catch (error: any) {
            console.error("Failed to create station:", error);
            toast.error(
                error.response?.data?.message || "Failed to create station. Please try again."
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
                    <h1 className="text-3xl font-bold">Add New Station</h1>
                    <p className="text-muted-foreground">
                        Fill in the details to create a new charging station
                    </p>
                </div>
            </div>

            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Basic Information</CardTitle>
                            <CardDescription>
                                Enter the basic details of your charging station
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
                            Create Station
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
