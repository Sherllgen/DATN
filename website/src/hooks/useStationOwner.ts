import { useQuery } from "@tanstack/react-query";
import {
    getOwnerBookingsApi,
    getOwnerBookingStatsApi,
    getOwnerInvoiceStatsApi,
    getOwnerStationStatsApi,
    getOwnerMonthlyChartApi,
    GetOwnerBookingsParams,
} from "@/apis/stationOwner/stationOwnerApi";

export const useOwnerBookings = (params: GetOwnerBookingsParams) => {
    return useQuery({
        queryKey: ["owner-bookings", params],
        queryFn: () => getOwnerBookingsApi(params),
    });
};

export const useOwnerBookingStats = () => {
    return useQuery({
        queryKey: ["owner-booking-stats"],
        queryFn: () => getOwnerBookingStatsApi(),
    });
};

export const useOwnerInvoiceStats = () => {
    return useQuery({
        queryKey: ["owner-invoice-stats"],
        queryFn: () => getOwnerInvoiceStatsApi(),
    });
};

export const useOwnerStationStats = () => {
    return useQuery({
        queryKey: ["owner-station-stats"],
        queryFn: () => getOwnerStationStatsApi(),
    });
};

export const useOwnerMonthlyChart = () => {
    return useQuery({
        queryKey: ["owner-monthly-chart"],
        queryFn: () => getOwnerMonthlyChartApi(),
    });
};
