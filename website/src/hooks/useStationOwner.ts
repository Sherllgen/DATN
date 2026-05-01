import { useQuery } from "@tanstack/react-query";
import { getOwnerBookingsApi, GetOwnerBookingsParams } from "@/apis/stationOwner/stationOwnerApi";

export const useOwnerBookings = (params: GetOwnerBookingsParams) => {
    return useQuery({
        queryKey: ["owner-bookings", params],
        queryFn: () => getOwnerBookingsApi(params),
    });
};
