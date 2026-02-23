"use client";

import { useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { DollarSign, Loader2, Clock } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";

export interface PriceSetting {
    id: number;
    stationId: number;
    version: number;
    chargingRatePerKwh: number;
    bookingFee: number | null;
    idlePenaltyPerMinute: number | null;
    gracePeriodMinutes: number;
    isActive: boolean;
    notes: string | null;
    effectiveFrom: string | null;
    createdAt: string;
}

interface PricingEditorProps {
    stationId: number;
    activePricing: PriceSetting | null;
    onPricingChange: () => void;
}

export function PricingEditor({ stationId, activePricing, onPricingChange }: PricingEditorProps) {
    const [editing, setEditing] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [chargingRate, setChargingRate] = useState("");
    const [bookingFee, setBookingFee] = useState("");
    const [idlePenalty, setIdlePenalty] = useState("");
    const [gracePeriod, setGracePeriod] = useState("30");
    const [notes, setNotes] = useState("");

    function startEditing() {
        if (activePricing) {
            setChargingRate(String(activePricing.chargingRatePerKwh));
            setBookingFee(activePricing.bookingFee != null ? String(activePricing.bookingFee) : "");
            setIdlePenalty(activePricing.idlePenaltyPerMinute != null ? String(activePricing.idlePenaltyPerMinute) : "");
            setGracePeriod(String(activePricing.gracePeriodMinutes));
        } else {
            setChargingRate("");
            setBookingFee("");
            setIdlePenalty("");
            setGracePeriod("30");
        }
        setNotes("");
        setEditing(true);
    }

    async function handleSave() {
        const rate = parseFloat(chargingRate);
        if (isNaN(rate) || rate <= 0) {
            toast.error("Charging rate must be a positive number");
            return;
        }

        try {
            setSubmitting(true);
            await axios.post(`/api/stations/${stationId}/pricing`, {
                chargingRatePerKwh: rate,
                bookingFee: bookingFee ? parseFloat(bookingFee) : null,
                idlePenaltyPerMinute: idlePenalty ? parseFloat(idlePenalty) : null,
                gracePeriodMinutes: gracePeriod ? parseInt(gracePeriod) : 30,
                notes: notes.trim() || null,
            }, { withCredentials: true });
            toast.success("Pricing updated successfully");
            setEditing(false);
            onPricingChange();
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Failed to update pricing");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <Card>
            <CardHeader>
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <DollarSign className="h-5 w-5" />
                            Pricing Configuration
                        </CardTitle>
                        <CardDescription>
                            {activePricing
                                ? `Version ${activePricing.version} — Active`
                                : "No pricing configured yet"}
                        </CardDescription>
                    </div>
                    {!editing && (
                        <Button
                            type="button"
                            size="sm"
                            onClick={startEditing}
                            className="cursor-pointer"
                        >
                            {activePricing ? "Update Pricing" : "Set Pricing"}
                        </Button>
                    )}
                </div>
            </CardHeader>
            <CardContent>
                {editing ? (
                    <div className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="text-sm font-medium">
                                    Charging Rate (VND/kWh) *
                                </label>
                                <Input
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    placeholder="e.g. 3500"
                                    value={chargingRate}
                                    onChange={(e) => setChargingRate(e.target.value)}
                                />
                            </div>
                            <div>
                                <label className="text-sm font-medium">Booking Fee (VND)</label>
                                <Input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    placeholder="e.g. 10000 (optional)"
                                    value={bookingFee}
                                    onChange={(e) => setBookingFee(e.target.value)}
                                />
                            </div>
                            <div>
                                <label className="text-sm font-medium">
                                    Idle Penalty (VND/min)
                                </label>
                                <Input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    placeholder="e.g. 1000 (optional)"
                                    value={idlePenalty}
                                    onChange={(e) => setIdlePenalty(e.target.value)}
                                />
                            </div>
                            <div>
                                <label className="text-sm font-medium flex items-center gap-1">
                                    <Clock className="h-3.5 w-3.5" />
                                    Grace Period (minutes)
                                </label>
                                <Input
                                    type="number"
                                    min="0"
                                    placeholder="Default: 30"
                                    value={gracePeriod}
                                    onChange={(e) => setGracePeriod(e.target.value)}
                                />
                            </div>
                        </div>
                        <div>
                            <label className="text-sm font-medium">Notes (reason for change)</label>
                            <Textarea
                                placeholder="Optional: describe why you're changing the pricing"
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                className="min-h-[80px]"
                            />
                        </div>
                        <div className="flex gap-2">
                            <Button
                                type="button"
                                onClick={handleSave}
                                disabled={submitting || !chargingRate}
                                className="cursor-pointer"
                            >
                                {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                {activePricing ? "Create New Version" : "Set Pricing"}
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setEditing(false)}
                                className="cursor-pointer"
                            >
                                Cancel
                            </Button>
                        </div>
                    </div>
                ) : activePricing ? (
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <PriceCard
                            label="Charging Rate"
                            value={`${activePricing.chargingRatePerKwh.toLocaleString()} VND/kWh`}
                        />
                        <PriceCard
                            label="Booking Fee"
                            value={
                                activePricing.bookingFee != null
                                    ? `${activePricing.bookingFee.toLocaleString()} VND`
                                    : "—"
                            }
                        />
                        <PriceCard
                            label="Idle Penalty"
                            value={
                                activePricing.idlePenaltyPerMinute != null
                                    ? `${activePricing.idlePenaltyPerMinute.toLocaleString()} VND/min`
                                    : "—"
                            }
                        />
                        <PriceCard
                            label="Grace Period"
                            value={`${activePricing.gracePeriodMinutes} min`}
                        />
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
                        <DollarSign className="h-10 w-10 mb-2 opacity-40" />
                        <p className="text-sm">No pricing configured</p>
                        <p className="text-xs">Set pricing to allow bookings</p>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}

function PriceCard({ label, value }: { label: string; value: string }) {
    return (
        <div className="rounded-lg border p-3">
            <p className="text-xs text-muted-foreground mb-1">{label}</p>
            <p className="text-sm font-semibold">{value}</p>
        </div>
    );
}
