export type BookingStatus = "Upcoming" | "Completed" | "Cancelled";

export interface Booking {
    id: string;
    date: string;
    time: string;
    status: BookingStatus;
    station: {
        name: string;
        address: string;
        coordinate: { lat: number; lng: number };
    };
    charger: {
        connectorType: string;
        maxPower: string;
    };
    duration: string;
    amount: number;
}

export const mockBookings: Booking[] = [
    {
        id: "BK-12345",
        date: "Dec 17, 2024",
        time: "10:00 AM",
        status: "Upcoming",
        station: {
            name: "Walgreens - Brooklyn, NY",
            address: "Brooklyn, 589 Prospect Avenue",
            coordinate: { lat: 40.66, lng: -73.98 }
        },
        charger: {
            connectorType: "Tesla (Plug)",
            maxPower: "100 kW"
        },
        duration: "1 hour",
        amount: 14.25
    },
    {
        id: "BK-12346",
        date: "Dec 15, 2024",
        time: "02:30 PM",
        status: "Completed",
        station: {
            name: "Whole Foods - Austin, TX",
            address: "525 N Lamar Blvd",
            coordinate: { lat: 30.27, lng: -97.75 }
        },
        charger: {
            connectorType: "CCS",
            maxPower: "150 kW"
        },
        duration: "45 mins",
        amount: 22.50
    },
    {
        id: "BK-12347",
        date: "Dec 10, 2024",
        time: "09:00 AM",
        status: "Cancelled",
        station: {
            name: "Target - Seattle, WA",
            address: "1401 2nd Ave",
            coordinate: { lat: 47.61, lng: -122.34 }
        },
        charger: {
            connectorType: "Type 2",
            maxPower: "22 kW"
        },
        duration: "2 hours",
        amount: 8.00
    }
];
