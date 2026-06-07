import axiosInstance from "@/utils/axiosInstance";
import { ApiResponse } from "@/types/booking.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export interface ZaloPayOrderRequest {
    invoiceId: number;
    userId: number;
    amount: number;
    description: string;
    redirectUrl?: string;
}

export interface InvoiceResponse {
    id: number;
    bookingId?: number;
    chargingSessionId?: number;
    userId: number;
    number: string;
    totalCost: number;
    purpose: string;
    status: string;
    createdAt?: string;
}

export interface ZaloPayOrderResponse {
    orderUrl: string;
    zpTransToken: string;
    appTransId: string;
}

import * as ExpoLinking from "expo-linking";

/**
 * Creates a ZaloPay Sandbox order and returns App-to-App URLs
 */
export const createZaloPayOrder = async (request: ZaloPayOrderRequest): Promise<ZaloPayOrderResponse> => {
    // Automatically inject a deep link redirect URL if not provided
    // This dynamically handles both Expo Go (exp://) and Standalone build (com.evgo.mobile://)
    if (!request.redirectUrl) {
        request.redirectUrl = ExpoLinking.createURL('booking');
    }

    const res = await axiosInstance.post<ApiResponse<ZaloPayOrderResponse>>(
        `${API_BACKEND_URL}/api/v1/zalopay/orders`,
        request
    );
    return res.data.data;
};

import { Linking, Platform } from "react-native";

/**
 * Handles ZaloPay App-to-App deep linking with Web Gateway fallback
 */
export const processZaloPayPayment = async (order: ZaloPayOrderResponse) => {
    // ZaloPay Sandbox uses app_id 2553 / 2554. We will extract it from orderUrl if possible, or use default 2553.
    const appId = 2553;
    const deepLink = `zalopay://app?app_id=${appId}&zptranstoken=${order.zpTransToken}`;

    try {
        if (Platform.OS === 'ios') {
            const canOpen = await Linking.canOpenURL(deepLink);
            if (canOpen) {
                await Linking.openURL(deepLink);
                return;
            }
        } else {
            // Android can bypass canOpenURL to avoid Intent Filter requirement in manifest
            await Linking.openURL(deepLink);
            return;
        }
    } catch (error) {
        console.log("Cannot open ZaloPay app, falling back to Web", error);
    }

    // Fallback to Web Gateway
    if (order.orderUrl) {
        await Linking.openURL(order.orderUrl);
    }
};

export const getInvoiceByBookingId = async (bookingId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/booking/${bookingId}`
    );
    return res.data.data;
};

export const getInvoiceByChargingSessionId = async (sessionId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/session/${sessionId}`
    );
    return res.data.data;
};

export const checkUnpaidInvoices = async (): Promise<boolean> => {
    const res = await axiosInstance.get<ApiResponse<boolean>>(
        `${API_BACKEND_URL}/api/v1/invoices/unpaid/check`
    );
    return res.data.data;
};
