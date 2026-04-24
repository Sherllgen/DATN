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