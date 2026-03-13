import { useState, useEffect, useRef } from "react";
import * as Location from "expo-location";
import MapView, { Region } from "react-native-maps";
import { router } from "expo-router";
import { searchStationsInBound, filterStations, getStationMetadata } from "@/apis/stationApi/stationApi";
import { StationSearchResult, StationFilterParams, FilterMetadata } from "@/types/station.types";
import { getRoute } from "@/apis/stationApi/directionApi";
import mapboxPolyline from "@mapbox/polyline";
import { haversineDistance, simplifyPolyline } from "@/utils/location";
import { LocationModalStatus } from "@/components/map/LocationPermissionModal";
import { useStationCache } from "@/stores/stationCacheStore";
import { useLocationStore } from "@/stores/locationStore";

export interface UseMapLogicReturn {
    // State
    location: Location.LocationObject | null;
    stations: StationSearchResult[];
    selectedStation: StationSearchResult | null;
    routeCoordinates: { latitude: number; longitude: number }[];
    routeDistance: number;  // Total route distance in meters
    routeDuration: number;  // Total route duration in seconds
    remainingDistance: number;  // Calculated remaining distance
    isNavigating: boolean;
    isInitialLoading: boolean;
    isNavigationLoading: boolean;
    viewMode: "map" | "list";
    searchQuery: string;
    mapKey: number;

    // List View State
    listStations: StationSearchResult[];
    listPage: number;
    hasMoreList: boolean;
    isListLoading: boolean;
    fetchListStations: (page: number, filters?: StationFilterParams) => Promise<void>;
    loadMoreListStations: () => void;

    // Metadata
    filterMeta: FilterMetadata | null;

    // Modals
    showPermissionModal: boolean;
    permissionStatus: LocationModalStatus;
    showManualInput: boolean;
    showQuickInfo: boolean;

    // Handlers
    handleMarkerPress: (station: StationSearchResult) => void;
    handleNavigate: () => Promise<void>;
    cancelNavigation: () => Promise<void>;
    centerToUserLocation: () => void;
    handleRegionChangeComplete: (region: Region) => void;
    requestLocationPermission: () => Promise<void>;
    handleEnterManually: () => void;
    handleManualLocationSet: (lat: number, lng: number) => void;
    handleViewDetails: () => void;
    handleBook: () => void;
    setViewMode: (mode: "map" | "list") => void;
    setSearchQuery: (query: string) => void;

    // Refs
    mapRef: React.RefObject<MapView | null>;
    initialRegion: Region;

    // Setters for modals
    setShowQuickInfo: (show: boolean) => void;
    setShowPermissionModal: (show: boolean) => void;
}

const INITIAL_REGION: Region = {
    latitude: 10.8231,
    longitude: 106.6297,
    latitudeDelta: 0.15,
    longitudeDelta: 0.15,
};

/**
 * Custom hook that encapsulates all MapScreen business logic.
 * Separates state management, API calls, and side effects from UI rendering.
 * 
 * Performance optimizations:
 * - Polyline simplification using Douglas-Peucker algorithm
 * - Debounced region changes (800ms)
 * - Distance threshold (2km) for background fetches
 * - Abort controller for cancelling pending requests
 */
