import React, { useState, useEffect } from "react";
import { View, Text, ScrollView, Dimensions } from "react-native";
import MultiSlider from "@ptomasroos/react-native-multi-slider";
import { StationFilterParams, StationStatus, FilterMetadata } from "@/types/station.types";
import Modal from "@/components/ui/Modal";
import Chip from "@/components/ui/Chip";
import Button from "@/components/ui/Button";
import Skeleton from "@/components/ui/Skeleton";

interface FilterBottomSheetProps {
    visible: boolean;
    onClose: () => void;
    onApply: (filters: StationFilterParams) => void;
    initialFilters?: StationFilterParams;
    meta: FilterMetadata | null;
}

const SCREEN_WIDTH = Dimensions.get("window").width;
const SLIDER_LENGTH = SCREEN_WIDTH - 80;

export default function FilterBottomSheet({
    visible,
    onClose,
    onApply,
    initialFilters,
    meta,
}: FilterBottomSheetProps) {

    const [powerRange, setPowerRange] = useState<number[]>([
        initialFilters?.minPower ?? (meta?.minPower || 0),
        initialFilters?.maxPower ?? (meta?.maxPower || 150),
    ]);
    const [selectedConnectors, setSelectedConnectors] = useState<string[]>(
        initialFilters?.connectorTypes ?? []
    );
    const [selectedStatuses, setSelectedStatuses] = useState<string[]>(
        initialFilters?.status ? [initialFilters.status as string] : []
    );

    // Synchronize state when modal opens or meta/initialFilters change
    useEffect(() => {
        if (visible) {
            setPowerRange([
                initialFilters?.minPower ?? (meta?.minPower || 0),
                initialFilters?.maxPower ?? (meta?.maxPower || 150),
            ]);
            setSelectedConnectors(initialFilters?.connectorTypes ?? []);
            setSelectedStatuses(initialFilters?.status ? [initialFilters.status as string] : []);
        }
    }, [visible, initialFilters, meta]);

    // Quick Select handler
    const handleQuickPowerSelect = (type: "ac" | "dc" | "uf") => {
        if (!meta) return;
        switch (type) {
            case "ac":
                setPowerRange([meta.minPower, 22]);
                break;
            case "dc":
                setPowerRange([22, 60]);
                break;
            case "uf":
                setPowerRange([60, meta.maxPower]);
                break;
        }
    };

    const toggleConnector = (connector: string) => {
        if (selectedConnectors.includes(connector)) {
            setSelectedConnectors(selectedConnectors.filter((c) => c !== connector));
        } else {
            setSelectedConnectors([...selectedConnectors, connector]);
        }
    };

    const toggleStatus = (status: string) => {
        if (selectedStatuses.includes(status)) {
            setSelectedStatuses([]); // Deselect if already selected
        } else {
            setSelectedStatuses([status]); // Single selection
        }
    };

    const handleApply = () => {
        onApply({
            minPower: powerRange[0],
            maxPower: powerRange[1],
            connectorTypes: selectedConnectors,
            status: selectedStatuses.length > 0 ? selectedStatuses[0] : undefined,
        });
        onClose();
    };

    const handleClear = () => {
        setPowerRange([meta?.minPower || 0, meta?.maxPower || 150]);
        setSelectedConnectors([]);
        setSelectedStatuses([]);
    };

    // UI Helpers
    const getConnectorDetails = (type: string) => {
        switch (type) {
            case "IEC_TYPE_2":
                return { label: "Type 2", icon: "flash-outline" as const };
            case "VINFAST_STD":
                return { label: "VinFast STD", icon: "flash-outline" as const };
            default:
                return { label: type, icon: "hardware-chip-outline" as const };
        }
    };

    const getStatusLabel = (status: string) => {
        return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
    };

    return (
        <Modal
            visible={visible}
            onClose={onClose}
            variant="bottom-sheet"
            title="Filter Stations"
        >
            <ScrollView className="max-h-[85vh] px-4" showsVerticalScrollIndicator={false}>
                {/* Power Output Section */}
                <View className="mb-6">
                    <Text className="text-secondary font-semibold text-base mb-2">Power Output</Text>

                    {!meta ? (
                        <View className="gap-4">
                            <View className="flex-row gap-2">
                                <Skeleton width={80} height={32} borderRadius={20} />
                                <Skeleton width={120} height={32} borderRadius={20} />
                                <Skeleton width={100} height={32} borderRadius={20} />
                            </View>
                            <Skeleton height={40} className="my-2" />
                            <View className="flex-row justify-between">
                                <Skeleton width={40} height={16} />
                                <Skeleton width={40} height={16} />
                            </View>
                        </View>
                    ) : (
                        <>
                            <View className="flex-row flex-wrap gap-2 mb-4">
                                <Chip
                                    label="AC (< 22kW)"
                                    selected={powerRange[0] === meta.minPower && powerRange[1] === 22}
                                    onPress={() => handleQuickPowerSelect("ac")}
                                />
                                <Chip
                                    label="Fast DC (22-60kW)"
                                    selected={powerRange[0] === 22 && powerRange[1] === 60}
                                    onPress={() => handleQuickPowerSelect("dc")}
                                />
                                <Chip
                                    label="Ultra-Fast (> 60kW)"
                                    selected={powerRange[0] === 60 && powerRange[1] === meta.maxPower}
                                    onPress={() => handleQuickPowerSelect("uf")}
                                />
                            </View>

                            <View className="items-center w-full px-2">
                                <MultiSlider
                                    values={powerRange}
                                    min={meta.minPower}
                                    max={meta.maxPower}
                                    step={1}
                                    onValuesChange={(values) => setPowerRange(values)}
                                    selectedStyle={{ backgroundColor: "#00A452" }}
                                    unselectedStyle={{ backgroundColor: "#E2E8F0" }}
                                    markerStyle={{ backgroundColor: "#FFB000", width: 24, height: 24, borderRadius: 12, elevation: 3, shadowColor: "#000", shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.2, shadowRadius: 2, borderWidth: 2, borderColor: "#FFF" }}
                                    containerStyle={{ height: 40 }}
                                    sliderLength={SLIDER_LENGTH}
                                    snapped
                                />
                                <View className="w-full flex-row justify-between -mt-2">
                                    <Text className="text-text-secondary text-sm font-medium">{meta.minPower} kW</Text>
                                    <Text className="text-text-secondary text-sm font-medium">{meta.maxPower} kW</Text>
                                </View>
                            </View>
                        </>
                    )}
                </View>

                {/* Connector Types Section */}
                <View className="mb-6">
                    <Text className="text-secondary font-semibold text-base mb-3">Connector Types</Text>
                    {!meta ? (
                        <View className="flex-row flex-wrap gap-2">
                            <Skeleton width={100} height={36} />
                            <Skeleton width={120} height={36} />
                            <Skeleton width={80} height={36} />
                        </View>
                    ) : (
                        <View className="flex-row flex-wrap gap-2">
                            {Array.from(new Set(meta.connectorTypes)).map((type) => {
                                const details = getConnectorDetails(type);
                                return (
                                    <Chip
                                        key={`conn-${type}`}
                                        label={details.label}
                                        icon={details.icon}
                                        selected={selectedConnectors.includes(type)}
                                        onPress={() => toggleConnector(type)}
                                    />
                                );
                            })}
                        </View>
                    )}
                </View>

                {/* Status Section */}
                <View className="mb-8">
                    <Text className="text-secondary font-semibold text-base mb-3">Status</Text>
                    {!meta ? (
                        <View className="flex-row flex-wrap gap-2">
                            <Skeleton width={80} height={36} />
                            <Skeleton width={80} height={36} />
                            <Skeleton width={80} height={36} />
                        </View>
                    ) : (
                        <View className="flex-row flex-wrap gap-2">
                            {Array.from(new Set(meta.statuses)).map((status) => (
                                <Chip
                                    key={`status-${status}`}
                                    label={getStatusLabel(status as string)}
                                    selected={selectedStatuses.includes(status as string)}
                                    onPress={() => toggleStatus(status as string)}
                                />
                            ))}
                        </View>
                    )}
                </View>
            </ScrollView>

            {/* Actions */}
            <View className="flex-row gap-4 pt-4 border-t border-border mt-2">
                <Button
                    variant="outline"
                    onPress={handleClear}
                    className="flex-1"
                >
                    Clear All
                </Button>
                <Button
                    variant="primary"
                    onPress={handleApply}
                    className="flex-1"
                >
                    Apply Filters
                </Button>
            </View>
        </Modal>
    );
}
