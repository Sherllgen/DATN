"use client";

import { useEffect, useRef, useState } from "react";
import { MapPin, Search, Crosshair } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface LocationPickerProps {
    latitude: string;
    longitude: string;
    onLocationChange: (lat: string, lng: string) => void;
}

const DEFAULT_CENTER: [number, number] = [10.7769, 106.7009]; // Ho Chi Minh City
const DEFAULT_ZOOM = 13;

export function LocationPicker({ latitude, longitude, onLocationChange }: LocationPickerProps) {
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<any>(null);
    const markerRef = useRef<any>(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [searching, setSearching] = useState(false);
    const [mapReady, setMapReady] = useState(false);

    // Initialize map
    useEffect(() => {
        if (!mapContainerRef.current || mapRef.current) return;

        const initMap = async () => {
            const L = (await import("leaflet")).default;

            // Fix default marker icons for webpack/next.js
            delete (L.Icon.Default.prototype as any)._getIconUrl;
            L.Icon.Default.mergeOptions({
                iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
                iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
            });

            const initialLat = latitude ? parseFloat(latitude) : DEFAULT_CENTER[0];
            const initialLng = longitude ? parseFloat(longitude) : DEFAULT_CENTER[1];
            const center: [number, number] = [
                isNaN(initialLat) ? DEFAULT_CENTER[0] : initialLat,
                isNaN(initialLng) ? DEFAULT_CENTER[1] : initialLng,
            ];

            const map = L.map(mapContainerRef.current!, {
                center,
                zoom: DEFAULT_ZOOM,
            });

            L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
                maxZoom: 19,
            }).addTo(map);

            // Add marker if coordinates exist
            if (latitude && longitude && !isNaN(initialLat) && !isNaN(initialLng)) {
                markerRef.current = L.marker(center, { draggable: true }).addTo(map);
                markerRef.current.on("dragend", () => {
                    const pos = markerRef.current.getLatLng();
                    onLocationChange(pos.lat.toFixed(6), pos.lng.toFixed(6));
                });
            }

            // Click to place marker
            map.on("click", (e: any) => {
                const { lat, lng } = e.latlng;
                if (markerRef.current) {
                    markerRef.current.setLatLng([lat, lng]);
                } else {
                    markerRef.current = L.marker([lat, lng], { draggable: true }).addTo(map);
                    markerRef.current.on("dragend", () => {
                        const pos = markerRef.current.getLatLng();
                        onLocationChange(pos.lat.toFixed(6), pos.lng.toFixed(6));
                    });
                }
                onLocationChange(lat.toFixed(6), lng.toFixed(6));
            });

            mapRef.current = map;
            setMapReady(true);
        };

        // Load Leaflet CSS
        if (!document.querySelector('link[href*="leaflet"]')) {
            const link = document.createElement("link");
            link.rel = "stylesheet";
            link.href = "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css";
            document.head.appendChild(link);
        }

        initMap();

        return () => {
            if (mapRef.current) {
                mapRef.current.remove();
                mapRef.current = null;
                markerRef.current = null;
            }
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Sync external coordinate changes to marker
    useEffect(() => {
        if (!mapReady || !mapRef.current) return;
        const lat = parseFloat(latitude);
        const lng = parseFloat(longitude);
        if (isNaN(lat) || isNaN(lng)) return;

        if (markerRef.current) {
            const currentPos = markerRef.current.getLatLng();
            if (Math.abs(currentPos.lat - lat) > 0.000001 || Math.abs(currentPos.lng - lng) > 0.000001) {
                markerRef.current.setLatLng([lat, lng]);
                mapRef.current.panTo([lat, lng]);
            }
        }
    }, [latitude, longitude, mapReady]);

    async function handleSearch() {
        if (!searchQuery.trim() || !mapRef.current) return;
        setSearching(true);
        try {
            const res = await fetch(
                `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery)}&limit=1`
            );
            const results = await res.json();
            if (results.length > 0) {
                const { lat, lon } = results[0];
                const parsedLat = parseFloat(lat);
                const parsedLng = parseFloat(lon);
                mapRef.current.setView([parsedLat, parsedLng], 16);
                onLocationChange(parsedLat.toFixed(6), parsedLng.toFixed(6));

                const L = (await import("leaflet")).default;
                if (markerRef.current) {
                    markerRef.current.setLatLng([parsedLat, parsedLng]);
                } else {
                    markerRef.current = L.marker([parsedLat, parsedLng], { draggable: true }).addTo(mapRef.current);
                    markerRef.current.on("dragend", () => {
                        const pos = markerRef.current.getLatLng();
                        onLocationChange(pos.lat.toFixed(6), pos.lng.toFixed(6));
                    });
                }
            }
        } catch (error) {
            console.error("Search failed:", error);
        } finally {
            setSearching(false);
        }
    }

    function handleLocateMe() {
        if (!navigator.geolocation || !mapRef.current) return;
        navigator.geolocation.getCurrentPosition(async (position) => {
            const { latitude: lat, longitude: lng } = position.coords;
            mapRef.current.setView([lat, lng], 16);
            onLocationChange(lat.toFixed(6), lng.toFixed(6));

            const L = (await import("leaflet")).default;
            if (markerRef.current) {
                markerRef.current.setLatLng([lat, lng]);
            } else {
                markerRef.current = L.marker([lat, lng], { draggable: true }).addTo(mapRef.current);
                markerRef.current.on("dragend", () => {
                    const pos = markerRef.current.getLatLng();
                    onLocationChange(pos.lat.toFixed(6), pos.lng.toFixed(6));
                });
            }
        });
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <MapPin className="h-5 w-5" />
                    Location on Map
                </CardTitle>
                <CardDescription>
                    Click on the map, search an address, or drag the pin to set location
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                {/* Search bar */}
                <div className="flex gap-2">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                            placeholder="Search for an address..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && (e.preventDefault(), handleSearch())}
                            className="pl-9"
                        />
                    </div>
                    <Button
                        type="button"
                        variant="outline"
                        onClick={handleSearch}
                        disabled={searching}
                        className="cursor-pointer"
                    >
                        {searching ? "..." : "Search"}
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="icon"
                        onClick={handleLocateMe}
                        className="cursor-pointer"
                        title="Use my location"
                    >
                        <Crosshair className="h-4 w-4" />
                    </Button>
                </div>

                {/* Map */}
                <div
                    ref={mapContainerRef}
                    className="w-full h-[400px] rounded-lg border overflow-hidden"
                    style={{ zIndex: 0 }}
                />

                {/* Coordinate display */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="text-sm font-medium">Latitude</label>
                        <Input
                            type="number"
                            step="any"
                            value={latitude}
                            onChange={(e) => onLocationChange(e.target.value, longitude)}
                            placeholder="e.g. 10.7769"
                        />
                        <p className="text-xs text-muted-foreground mt-1">-90 to 90</p>
                    </div>
                    <div>
                        <label className="text-sm font-medium">Longitude</label>
                        <Input
                            type="number"
                            step="any"
                            value={longitude}
                            onChange={(e) => onLocationChange(latitude, e.target.value)}
                            placeholder="e.g. 106.7009"
                        />
                        <p className="text-xs text-muted-foreground mt-1">-180 to 180</p>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
