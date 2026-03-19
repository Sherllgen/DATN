export interface ChargingActivity {
    id: string;
    percentage: number;
    kwh: number;
    total: number;
    currency: string;
}

export interface HistoryActivity {
    id: string;
    date: string;
    duration: string;
    energy: number;
}

export const mockChargingInfo: ChargingActivity = {
    id: "1",
    percentage: 76,
    kwh: 64,
    total: 6.13,
    currency: "$",
};

export const mockHistory: HistoryActivity[] = [
    {
        id: "h1",
        date: "26 October 2023",
        duration: "70 min",
        energy: 55,
    },
    {
        id: "h2",
        date: "26 October 2025",
        duration: "70 min",
        energy: 50,
    },
    {
        id: "h3",
        date: "26 October 2025",
        duration: "70 min",
        energy: 50,
    },
    {
        id: "h4",
        date: "26 October 2025",
        duration: "70 min",
        energy: 50,
    },
];
