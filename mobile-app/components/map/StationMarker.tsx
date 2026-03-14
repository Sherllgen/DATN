import React from "react";
import { Marker } from "react-native-maps";
import { StationSearchResult, StationStatus } from "@/types/station.types";

const PIN_ACTIVE = require("@/assets/images/pin-active.png");
const PIN_INACTIVE = require("@/assets/images/pin-inactive.png");
const PIN_SUSPENDED = require("@/assets/images/pin-suspended.png");
const PIN_NAVIGATE = require("@/assets/images/pin-navigate.png");

export interface StationMarkerProps {
    station: StationSearchResult;
    isNavigating: boolean;
    isDestination: boolean;
    onPress: () => void;
}

/**
 * Optimized marker component with smart state logic.
 * 
 * Key features:
 * - Single marker that changes icon based on state (no dual rendering)
 * - Prevents flickering when entering/exiting navigation
 * - Memoized to prevent unnecessary re-renders
 * - tracksViewChanges={false} for static content performance
 * - Proper zIndex to prevent z-fighting with polyline
 */
const StationMarker: React.FC<StationMarkerProps> = React.memo(
    ({ station, isNavigating, isDestination, onPress }) => {
        /**
         * Smart icon selection logic:
         * - If navigating TO this station → Navigation icon (high priority)
         * - Otherwise → Available/Inactive based on station status
         */
        const getMarkerImage = () => {
            if (isNavigating && isDestination) {
                return PIN_NAVIGATE;
            }
            if (station.status === StationStatus.SUSPENDED) {
                return PIN_SUSPENDED;
            }
            return station.status === StationStatus.ACTIVE ? PIN_ACTIVE : PIN_INACTIVE;
        };

        /**
         * zIndex hierarchy:
         * - Normal markers: 5
         * - Destination marker during navigation: 10
         * This prevents z-fighting with the polyline (zIndex: 1)
         */
        const zIndex = isNavigating && isDestination ? 10 : 5;

        return (
            <Marker
                coordinate={{
                    latitude: station.latitude,
                    longitude: station.longitude,
                }}
                onPress={onPress}
                image={getMarkerImage()}
                zIndex={zIndex}
                tracksViewChanges={false} // Performance: Stop tracking for static markers
            />
        );
    }
);

StationMarker.displayName = "StationMarker";

export default StationMarker;
