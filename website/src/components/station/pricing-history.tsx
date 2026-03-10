"use client";

import { useEffect, useState } from "react";
import axios from "axios";
import { History, Loader2 } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

import { PriceSetting } from "./pricing-editor";

interface PricingHistoryProps {
    stationId: number;
    refreshKey?: number;
}

export function PricingHistory({ stationId, refreshKey }: PricingHistoryProps) {
    const [history, setHistory] = useState<PriceSetting[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [stationId, refreshKey]);

    async function fetchHistory() {
        try {
            setLoading(true);
            const res = await axios.get(`/api/stations/${stationId}/pricing/history`, {
                withCredentials: true,
            });
            setHistory(res.data?.data || []);
        } catch (error) {
            console.error("Failed to fetch pricing history:", error);
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return (
            <Card>
                <CardContent className="py-8 flex items-center justify-center">
                    <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
                </CardContent>
            </Card>
        );
    }

    if (history.length === 0) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-lg">
                        <History className="h-5 w-5" />
                        Pricing History
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground text-center py-4">
                        No pricing versions yet
                    </p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-lg">
                    <History className="h-5 w-5" />
                    Pricing History
                </CardTitle>
                <CardDescription>
                    {history.length} version{history.length !== 1 ? "s" : ""} — Latest version is active
                </CardDescription>
            </CardHeader>
            <CardContent>
                <div className="rounded-md border">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead className="w-[80px]">Version</TableHead>
                                <TableHead>Rate/kWh</TableHead>
                                <TableHead>Booking Fee</TableHead>
                                <TableHead>Idle Penalty</TableHead>
                                <TableHead>Grace</TableHead>
                                <TableHead>Notes</TableHead>
                                <TableHead className="w-[100px]">Status</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {history.map((entry) => (
                                <TableRow key={entry.id}>
                                    <TableCell className="font-mono">v{entry.version}</TableCell>
                                    <TableCell>
                                        {entry.chargingRatePerKwh.toLocaleString()} VND
                                    </TableCell>
                                    <TableCell>
                                        {entry.bookingFee != null
                                            ? `${entry.bookingFee.toLocaleString()} VND`
                                            : "—"}
                                    </TableCell>
                                    <TableCell>
                                        {entry.idlePenaltyPerMinute != null
                                            ? `${entry.idlePenaltyPerMinute.toLocaleString()} VND/min`
                                            : "—"}
                                    </TableCell>
                                    <TableCell>{entry.gracePeriodMinutes} min</TableCell>
                                    <TableCell className="max-w-[200px] truncate text-muted-foreground text-xs">
                                        {entry.notes || "—"}
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={entry.isActive ? "default" : "secondary"}>
                                            {entry.isActive ? "Active" : "Inactive"}
                                        </Badge>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            </CardContent>
        </Card>
    );
}
