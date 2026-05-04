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

export const recentBookings: Booking[] = [
  {
    id: "BK-001",
    customerName: "Nguyễn Văn A",
    stationName: "Trạm sạc Vincom Q1",
    dateTime: "2024-05-01T08:30:00",
    amount: 150000,
    status: "SUCCESS",
  },
  {
    id: "BK-002",
    customerName: "Trần Thị B",
    stationName: "Trạm sạc AEON Mall",
    dateTime: "2024-05-01T09:15:00",
    amount: 85000,
    status: "SUCCESS",
  },
  {
    id: "BK-003",
    customerName: "Lê Văn C",
    stationName: "Trạm sạc Landmark 81",
    dateTime: "2024-05-01T10:00:00",
    amount: 0,
    status: "PENDING",
  },
  {
    id: "BK-004",
    customerName: "Phạm Thu D",
    stationName: "Trạm sạc Gigamall",
    dateTime: "2024-05-01T11:45:00",
    amount: 200000,
    status: "CANCELLED",
  },
  {
    id: "BK-005",
    customerName: "Hoàng Minh E",
    stationName: "Trạm sạc Vincom Q1",
    dateTime: "2024-05-01T13:20:00",
    amount: 120000,
    status: "SUCCESS",
  },
]
