import { TrendingUp, Users, Zap, CreditCard, CalendarCheck } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { summaryStats } from "../data/dashboard-data"

export function DashboardStats() {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
  }

  return (
    <div className="*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card className="@container/card">
        <CardHeader>
          <CardDescription className="flex items-center gap-2"><CreditCard className="size-4" /> Total Revenue</CardDescription>
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={formatCurrency(summaryStats.totalRevenue)}>
            {formatCurrency(summaryStats.totalRevenue)}
          </CardTitle>
          <CardAction>
            <Badge variant="outline" className="text-green-600 bg-green-50 dark:bg-green-950 dark:text-green-400">
              <TrendingUp className="mr-1 size-3" />
              +{summaryStats.revenueGrowth}%
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className="flex-col items-start gap-1.5 text-sm">
          <div className="text-muted-foreground">
            Revenue from successful bookings
          </div>
        </CardFooter>
      </Card>
      
      <Card className="@container/card">
        <CardHeader>
          <CardDescription className="flex items-center gap-2"><CalendarCheck className="size-4" /> Total Bookings</CardDescription>
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={String(summaryStats.totalBookings)}>
            {summaryStats.totalBookings}
          </CardTitle>
          <CardAction>
            <Badge variant="outline" className="text-green-600 bg-green-50 dark:bg-green-950 dark:text-green-400">
              <TrendingUp className="mr-1 size-3" />
              +{summaryStats.bookingsGrowth}%
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className="flex-col items-start gap-1.5 text-sm">
          <div className="text-muted-foreground">
            All charging sessions scheduled
          </div>
        </CardFooter>
      </Card>

      <Card className="@container/card">
        <CardHeader>
          <CardDescription className="flex items-center gap-2"><Zap className="size-4" /> Active Stations</CardDescription>
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={String(summaryStats.activeStations)}>
            {summaryStats.activeStations}
          </CardTitle>
          <CardAction>
            <Badge variant="outline" className="text-muted-foreground bg-muted">
              {summaryStats.stationsGrowth}%
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className="flex-col items-start gap-1.5 text-sm">
          <div className="text-muted-foreground">
            Stations currently online
          </div>
        </CardFooter>
      </Card>

      <Card className="@container/card">
        <CardHeader>
          <CardDescription className="flex items-center gap-2"><Users className="size-4" /> Total Customers</CardDescription>
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={summaryStats.totalCustomers.toLocaleString()}>
            {summaryStats.totalCustomers.toLocaleString()}
          </CardTitle>
          <CardAction>
            <Badge variant="outline" className="text-green-600 bg-green-50 dark:bg-green-950 dark:text-green-400">
              <TrendingUp className="mr-1 size-3" />
              +{summaryStats.customersGrowth}%
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className="flex-col items-start gap-1.5 text-sm">
          <div className="text-muted-foreground">
            Unique users registered
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
