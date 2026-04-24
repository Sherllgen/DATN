export interface InvoiceResponse {
    id: number;
    bookingId?: number;
    chargingSessionId?: number;
    userId: number;
    number: string;
    totalCost: number;
    purpose: string;
    status: 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED';
    createdAt: string;
}

export interface PaymentOrderResponse {
    orderUrl: string;
    zpTransToken: string;
    appTransId: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}