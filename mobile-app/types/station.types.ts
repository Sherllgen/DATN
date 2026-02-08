/**
 * Station-related TypeScript types and interfaces
 * Based on backend DTOs from StationResponse and StationSearchResult
 */

export enum StationStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
}

export interface ChargerSummary {
    connectorType: string;
    available: number;
    total: number;
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
    createdAt: string;
    updatedAt: string;
}

export interface StationSearchResult extends Station {
    distanceKm: number; // Distance from user location
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

// Props for marker status
export type MarkerStatus = "available" | "occupied";
