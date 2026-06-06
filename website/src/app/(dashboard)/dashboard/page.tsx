import { DashboardStats } from "./components/dashboard-stats";
import { DashboardCharts } from "./components/dashboard-charts";
import { RecentBookingsTable } from "./components/recent-bookings-table";

export default function Page() {
    return (
        <div className="flex flex-col gap-8 pb-8">
            {/* Page Title and Description */}
            <div className="px-4 lg:px-6">
                <div className="flex flex-col gap-2">
                    <h1 className="font-bold text-2xl tracking-tight">
                        Dashboard
                    </h1>
                    <p className="text-muted-foreground">
                        Welcome to your admin dashboard
                    </p>
                </div>
            </div>

            <div className="@container/main space-y-8 px-4 lg:px-6">
                <DashboardStats />
                <DashboardCharts />
                <RecentBookingsTable />
            </div>
        </div>
    );
}
