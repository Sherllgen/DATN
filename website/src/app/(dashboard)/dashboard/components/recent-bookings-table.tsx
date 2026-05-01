"use client"

import * as React from "react"
import {
  ColumnDef,
  ColumnFiltersState,
  SortingState,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
} from "@tanstack/react-table"
import { ArrowUpDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react"

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Booking, recentBookings } from "../data/dashboard-data"

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

const getStatusBadge = (status: string) => {
  switch (status) {
    case "SUCCESS":
      return <Badge variant="outline" className="text-green-600 border-green-200 bg-green-50 dark:bg-green-950 dark:border-green-800 dark:text-green-400">Success</Badge>
    case "PENDING":
      return <Badge variant="outline" className="text-yellow-600 border-yellow-200 bg-yellow-50 dark:bg-yellow-950 dark:border-yellow-800 dark:text-yellow-400">Pending</Badge>
    case "CANCELLED":
      return <Badge variant="outline" className="text-red-600 border-red-200 bg-red-50 dark:bg-red-950 dark:border-red-800 dark:text-red-400">Cancelled</Badge>
    default:
      return <Badge variant="outline">{status}</Badge>
  }
}

export const columns: ColumnDef<Booking>[] = [
  {
    accessorKey: "id",
    header: "ID",
    cell: ({ row }) => <div className="font-medium">{row.getValue("id")}</div>,
  },
  {
    accessorKey: "customerName",
    header: "Customer",
  },
  {
    accessorKey: "stationName",
    header: ({ column }) => {
      return (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="cursor-pointer -ml-4"
        >
          Station
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      )
    },
  },
  {
    accessorKey: "dateTime",
    header: ({ column }) => {
      return (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="cursor-pointer -ml-4"
        >
          Date & Time
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      )
    },
    cell: ({ row }) => <div>{new Date(row.getValue("dateTime")).toLocaleString('vi-VN')}</div>,
  },
  {
    accessorKey: "status",
    header: ({ column }) => {
      return (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="cursor-pointer -ml-4"
        >
          Status
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      )
    },
    cell: ({ row }) => getStatusBadge(row.getValue("status")),
  },
  {
    accessorKey: "amount",
    header: () => <div className="text-right">Amount</div>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue("amount"))
      return <div className="text-right font-medium">{formatCurrency(amount)}</div>
    },
  },
]

export function RecentBookingsTable() {
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: "dateTime", desc: true },
  ])

  // Use more mock data so pagination is visible
  const expandedData = React.useMemo(() => {
    const data = [...recentBookings]
    for (let i = 6; i <= 25; i++) {
      data.push({
        id: `BK-00${i}`,
        customerName: `Customer ${i}`,
        stationName: i % 2 === 0 ? "Trạm sạc Vincom Q1" : "Trạm sạc Landmark 81",
        dateTime: new Date(Date.now() - i * 3600000).toISOString(),
        amount: Math.floor(Math.random() * 200000) + 50000,
        status: i % 3 === 0 ? "PENDING" : i % 5 === 0 ? "CANCELLED" : "SUCCESS",
      })
    }
    return data
  }, [])

  const table = useReactTable({
    data: expandedData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onSortingChange: setSorting,
    getSortedRowModel: getSortedRowModel(),
    state: {
      sorting,
    },
    initialState: {
      pagination: {
        pageSize: 5,
      },
    },
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent Bookings</CardTitle>
        <CardDescription>
          Latest customer charging sessions and bookings across stations.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto rounded-md border">
          <Table>
            <TableHeader>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    return (
                      <TableHead key={header.id}>
                        {header.isPlaceholder
                          ? null
                          : flexRender(
                              header.column.columnDef.header,
                              header.getContext()
                            )}
                      </TableHead>
                    )
                  })}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              {table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row) => (
                  <TableRow
                    key={row.id}
                    data-state={row.getIsSelected() && "selected"}
                  >
                    {row.getVisibleCells().map((cell) => (
                      <TableCell key={cell.id}>
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext()
                        )}
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell
                    colSpan={columns.length}
                    className="h-24 text-center"
                  >
                    No results.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
        
        {/* Pagination controls */}
        <div className="flex items-center justify-between space-x-2 py-4">
          <div className="flex-1 text-sm text-muted-foreground">
            Showing {table.getRowModel().rows.length} of {expandedData.length} bookings.
          </div>
          <div className="flex items-center space-x-4">
            <div className="flex w-[100px] items-center justify-center text-sm font-medium">
              Page {table.getState().pagination.pageIndex + 1} / {table.getPageCount()}
            </div>
            <div className="space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => table.previousPage()}
                disabled={!table.getCanPreviousPage()}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => table.nextPage()}
                disabled={!table.getCanNextPage()}
              >
                Next
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
