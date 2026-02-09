"use client";

import { useState, useEffect } from "react";
import { Clock, Calendar } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export type DayOfWeek =
    | "MONDAY"
    | "TUESDAY"
    | "WEDNESDAY"
    | "THURSDAY"
    | "FRIDAY"
    | "SATURDAY"
    | "SUNDAY";

export interface OpeningHoursEntry {
    dayOfWeek: DayOfWeek;
    openTime: string | null;
    closeTime: string | null;
    isOpen: boolean;
}

interface OpeningHoursEditorProps {
    value: OpeningHoursEntry[];
    onChange: (hours: OpeningHoursEntry[]) => void;
    is24_7?: boolean;
    onToggle24_7?: (value: boolean) => void;
}

const DAYS: { key: DayOfWeek; label: string; short: string }[] = [
    { key: "MONDAY", label: "Monday", short: "Mon" },
    { key: "TUESDAY", label: "Tuesday", short: "Tue" },
    { key: "WEDNESDAY", label: "Wednesday", short: "Wed" },
    { key: "THURSDAY", label: "Thursday", short: "Thu" },
    { key: "FRIDAY", label: "Friday", short: "Fri" },
    { key: "SATURDAY", label: "Saturday", short: "Sat" },
    { key: "SUNDAY", label: "Sunday", short: "Sun" },
];

const DEFAULT_OPEN_TIME = "07:00";
const DEFAULT_CLOSE_TIME = "22:00";

export function getDefaultOpeningHours(): OpeningHoursEntry[] {
    return DAYS.map((day) => ({
        dayOfWeek: day.key,
        openTime: DEFAULT_OPEN_TIME,
        closeTime: DEFAULT_CLOSE_TIME,
        isOpen: true,
    }));
}

export function OpeningHoursEditor({
    value,
    onChange,
    is24_7 = true,
    onToggle24_7,
}: OpeningHoursEditorProps) {
    const [localIs24_7, setLocalIs24_7] = useState(is24_7);

    useEffect(() => {
        setLocalIs24_7(is24_7);
    }, [is24_7]);

    function handleToggle24_7(enabled: boolean) {
        setLocalIs24_7(enabled);
        onToggle24_7?.(enabled);
    }

    function updateDay(dayOfWeek: DayOfWeek, updates: Partial<OpeningHoursEntry>) {
        const newHours = value.map((entry) =>
            entry.dayOfWeek === dayOfWeek ? { ...entry, ...updates } : entry
        );
        onChange(newHours);
    }

    function toggleDayOpen(dayOfWeek: DayOfWeek, isOpen: boolean) {
        updateDay(dayOfWeek, {
            isOpen,
            openTime: isOpen ? DEFAULT_OPEN_TIME : null,
            closeTime: isOpen ? DEFAULT_CLOSE_TIME : null,
        });
    }

    function applyToAllWeekdays() {
        const monday = value.find((e) => e.dayOfWeek === "MONDAY");
        if (!monday) return;

        const weekdays: DayOfWeek[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];
        const newHours = value.map((entry) =>
            weekdays.includes(entry.dayOfWeek)
                ? { ...entry, openTime: monday.openTime, closeTime: monday.closeTime, isOpen: monday.isOpen }
                : entry
        );
        onChange(newHours);
    }

    function applyToAll() {
        const monday = value.find((e) => e.dayOfWeek === "MONDAY");
        if (!monday) return;

        const newHours = value.map((entry) => ({
            ...entry,
            openTime: monday.openTime,
            closeTime: monday.closeTime,
            isOpen: monday.isOpen,
        }));
        onChange(newHours);
    }

    return (
        <Card>
            <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2 text-lg">
                        <Clock className="h-5 w-5" />
                        Opening Hours
                    </CardTitle>
                    <div className="flex items-center gap-2">
                        <Label htmlFor="toggle-24-7" className="text-sm text-muted-foreground">
                            Open 24/7
                        </Label>
                        <Switch
                            id="toggle-24-7"
                            checked={localIs24_7}
                            onCheckedChange={handleToggle24_7}
                        />
                    </div>
                </div>
            </CardHeader>
            <CardContent className="space-y-4">
                {localIs24_7 ? (
                    <div className="flex items-center justify-center py-6 text-muted-foreground">
                        <Calendar className="h-5 w-5 mr-2" />
                        <span>Station is open 24 hours, 7 days a week</span>
                    </div>
                ) : (
                    <>
                        <div className="flex gap-2 mb-4">
                            <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={applyToAllWeekdays}
                                className="cursor-pointer"
                            >
                                Apply Monday to Weekdays
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={applyToAll}
                                className="cursor-pointer"
                            >
                                Apply Monday to All
                            </Button>
                        </div>

                        <div className="space-y-3">
                            {DAYS.map((day) => {
                                const entry = value.find((e) => e.dayOfWeek === day.key);
                                const isOpen = entry?.isOpen ?? true;

                                return (
                                    <div
                                        key={day.key}
                                        className="grid grid-cols-[100px_60px_1fr] gap-8 items-center"
                                    >
                                        <div className="font-medium">{day.label}</div>
                                        <div className="flex items-center gap-2">
                                            <Switch
                                                checked={isOpen}
                                                onCheckedChange={(checked) =>
                                                    toggleDayOpen(day.key, checked)
                                                }
                                            />
                                            <span className="text-xs text-muted-foreground">
                                                {isOpen ? "Open" : "Closed"}
                                            </span>
                                        </div>
                                        {isOpen ? (
                                            <div className="flex items-center gap-2">
                                                <Input
                                                    type="time"
                                                    value={entry?.openTime || DEFAULT_OPEN_TIME}
                                                    onChange={(e) =>
                                                        updateDay(day.key, { openTime: e.target.value })
                                                    }
                                                    className="w-32"
                                                />
                                                <span className="text-muted-foreground">to</span>
                                                <Input
                                                    type="time"
                                                    value={entry?.closeTime || DEFAULT_CLOSE_TIME}
                                                    onChange={(e) =>
                                                        updateDay(day.key, { closeTime: e.target.value })
                                                    }
                                                    className="w-32"
                                                />
                                            </div>
                                        ) : (
                                            <div className="text-muted-foreground text-sm">
                                                Closed all day
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </>
                )}
            </CardContent>
        </Card>
    );
}
