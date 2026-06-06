export const summaryStats = {
  totalRevenue: 12500000, // 12.5M VND
  revenueGrowth: 12.5,
  totalBookings: 342,
  bookingsGrowth: 8.2,
  activeStations: 15,
  stationsGrowth: 0,
  totalCustomers: 1284,
  customersGrowth: 15.3,
}

export const revenueData = [
  { month: "Jan", revenue: 8500000, bookings: 120 },
  { month: "Feb", revenue: 9200000, bookings: 145 },
  { month: "Mar", revenue: 10500000, bookings: 190 },
  { month: "Apr", revenue: 11200000, bookings: 210 },
  { month: "May", revenue: 12500000, bookings: 250 },
  { month: "Jun", revenue: 15000000, bookings: 300 },
]

export type BookingStatus = "SUCCESS" | "PENDING" | "CANCELLED"

export interface Booking {
  id: string
  customerName: string
  stationName: string
  dateTime: string
  amount: number
  status: BookingStatus
}