export const useMapLogic = (): UseMapLogicReturn => {
    // ==================== GLOBAL LOCATION STORE ====================
    const setGlobalLocation = useLocationStore((state) => state.setLocation);

    // ==================== STATE ====================
    const [location, setLocation] = useState<Location.LocationObject | null>(null);
    const [showPermissionModal, setShowPermissionModal] = useState(false);
    const [permissionStatus, setPermissionStatus] = useState<LocationModalStatus>("idle");
    const [showManualInput, setShowManualInput] = useState(false);
    const [stations, setStations] = useState<StationSearchResult[]>([]);
    const [selectedStation, setSelectedStation] = useState<StationSearchResult | null>(null);
    const [routeCoordinates, setRouteCoordinates] = useState<{ latitude: number; longitude: number }[]>([]);
    const [routeDistance, setRouteDistance] = useState<number>(0);
    const [routeDuration, setRouteDuration] = useState<number>(0);
    const [isNavigating, setIsNavigating] = useState(false);
    const [showQuickInfo, setShowQuickInfo] = useState(false);
    const [isInitialLoading, setIsInitialLoading] = useState(true);
    const [isNavigationLoading, setIsNavigationLoading] = useState(false);
    const [viewMode, setViewMode] = useState<"map" | "list">("map");
    const [searchQuery, setSearchQuery] = useState("");
    const [mapKey, setMapKey] = useState(0);

    // ==================== LIST VIEW STATE ====================
    const [listStations, setListStations] = useState<StationSearchResult[]>([]);
    const [listPage, setListPage] = useState(0);
    const [hasMoreList, setHasMoreList] = useState(true);
    const [isListLoading, setIsListLoading] = useState(false);
    
    // Track active filters for the list to pass when loading more
    const activeFiltersRef = useRef<StationFilterParams>({});

    // ==================== METADATA STATE ====================
    const [filterMeta, setFilterMeta] = useState<FilterMetadata | null>(null);

    // ==================== REFS ====================
    const mapRef = useRef<MapView>(null);
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const lastFetchedRegionRef = useRef<Region | null>(null);
    const abortControllerRef = useRef<AbortController | null>(null);
    const initialRegionRef = useRef<Region>(INITIAL_REGION);
    const locationWatcherRef = useRef<Location.LocationSubscription | null>(null);
    const listAbortControllerRef = useRef<AbortController | null>(null);

    // ==================== INITIALIZATION ====================
    useEffect(() => {
        checkLocationPermission();
        // Load stations immediately with default region
        fetchStationsInBound(initialRegionRef.current);
        
        // Fetch metadata
        getStationMetadata()
            .then((data) => setFilterMeta(data))
            .catch((error) => console.error("Error fetching station metadata:", error));
            
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // ==================== INITIAL LIST FETCH ====================
    // We wait for location to be available (or default to null) before fetching
    // the initial list so that distance can be calculated correctly on the backend.
    useEffect(() => {
        // Fetch list as long as it's not already loading
        fetchListStations(0);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location]);

    // ==================== SYNC LOCATION TO GLOBAL STORE ====================
    // Whenever location changes in map, sync it to global store for home screen
    useEffect(() => {
        if (location) {
            setGlobalLocation(location);
        }
    }, [location]);

    // ==================== NAVIGATION CAMERA CONTROL ====================
    // Effect to fit map to route when coordinates update
    useEffect(() => {
        if (routeCoordinates.length > 0 && isNavigating) {
            console.log('[Polyline Effect] Coordinates updated:', routeCoordinates.length, 'points');
            console.log('[Polyline Effect] Calculating route bounds...');
            
            // Calculate bounding box for route
            const lats = routeCoordinates.map(c => c.latitude);
            const lngs = routeCoordinates.map(c => c.longitude);
            const minLat = Math.min(...lats);
            const maxLat = Math.max(...lats);
            const minLng = Math.min(...lngs);
            const maxLng = Math.max(...lngs);
            
            const centerLat = (minLat + maxLat) / 2;
            const centerLng = (minLng + maxLng) / 2;
            const latDelta = (maxLat - minLat) * 1.5; // 1.5x padding
            const lngDelta = (maxLng - minLng) * 1.5;
            
            // Use animateToRegion instead of fitToCoordinates
            // This triggers a more reliable re-render in the native layer
            setTimeout(() => {
                console.log('[Polyline Effect] Animating to region NOW');
                mapRef.current?.animateToRegion({
                    latitude: centerLat,
                    longitude: centerLng,
                    latitudeDelta: Math.max(latDelta, 0.01),
                    longitudeDelta: Math.max(lngDelta, 0.01),
                }, 500);
            }, 300);
        }
    }, [routeCoordinates, isNavigating]);

    // ==================== LOCATION PERMISSIONS ====================
    const checkLocationPermission = async () => {
        const { status } = await Location.getForegroundPermissionsAsync();

        if (status !== "granted") {
            setPermissionStatus("permission_required");
            setShowPermissionModal(true);
        } else {
            getCurrentLocation();
        }
    };

    const requestLocationPermission = async () => {
        const { status } = await Location.requestForegroundPermissionsAsync();

        if (status === "granted") {
            setShowPermissionModal(false);
            setPermissionStatus("idle");
            getCurrentLocation();
        } else {
            setShowPermissionModal(false);
        }
    };

    const getCurrentLocation = async () => {
        try {
            setShowPermissionModal(false);

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
            
            console.log('[getCurrentLocation] User location obtained:', {
                lat: userLocation.coords.latitude,
                lng: userLocation.coords.longitude,
                accuracy: userLocation.coords.accuracy
            });

            const newRegion = {
                latitude: userLocation.coords.latitude,
                longitude: userLocation.coords.longitude,
                latitudeDelta: 0.05,
                longitudeDelta: 0.05,
            };

            // Smooth camera transition
            mapRef.current?.animateCamera(
                {
                    center: {
                        latitude: userLocation.coords.latitude,
                        longitude: userLocation.coords.longitude,
                    },
                    zoom: 15,
                    pitch: 0,
                    heading: 0,
                },
                { duration: 1000 }
            );

            // Fetch stations in new region's bounds
            fetchStationsInBound(newRegion);
        } catch (error) {
            console.error("Error getting location (all attempts failed):", error);
            setPermissionStatus("gps_error");
            setShowPermissionModal(true);
        }
    };

    const handleEnterManually = () => {
        setShowPermissionModal(false);
        setShowManualInput(true);
    };

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

        const newRegion = {
            latitude,
            longitude,
            latitudeDelta: 0.15,
            longitudeDelta: 0.15,
        };

        mapRef.current?.animateCamera(
            {
                center: { latitude, longitude },
                zoom: 14,
                pitch: 0,
                heading: 0,
            },
            { duration: 1000 }
        );

        fetchStationsInBound(newRegion);
    };

    // ==================== LOCATION TRACKING ====================
    /**
     * Start real-time GPS tracking during navigation
     * Optimized for battery efficiency:
     * - Balanced accuracy (vs High)
     * - Updates every 10m movement (not continuous)
     * - Max 1 update per 5 seconds
     */
    const startLocationTracking = async () => {
        try {
            // Stop existing watcher if any
            await stopLocationTracking();
            
            console.log('[Location Tracking] Starting GPS tracking...');
            
            locationWatcherRef.current = await Location.watchPositionAsync(
                {
                    accuracy: Location.Accuracy.Balanced,
                    distanceInterval: 10,  // Update every 10 meters
                    timeInterval: 5000,    // Max frequency: 5 seconds
                },
                (newLocation) => {
                    console.log('[Location Tracking] Position updated:', {
                        lat: newLocation.coords.latitude.toFixed(6),
                        lng: newLocation.coords.longitude.toFixed(6)
                    });
                    setLocation(newLocation);
                }
            );
            
            console.log('[Location Tracking] GPS tracking active');
        } catch (error) {
            console.error('[Location Tracking] Failed to start:', error);
        }
    };
    
    /**
     * Stop GPS tracking and cleanup
     */
    const stopLocationTracking = async () => {
        if (locationWatcherRef.current) {
            console.log('[Location Tracking] Stopping GPS tracking...');
            locationWatcherRef.current.remove();
            locationWatcherRef.current = null;
            console.log('[Location Tracking] GPS tracking stopped');
        }
    };
    
    // ==================== LOCATION TRACKING LIFECYCLE ====================
    // Start/stop tracking based on navigation state
    useEffect(() => {
        if (isNavigating) {
            startLocationTracking();
        } else {
            stopLocationTracking();
        }
        
        // Cleanup on unmount
        return () => {
            stopLocationTracking();
        };
    }, [isNavigating]);

    const hasRefetchedWithLocationRef = useRef<boolean>(false);

    // ==================== RE-FETCH WITH LOCATION ====================
    /**
     * Re-fetch stations when location becomes available
     * This ensures distanceKm is calculated with actual user coordinates
     */
    useEffect(() => {
        // Only re-fetch ONCE when location first becomes available
        if (location && lastFetchedRegionRef.current && stations.length > 0 && !hasRefetchedWithLocationRef.current) {
            // Check if stations are missing distanceKm (indicates they were fetched without location)
            const needsRefetch = stations.some(s => s.distanceKm === null);
            
            console.log('[Location Effect] Checking re-fetch:', {
                hasLocation: !!location,
                stationsCount: stations.length,
                // sampleDistances: stations.slice(0, 3).map(s => ({ name: s.name, distanceKm: s.distanceKm })),
                needsRefetch,
                alreadyRefetched: hasRefetchedWithLocationRef.current
            });
            
            if (needsRefetch) {
                console.log('[Location Effect] Re-fetching stations with user location for distance calculation');
                hasRefetchedWithLocationRef.current = true; // Mark as refetched to prevent loop
                fetchStationsInBound(lastFetchedRegionRef.current);
            }
        }
    }, [location, stations]); // Include stations to detect when they're loaded with null distances

    // ==================== STATION FETCHING ====================
    /**
     * Fetches stations within map bounds with performance optimizations:
     * - Blocks fetches during navigation to prevent route clearing
     * - Distance threshold check (2km) for background fetches
     * - Abort controller to cancel pending requests
     * 
     * @param mapRegion - The region to fetch stations for
     * @param isBackground - Whether this is a background fetch (debounced)
     * @param forceRefresh - Bypass navigation check (used when exiting navigation)
     */
    const fetchStationsInBound = async (mapRegion: Region, isBackground = false, forceRefresh = false) => {
        try {
            // CRITICAL: Stop fetching during navigation to prevent re-renders
            // EXCEPT when forceRefresh is true (exiting navigation)
            if (isNavigating && !forceRefresh) {
                console.log('[fetchStationsInBound] Blocked during navigation');
                return;
            }

            // Check distance threshold for background fetches (panning)
            if (isBackground && lastFetchedRegionRef.current) {
                const distance = haversineDistance(
                    lastFetchedRegionRef.current.latitude,
                    lastFetchedRegionRef.current.longitude,
                    mapRegion.latitude,
                    mapRegion.longitude
                );

                // Only fetch if moved more than 2km
                if (distance < 2) {
                    return;
                }
            }

            if (!isBackground) {
                setIsInitialLoading(true);
            }

            // Cancel previous request to prevent race conditions
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
            const controller = new AbortController();
            abortControllerRef.current = controller;

            // Calculate bounding box from region
            const minLat = mapRegion.latitude - mapRegion.latitudeDelta / 2;
            const maxLat = mapRegion.latitude + mapRegion.latitudeDelta / 2;
            const minLng = mapRegion.longitude - mapRegion.longitudeDelta / 2;
            const maxLng = mapRegion.longitude + mapRegion.longitudeDelta / 2;

            const results = await searchStationsInBound(
                {
                    minLat,
                    maxLat,
                    minLng,
                    maxLng,
                    userLat: location?.coords.latitude,
                    userLng: location?.coords.longitude,
                    maxResults: 50,
                },
                controller.signal
            );

            console.log(`[fetchStationsInBound] Loaded ${results.length} stations`, {
                hasLocation: !!location,
                userLat: location?.coords.latitude,
                userLng: location?.coords.longitude,
                sampleDistances: results.slice(0, 3).map(s => ({ name: s.name, distanceKm: s.distanceKm }))
            });

            setStations(results);
            lastFetchedRegionRef.current = mapRegion;
        } catch (error: any) {
            if (error.name === "AbortError" || error.message === "canceled") {
                console.log("Fetch aborted");
                return;
            }
            console.error("Error fetching stations:", error);
            if (!isBackground) setStations([]);
        } finally {
            if (!isBackground) setIsInitialLoading(false);
        }
    };

    // ==================== LIST STATION FETCHING ====================
    /**
     * Fetch stations using the paginated `/filter` API. 
     * Independent of the map region. Only relies on applied filters and search query.
     */
    const fetchListStations = async (page: number, filters?: StationFilterParams) => {
        // Cancel previous request to prevent race conditions
        if (listAbortControllerRef.current) {
            console.log(`[fetchListStations] Cancelling previous list request for new page ${page}`);
            listAbortControllerRef.current.abort();
        }

        // Only block if we're NOT fetching Page 0 (which might be a refetch with location)
        if (isListLoading && page !== 0) return;
        
        setIsListLoading(true);
        
        // Create new controller for this request
        const controller = new AbortController();
        listAbortControllerRef.current = controller;
        
        try {
            // Update active filters if provided
            if (filters) {
                activeFiltersRef.current = filters;
            }

            // Always merge searchQuery with active filters
            const params: StationFilterParams = {
                page,
                size: 10,
                query: searchQuery || undefined,
                ...activeFiltersRef.current,
                ...(location?.coords && {
                    userLat: location.coords.latitude,
                    userLng: location.coords.longitude
                })
            };
            
            console.log(`[fetchListStations] passing params:`, JSON.stringify(params));
            
            const paginatedResponse = await filterStations(params, controller.signal);
            
            console.log(`[fetchListStations] page ${page} returned ${paginatedResponse.content?.length} stations`);

            if (page === 0) {
                setListStations(paginatedResponse.content || []);
            } else {
                setListStations(prev => {
                    const existingIds = new Set(prev.map(s => s.id));
                    const newUniqueItems = (paginatedResponse.content || []).filter(
                        s => !existingIds.has(s.id)
                    );
                    return [...prev, ...newUniqueItems];
                });
            }

            setListPage(page);
            setHasMoreList(!paginatedResponse.last);
            
        } catch (error: any) {
            if (error.name === 'CanceledError' || error.name === 'AbortError') {
                console.log(`[fetchListStations] Request for page ${page} was cancelled`);
                return; // Don't reset loading state if we're already starting another one
            }
            console.error("Error fetching list stations:", error);
        } finally {
            // Only reset loading if this is still the active controller
            if (listAbortControllerRef.current === controller) {
                setIsListLoading(false);
            }
        }
    };

    const loadMoreListStations = () => {
        if (!isListLoading && hasMoreList) {
            fetchListStations(listPage + 1);
        }
    };

    /**
     * Debounced region change handler to prevent API spam
     */
    const handleRegionChangeComplete = (newRegion: Region) => {
        // Clear existing debounce timer
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        // Set new debounce timer (800ms)
        debounceTimerRef.current = setTimeout(() => {
            // Double-check navigation state inside timeout
            if (!isNavigating) {
                fetchStationsInBound(newRegion, true); // isBackground = true
            }
        }, 800);
    };

    // ==================== MARKER INTERACTION ====================
    const handleMarkerPress = (station: StationSearchResult) => {
        setSelectedStation(station);
        setShowQuickInfo(true);
    };

    const handleViewDetails = () => {
        if (selectedStation) {
            setShowQuickInfo(false);
            // Cache station data before navigation for instant render
            useStationCache.getState().setStation(selectedStation);
            router.push(`/station/${selectedStation.id}`);
        }
    };

    const handleBook = () => {
        if (selectedStation) {
            setShowQuickInfo(false);
            router.push(`/booking/selectVehicle?stationId=${selectedStation.id}`);
        }
    };

    // ==================== NAVIGATION ====================
    /**
     * Handles navigation with instant feedback and polyline simplification.
     * Performance: Simplifies route coordinates from 5000+ to ~200-500 points.
     */
    const handleNavigate = async () => {
        if (!selectedStation || !location) return;

        console.log('[handleNavigate] START - Station:', selectedStation.name);
        setShowQuickInfo(false);
        setIsInitialLoading(true); // Instant feedback

        try {
            const routeData = await getRoute(
                location.coords.latitude,
                location.coords.longitude,
                selectedStation.latitude,
                selectedStation.longitude
            );

            if (routeData && routeData.encodedPolyline) {
                const points = mapboxPolyline.decode(routeData.encodedPolyline);
                const coords = points.map((point: [number, number]) => ({
                    latitude: point[0],
                    longitude: point[1],
                }));

                // CRITICAL: Simplify polyline to prevent OOM crashes
                // 0.0001 degrees ≈ 11 meters, good balance for urban navigation
                const simplifiedCoords = simplifyPolyline(coords, 0.0001);
                
                console.log(`[handleNavigate] Polyline simplified: ${coords.length} → ${simplifiedCoords.length} points`);

                // Only set navigation state AFTER route is calculated
                // Store route metrics
                setRouteDistance(routeData.distance);
                setRouteDuration(routeData.duration);
                
                console.log('[handleNavigate] Setting routeCoordinates and isNavigating=true');
                setRouteCoordinates(simplifiedCoords);
                setIsNavigating(true);
                
                // Show loading spinner to cover MapView remount
                setIsNavigationLoading(true);
                
                // CRITICAL: Force MapView re-render by changing key
                // This ensures the native layer detects the new Polyline component
                setMapKey(prev => prev + 1);
                console.log('[handleNavigate] MapKey incremented to force re-render');
                
                // Hide loading spinner after MapView remounts
                setTimeout(() => {
                    setIsNavigationLoading(false);
                }, 250);
                
                console.log('[handleNavigate] State updated, useEffect should trigger');
            }
        } catch (error) {
            console.error("[handleNavigate] Navigation failed:", error);
        } finally {
            setIsInitialLoading(false);
        }
    };

    /**
     * Cancels navigation and reverts to normal map view.
     * CRITICAL: Must re-fetch stations to prevent crash from empty marker array.
     */
    const cancelNavigation = async () => {
        console.log('[cancelNavigation] START - Clearing navigation state');
        
        // Step 1: Clear navigation state FIRST
        setIsNavigating(false);
        setRouteCoordinates([]);
        setSelectedStation(null);

        // Step 2: Get current map region for refetch
        let currentRegion: Region;
        try {
            const camera = await mapRef.current?.getCamera();
            if (camera) {
                currentRegion = {
                    latitude: camera.center.latitude,
                    longitude: camera.center.longitude,
                    latitudeDelta: 0.1,
                    longitudeDelta: 0.1,
                };
                console.log('[cancelNavigation] Got camera region:', currentRegion.latitude, currentRegion.longitude);
            } else {
                throw new Error('No camera');
            }
        } catch (error) {
            // Fallback chain: lastFetched → userLocation → INITIAL_REGION
            if (lastFetchedRegionRef.current) {
                currentRegion = lastFetchedRegionRef.current;
                console.log('[cancelNavigation] Using lastFetched region');
            } else if (location) {
                currentRegion = {
                    latitude: location.coords.latitude,
                    longitude: location.coords.longitude,
                    latitudeDelta: 0.1,
                    longitudeDelta: 0.1,
                };
                console.log('[cancelNavigation] Using user location');
            } else {
                currentRegion = INITIAL_REGION;
                console.log('[cancelNavigation] Using INITIAL_REGION fallback');
            }
        }

        // Step 3: Restore camera to standard zoom
        mapRef.current?.animateCamera(
            {
                center: {
                    latitude: currentRegion.latitude,
                    longitude: currentRegion.longitude,
                },
                zoom: 15,
                pitch: 0,
                heading: 0,
            },
            { duration: 1000 }
        );
        console.log('[cancelNavigation] Camera animation started');

        // Step 4: CRITICAL - Force re-fetch stations immediately
        // Use forceRefresh=true to bypass isNavigating check
        console.log('[cancelNavigation] Forcing station refetch...');
        await fetchStationsInBound(currentRegion, false, true);
        console.log('[cancelNavigation] COMPLETE');
    };

    // ==================== MAP CONTROLS ====================
    const centerToUserLocation = () => {
        if (location) {
            mapRef.current?.animateCamera(
                {
                    center: {
                        latitude: location.coords.latitude,
                        longitude: location.coords.longitude,
                    },
                    zoom: 15,
                    pitch: 0,
                    heading: 0,
                },
                { duration: 1000 }
            );
        }
    };

    // ==================== RETURN INTERFACE ====================
    return {
        // State
        location,
        stations,
        selectedStation,
        routeCoordinates,
        routeDistance,
        routeDuration,
        remainingDistance: (() => {
            // Calculate remaining distance along the route (not straight-line)
            if (!location || routeCoordinates.length === 0) return 0;
            
            const userLat = location.coords.latitude;
            const userLng = location.coords.longitude;
            
            // Find nearest point on route to current location
            let nearestIndex = 0;
            let minDistance = Infinity;
            
            routeCoordinates.forEach((coord, index) => {
                const dist = haversineDistance(userLat, userLng, coord.latitude, coord.longitude);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestIndex = index;
                }
            });
            
            // Sum distances from nearest point to end of route
            let remaining = 0;
            for (let i = nearestIndex; i < routeCoordinates.length - 1; i++) {
                remaining += haversineDistance(
                    routeCoordinates[i].latitude,
                    routeCoordinates[i].longitude,
                    routeCoordinates[i + 1].latitude,
                    routeCoordinates[i + 1].longitude
                );
            }
            
            return remaining;
        })(),
        isNavigating,
        isInitialLoading,
        isNavigationLoading,
        viewMode,
        searchQuery,
        mapKey,
        
        listStations,
        listPage,
        hasMoreList,
        isListLoading,
        fetchListStations,
        loadMoreListStations,

        // Modals
        showPermissionModal,
        permissionStatus,
        showManualInput,
        showQuickInfo,

        // Handlers
        handleMarkerPress,
        handleNavigate,
        cancelNavigation,
        centerToUserLocation,
        handleRegionChangeComplete,
        requestLocationPermission,
        handleEnterManually,
        handleManualLocationSet,
        handleViewDetails,
        handleBook,
        setViewMode,
        setSearchQuery,
        filterMeta,

        // Refs
        mapRef,
        initialRegion: initialRegionRef.current,

        // Setters
        setShowQuickInfo,
        setShowPermissionModal,
    };
};
