import { useQuery } from "@tanstack/react-query";
import { getOwnerBookingsApi, GetOwnerBookingsParams } from "@/apis/stationOwner/stationOwnerApi";

export const useOwnerBookings = (params: GetOwnerBookingsParams) => {
    return useQuery({
        queryKey: ["owner-bookings", params],
        queryFn: () => getOwnerBookingsApi(params),
    });
};

export const useOwnerBookingStats = () => {
    return useQuery({
        queryKey: ["owner-booking-stats"],
        queryFn: () => import("@/apis/stationOwner/stationOwnerApi").then(m => m.getOwnerBookingStatsApi()),
    });
};

export const useOwnerInvoiceStats = () => {
    return useQuery({
        queryKey: ["owner-invoice-stats"],
        queryFn: () => import("@/apis/stationOwner/stationOwnerApi").then(m => m.getOwnerInvoiceStatsApi()),
    });
};

export const useOwnerStationStats = () => {
    return useQuery({
        queryKey: ["owner-station-stats"],
        queryFn: () => import("@/apis/stationOwner/stationOwnerApi").then(m => m.getOwnerStationStatsApi()),
    });
};
