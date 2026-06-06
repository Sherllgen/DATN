import React from "react";
import { Marker } from "react-native-maps";
import { Image, View } from "react-native";
import { Station, StationStatus } from "@/types/station.types";

const PIN_ACTIVE = require("@/assets/images/pin-active.png");
const PIN_INACTIVE = require("@/assets/images/pin-inactive.png");
const PIN_SUSPENDED = require("@/assets/images/pin-suspended.png");
const PIN_NAVIGATE = require("@/assets/images/pin-navigate.png");

export interface StationMarkerProps {
    station: Station;
    isNavigating?: boolean;
    isDestination?: boolean;
    onPress?: () => void;
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

        // Unify size across all maps for consistency
        const size = 40;

        const [tracksViewChanges, setTracksViewChanges] = React.useState(true);

        // Force re-tracking if marker status changes
        React.useEffect(() => {
            setTracksViewChanges(true);
        }, [isNavigating, isDestination, station.status]);

        // Generate a unique key based on state so the Marker remounts when icon changes
        const markerKey = `${station.id}-${isNavigating}-${isDestination}-${station.status}`;

        return (
            <Marker
                key={markerKey}
                coordinate={{
                    latitude: station.latitude,
                    longitude: station.longitude,
                }}
                anchor={{ x: 0.5, y: 1 }} // Pointy tip at the exact coordinate
                centerOffset={{ x: 0, y: -size / 2 }} // Needed for callout positioning
                onPress={onPress}
                zIndex={zIndex}
                tracksViewChanges={tracksViewChanges}
            >
                <View style={{ width: size, height: size }}>
                    <Image 
                        source={getMarkerImage()} 
                        style={{ width: size, height: size }} 
                        resizeMode="contain"
                        onLoad={() => {
                            // On Android Release builds, we must wait a tick after load 
                            // to let the layout engine resize the image before freezing the marker.
                            setTimeout(() => setTracksViewChanges(false), 100);
                        }}
                    />
                </View>
            </Marker>
        );
    }
);

StationMarker.displayName = "StationMarker";

export default StationMarker;
