export interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface DurationConfigResponse {
    durations: number[]; // Array of hours e.g., [1.0, 1.5, 2.0]
}

export enum DayStatus {
    AVAILABLE = 'AVAILABLE',
    FULL = 'FULL',
    UNAVAILABLE = 'UNAVAILABLE'
}

export interface CalendarStatusResponse {
    date: string; // "YYYY-MM-DD"
    status: DayStatus;
}

export interface AvailableSlotResponse {
    startTime: string; // "HH:mm:ss"
    endTime: string; // "HH:mm:ss"
    availablePorts: number;
}

export interface CheckAvailabilityRequest {
    stationId: number;
    chargerId: number;
    portNumber: number;
    startTime: string; // "YYYY-MM-DDTHH:mm:ss"
    endTime: string;   // "YYYY-MM-DDTHH:mm:ss"
}

export interface CreateBookingRequest extends CheckAvailabilityRequest {
    vehicleId: number;
}

export enum BookingStatus {
    PENDING = 'PENDING',
    CONFIRMED = 'CONFIRMED',
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED'
}

export interface BookingResponse {
    id: number;
    userId: number;
    stationId: number;
    chargerId: number;
    portNumber: number;
    startTime: string; // "YYYY-MM-DDTHH:mm:ss"
    endTime: string;   // "YYYY-MM-DDTHH:mm:ss"
    status: BookingStatus;
    totalPrice: number;
    createdAt: string;

    // Flattened UI fields
    stationName?: string;
    stationAddress?: string;
    chargerName?: string;
    connectorType?: string;
    maxPower?: number;

    // Vehicle fields
    vehicleId?: number;
    vehicleBrand?: string;
    vehicleModelName?: string;
}
