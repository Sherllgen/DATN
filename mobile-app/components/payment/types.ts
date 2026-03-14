export interface BillingItem {
    id: string;
    title: string;
    date: string;
    amount: string;
    isPaid: boolean;
}

export interface BillingGroup {
    period: string;
    items: BillingItem[];
}
