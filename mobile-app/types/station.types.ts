/**
 * Station-related TypeScript types and interfaces
 * Based on backend DTOs from StationResponse and StationSearchResult
 */

export enum StationStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
    SUSPENDED = "SUSPENDED",
}

export interface ChargerSummary {
    connectorType: string;
    available: number;
    total: number;
}

export interface StationOpeningHours {
    id: number;
    dayOfWeek: string; // "MONDAY", "TUESDAY", etc.
    openTime: string | null; // ISO time format "HH:MM:SS" or null for 24/7
    closeTime: string | null; // ISO time format "HH:MM:SS" or null for 24/7
    isOpen: boolean;
}

export interface Station {
    id: number;
    ownerId: number;
    name: string;
    description: string | null;
    address: string;
    latitude: number;
    longitude: number;
    rate: number; // Rating out of 5
    status: StationStatus;
    imageUrls: string[];
    isFlaggedLowQuality: boolean;
    availableChargersCount: number;
    totalChargersCount: number;
    chargers: ChargerSummary[];
    openingHours: StationOpeningHours[];
    createdAt: string;
    updatedAt: string;
}

export interface StationSearchResult extends Station {
    distanceKm: number | null; // Distance from user location
}

export interface SearchNearbyParams {
    latitude: number;
    longitude: number;
    radiusKm?: number; // Default: 5.0
    maxResults?: number; // Default: 20
}

export interface SearchTextParams {
    query: string;
    latitude?: number;
    longitude?: number;
    maxResults?: number; // Default: 20
}

export interface SearchInBoundParams {
    minLat: number;
    maxLat: number;
    minLng: number;
    maxLng: number;
    userLat?: number; // Optional: user's latitude for distance calculation
    userLng?: number; // Optional: user's longitude for distance calculation
    maxResults?: number; // Default: 20
}


// API Response wrapper
export interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}

// Paginated Response Wrapper
export interface PaginatedResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// Props for marker status
export type MarkerStatus = "available" | "occupied";

// Filter Types
export interface FilterMetadata {
    minPower: number;
    maxPower: number;
    connectorTypes: string[];
    statuses: StationStatus[] | string[];
}

export interface StationFilterParams {
    minPower?: number;
    maxPower?: number;
    connectorTypes?: string[];
    status?: string | StationStatus;
    query?: string;
    userLat?: number;
    userLng?: number;
    page?: number;
    size?: number;
}
