"use client"

import { Users, Zap, CreditCard, CalendarCheck } from "lucide-react"

import {
  Card,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { useOwnerBookingStats, useOwnerInvoiceStats, useOwnerStationStats } from "@/hooks/useStationOwner"
import { Skeleton } from "@/components/ui/skeleton"

export function DashboardStats() {
  const { data: bookingStats, isLoading: isBookingLoading } = useOwnerBookingStats();
  const { data: invoiceStats, isLoading: isInvoiceLoading } = useOwnerInvoiceStats();
  const { data: stationStats, isLoading: isStationLoading } = useOwnerStationStats();

  const isLoading = isBookingLoading || isInvoiceLoading || isStationLoading;

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
  }

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i} className="@container/card">
            <CardHeader>
              <Skeleton className="h-4 w-[150px]" />
              <Skeleton className="h-8 w-[200px] mt-2" />
            </CardHeader>
          </Card>
        ))}
      </div>
    );
  }

  const totalRevenue = invoiceStats?.data?.totalRevenue || 0;
  const totalBookings = bookingStats?.data?.totalBookings || 0;
  const totalCustomers = bookingStats?.data?.totalCustomers || 0;
  const activeStations = stationStats?.data?.activeStations || 0;

  return (
    <div className="*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card className="@container/card">
        <CardHeader>
          <CardDescription className="flex items-center gap-2"><CreditCard className="size-4" /> Total Revenue</CardDescription>
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={formatCurrency(totalRevenue)}>
            {formatCurrency(totalRevenue)}
          </CardTitle>
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
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={String(totalBookings)}>
            {totalBookings}
          </CardTitle>
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
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={String(activeStations)}>
            {activeStations}
          </CardTitle>
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
          <CardTitle className="text-xl font-semibold tabular-nums @[250px]/card:text-2xl truncate min-w-0" title={totalCustomers.toLocaleString()}>
            {totalCustomers.toLocaleString()}
          </CardTitle>
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
