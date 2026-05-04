"use client"

import { CartesianGrid, Line, LineChart, XAxis, YAxis, ResponsiveContainer } from "recharts"

import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {
    ChartConfig,
    ChartContainer,
    ChartTooltip,
    ChartTooltipContent,
} from "@/components/ui/chart"
import { Skeleton } from "@/components/ui/skeleton"
import { useOwnerMonthlyChart } from "@/hooks/useStationOwner"

interface MonthlyChartEntry {
    month: string;
    revenue: number;
    bookings: number;
}

const chartConfig = {
    revenue: {
        label: "Revenue (VND)",
        color: "#22c55e",
    },
    bookings: {
        label: "Bookings",
        color: "#3b82f6",
    },
} satisfies ChartConfig

export function DashboardCharts() {
    const { data: chartResponse, isLoading } = useOwnerMonthlyChart();
    const chartData: MonthlyChartEntry[] = chartResponse?.data ?? [];

    if (isLoading) {
        return (
            <div className="grid gap-4 md:grid-cols-2">
                {[1, 2].map((i) => (
                    <Card key={i}>
                        <CardHeader>
                            <Skeleton className="h-5 w-[160px]" />
                            <Skeleton className="h-4 w-[240px] mt-1" />
                        </CardHeader>
                        <CardContent>
                            <Skeleton className="h-[250px] w-full" />
                        </CardContent>
                    </Card>
                ))}
            </div>
        );
    }

    return (
        <div className="grid gap-4 md:grid-cols-2">
            <Card>
                <CardHeader>
                    <CardTitle>Revenue Overview</CardTitle>
                    <CardDescription>
                        Monthly revenue from completed charging sessions
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <ChartContainer config={chartConfig} className="min-h-[250px] w-full">
                        <LineChart accessibilityLayer data={chartData} margin={{ top: 10, right: 10, left: 20, bottom: 0 }}>
                            <CartesianGrid vertical={false} />
                            <XAxis
                                dataKey="month"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                            />
                            <YAxis
                                tickLine={false}
                                axisLine={false}
                                tickFormatter={(value) => `${(value / 1000000).toFixed(0)}M`}
                                width={40}
                            />
                            <ChartTooltip
                                cursor={false}
                                content={<ChartTooltipContent formatter={(value) =>
                                    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(value))
                                } />}
                            />
                            <Line
                                dataKey="revenue"
                                type="monotone"
                                stroke="#22c55e"
                                strokeWidth={2}
                                dot={{ r: 3, fill: "#22c55e" }}
                                activeDot={{ r: 6 }}
                            />
                        </LineChart>
                    </ChartContainer>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Bookings Overview</CardTitle>
                    <CardDescription>
                        Number of completed monthly charging bookings
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <ChartContainer config={chartConfig} className="min-h-[250px] w-full">
                        <LineChart accessibilityLayer data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
                            <CartesianGrid vertical={false} />
                            <XAxis
                                dataKey="month"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                            />
                            <YAxis
                                tickLine={false}
                                axisLine={false}
                                width={30}
                            />
                            <ChartTooltip cursor={false} content={<ChartTooltipContent />} />
                            <Line
                                dataKey="bookings"
                                type="monotone"
                                stroke="#3b82f6"
                                strokeWidth={2}
                                dot={{ r: 3, fill: "#3b82f6" }}
                                activeDot={{ r: 6 }}
                            />
                        </LineChart>
                    </ChartContainer>
                </CardContent>
            </Card>
        </div>
    )
}
