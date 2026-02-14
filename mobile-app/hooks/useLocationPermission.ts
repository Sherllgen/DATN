import { useState, useEffect } from "react";
import * as Location from "expo-location";
import { LocationModalStatus } from "@/components/map/LocationPermissionModal";

export interface UseLocationPermissionReturn {
    // State
    location: Location.LocationObject | null;
    hasPermission: boolean;
    
    // Modal state
    showPermissionModal: boolean;
    permissionStatus: LocationModalStatus;
    showManualInput: boolean;
    
    // Handlers
    checkLocationPermission: () => Promise<void>;
    requestLocationPermission: () => Promise<void>;
    getCurrentLocation: () => Promise<void>;
    handleEnterManually: () => void;
    handleManualLocationSet: (latitude: number, longitude: number) => void;
    
    // Setters
    setShowPermissionModal: (show: boolean) => void;
    setLocation: (location: Location.LocationObject | null) => void;
}

/**
 * Custom hook for managing location permissions and user location.
 * Can be used across multiple screens (Home, Map, etc.)
 */
export const useLocationPermission = (autoCheck: boolean = true): UseLocationPermissionReturn => {
    const [location, setLocation] = useState<Location.LocationObject | null>(null);
    const [hasPermission, setHasPermission] = useState(false);
    const [showPermissionModal, setShowPermissionModal] = useState(false);
    const [permissionStatus, setPermissionStatus] = useState<LocationModalStatus>("idle");
    const [showManualInput, setShowManualInput] = useState(false);

    // Auto-check permission on mount if enabled
    useEffect(() => {
        if (autoCheck) {
            checkLocationPermission();
        }
    }, []);

    /**
     * Check if location permission is granted
     */
    const checkLocationPermission = async () => {
        const { status } = await Location.getForegroundPermissionsAsync();

        if (status !== "granted") {
            setPermissionStatus("permission_required");
            setShowPermissionModal(true);
        } else {
            setHasPermission(true);
            getCurrentLocation();
        }
    };

    /**
     * Request location permission from user
     */
    const requestLocationPermission = async () => {
        const { status } = await Location.requestForegroundPermissionsAsync();

        if (status === "granted") {
            setShowPermissionModal(false);
            setPermissionStatus("idle");
            setHasPermission(true);
            getCurrentLocation();
        } else {
            setShowPermissionModal(false);
        }
    };

    /**
     * Get current user location
     */
    const getCurrentLocation = async () => {
        try {
            let userLocation;

            try {
                // First try with Balanced accuracy and timeout
                userLocation = await Location.getCurrentPositionAsync({
                    accuracy: Location.Accuracy.Balanced,
                    timeInterval: 10000, // 10 second timeout
                });
            } catch (balancedError) {
                console.warn("Balanced accuracy failed, trying Low accuracy:", balancedError);

                // Fallback to Low accuracy (uses network location)
                userLocation = await Location.getCurrentPositionAsync({
                    accuracy: Location.Accuracy.Low,
                    timeInterval: 5000, // 5 second timeout for fallback
                });
            }

            setLocation(userLocation);
            
            if (__DEV__) {
                console.log('[useLocationPermission] User location obtained:', {
                    lat: userLocation.coords.latitude,
                    lng: userLocation.coords.longitude,
                    accuracy: userLocation.coords.accuracy
                });
            }
        } catch (error) {
            console.error("Error getting location (all attempts failed):", error);
            setPermissionStatus("gps_error");
            setShowPermissionModal(true);
        }
    };

    /**
     * Handle "Enter Manually" button in permission modal
     */
    const handleEnterManually = () => {
        setShowPermissionModal(false);
        setShowManualInput(true);
    };

    /**
     * Handle manual location input
     */
    const handleManualLocationSet = (latitude: number, longitude: number) => {
        const manualLocation = {
            coords: {
                latitude,
                longitude,
                altitude: null,
                accuracy: null,
                altitudeAccuracy: null,
                heading: null,
                speed: null,
            },
            timestamp: Date.now(),
        };

        setLocation(manualLocation as any);
        setHasPermission(true);
        setShowManualInput(false);

        if (__DEV__) {
            console.log('[useLocationPermission] Manual location set:', { latitude, longitude });
        }
    };

    return {
        // State
        location,
        hasPermission,
        
        // Modal state
        showPermissionModal,
        permissionStatus,
        showManualInput,
        
        // Handlers
        checkLocationPermission,
        requestLocationPermission,
        getCurrentLocation,
        handleEnterManually,
        handleManualLocationSet,
        
        // Setters
        setShowPermissionModal,
        setLocation,
    };
};
