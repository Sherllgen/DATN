import axiosInstance from "@/utils/axiosInstance";
import {
    ApiResponse,
    SearchNearbyParams,
    SearchTextParams,
    SearchInBoundParams,
    Station,
    StationSearchResult,
} from "@/types/station.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

/**
 * Search for nearby charging stations based on GPS coordinates
 */
export const searchNearbyStations = async (
    params: SearchNearbyParams
): Promise<StationSearchResult[]> => {
    const { latitude, longitude, radiusKm = 5.0, maxResults = 20 } = params;

    const res = await axiosInstance.get<ApiResponse<StationSearchResult[]>>(
        `${API_BACKEND_URL}/api/v1/stations/search/nearby`,
        {
            params: {
                latitude,
                longitude,
                radiusKm,
                maxResults,
            },
        }
    );

    return res.data.data;
};

/**
 * Search stations by text query (name, address, description)
 */
export const searchStationsByText = async (
    params: SearchTextParams
): Promise<StationSearchResult[]> => {
    const { query, latitude, longitude, maxResults = 20 } = params;

    const res = await axiosInstance.get<ApiResponse<StationSearchResult[]>>(
        `${API_BACKEND_URL}/api/v1/stations/search/text`,
        {
            params: {
                query,
                latitude,
                longitude,
                maxResults,
            },
        }
    );

    return res.data.data;
};

/**
 * Get station details by ID
 */
export const getStationById = async (id: number): Promise<Station> => {
    const res = await axiosInstance.get<ApiResponse<Station>>(
        `${API_BACKEND_URL}/api/v1/stations/${id}`
    );

    return res.data.data;
};

/**
 * Get all stations (public)
 */
export const getAllStations = async (): Promise<Station[]> => {
    const res = await axiosInstance.get<ApiResponse<Station[]>>(
        `${API_BACKEND_URL}/api/v1/stations`
    );

    return res.data.data;
};

/**
 * Search stations within a bounding box (viewport)
 * Optionally calculates distance from user's location
 */
export const searchStationsInBound = async (
    params: SearchInBoundParams
): Promise<StationSearchResult[]> => {
    const { minLat, maxLat, minLng, maxLng, userLat, userLng, maxResults = 50 } = params;

    const res = await axiosInstance.get<ApiResponse<StationSearchResult[]>>(
        `${API_BACKEND_URL}/api/v1/stations/in-bound`,
        {
            params: {
                minLat,
                maxLat,
                minLng,
                maxLng,
                userLat,
                userLng,
                maxResults,
            },
        }
    );

    return res.data.data;
};
