/**
 * Station-related TypeScript types and interfaces
 * Based on backend DTOs from StationResponse and StationSearchResult
 */

export enum StationStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
    MAINTENANCE = "MAINTENANCE",
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
    createdAt: string;
    updatedAt: string;
}

export interface StationSearchResult extends Station {
    distanceKm: number; // Distance from user location
    availableChargersCount: number; // Number of available chargers
    totalChargersCount: number; // Total number of chargers
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

// API Response wrapper
export interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}

// Props for marker availability status
export type MarkerStatus = "available" | "occupied" | "maintenance";

// Amenity types (can be extended based on backend)
export interface Amenity {
    id: string;
    name: string;
    icon: string; // Icon name from vector-icons
}
