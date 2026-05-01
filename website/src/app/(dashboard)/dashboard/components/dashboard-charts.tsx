"use client"

import { CartesianGrid, Line, LineChart, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts"

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
import { revenueData } from "../data/dashboard-data"

const chartConfig = {
  revenue: {
    label: "Revenue",
    color: "hsl(var(--chart-1))",
  },
  bookings: {
    label: "Bookings",
    color: "hsl(var(--chart-2))",
  },
} satisfies ChartConfig

export function DashboardCharts() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>Revenue Overview</CardTitle>
          <CardDescription>
            Monthly revenue from successful charging sessions
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="min-h-[250px] w-full">
            <LineChart accessibilityLayer data={revenueData} margin={{ top: 10, right: 10, left: 20, bottom: 0 }}>
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="month"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tickFormatter={(value) => value.slice(0, 3)}
              />
              <YAxis 
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => `${value / 1000000}M`}
                width={40}
              />
              <ChartTooltip cursor={false} content={<ChartTooltipContent />} />
              <Line
                dataKey="revenue"
                type="monotone"
                stroke="var(--color-revenue)"
                strokeWidth={2}
                dot={false}
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
            Number of monthly charging bookings
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="min-h-[250px] w-full">
            <LineChart accessibilityLayer data={revenueData} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="month"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tickFormatter={(value) => value.slice(0, 3)}
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
                stroke="var(--color-bookings)"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ChartContainer>
        </CardContent>
      </Card>
    </div>
  )
}
