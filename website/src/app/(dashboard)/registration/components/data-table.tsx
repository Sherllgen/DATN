"use client";

import { useMemo, useState } from "react";
import {
    type ColumnDef,
    type ColumnFiltersState,
    type SortingState,
    type VisibilityState,
    type Row,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable,
} from "@tanstack/react-table";
import {
    ChevronDown,
    EllipsisVertical,
    Eye,
    Download,
    Search,
} from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { OwnerType, Profile, ProfileStatus } from "@/types/profileRegistration";
import {
    approveRegistrationApi,
    rejectRegistrationApi,
    underReviewRegistrationApi,
} from "@/apis/admin/adminApi";
import { toast } from "react-toastify";
import { getOwnerTypeColor, getStatusColor } from "./utils";
import RejectDialog from "./reject.dialog";
import ActionCell from "./acction.cell";

interface DataTableProps {
    profiles: Profile[];
    onEditProfile: (profile: Profile) => void;
    onStatusChange: () => void;
}

export function DataTable({
    profiles,
    onEditProfile,
    onStatusChange,
}: DataTableProps) {
    const [sorting, setSorting] = useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(
        {}
    );
    const [rowSelection, setRowSelection] = useState({});
    const [globalFilter, setGlobalFilter] = useState("");

    const exactFilter = (
        row: Row<Profile>,
        columnId: string,
        value: string
    ) => {
        return row.getValue(columnId) === value;
    };

    const columns = useMemo<ColumnDef<Profile>[]>(
        () => [
            {
                id: "select",
                header: ({ table }) => (
                    <div className="flex justify-center items-center px-2">
                        <Checkbox
                            checked={
                                table.getIsAllPageRowsSelected() ||
                                (table.getIsSomePageRowsSelected() &&
                                    "indeterminate")
                            }
                            onCheckedChange={(value) =>
                                table.toggleAllPageRowsSelected(!!value)
                            }
                            aria-label="Select all"
                        />
                    </div>
                ),
                cell: ({ row }) => (
                    <div className="flex justify-center items-center px-2">
                        <Checkbox
                            checked={row.getIsSelected()}
                            onCheckedChange={(value) =>
                                row.toggleSelected(!!value)
                            }
                            aria-label="Select row"
                        />
                    </div>
                ),
                enableSorting: false,
                enableHiding: false,
                size: 50,
            },
            {
                accessorKey: "email",
                header: "Email",
                cell: ({ row }) => {
                    const profile = row.original;
                    return (
                        <div className="flex items-center gap-3">
                            <div className="flex flex-col">
                                <span className="font-medium">
                                    {profile.email}
                                </span>
                                <span className="text-muted-foreground text-sm">
                                    {profile.registrationCode}
                                </span>
                            </div>
                        </div>
                    );
                },
            },
            {
                accessorKey: "ownerType",
                header: "Owner Type",
                cell: ({ row }) => {
                    const ownerType = row.getValue("ownerType") as OwnerType;
                    return (
                        <Badge
                            variant="secondary"
                            className={getOwnerTypeColor(ownerType)}
                        >
                            {ownerType}
                        </Badge>
                    );
                },
            },
            {
                accessorKey: "pdfFileUrl",
                header: "PDF File URL",
                cell: ({ row }) => {
                    const pdfFileUrl = row.getValue("pdfFileUrl") as string;
                    // Rút ngắn URL nếu quá dài
                    const displayUrl =
                        pdfFileUrl && pdfFileUrl.length > 30
                            ? pdfFileUrl.slice(0, 10) +
                              "..." +
                              pdfFileUrl.slice(-10)
                            : pdfFileUrl;
                    return pdfFileUrl ? (
                        <a
                            href={pdfFileUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="font-medium text-blue-600 hover:underline"
                            title={pdfFileUrl}
                        >
                            {displayUrl}
                        </a>
                    ) : null;
                },
                filterFn: exactFilter,
            },
            {
                accessorKey: "submittedAt",
                header: "Submitted At",
                cell: ({ row }) => {
                    const submittedAt = row.getValue("submittedAt") as string;
                    let formatted = "";
                    if (submittedAt) {
                        const date = new Date(submittedAt);
                        if (!isNaN(date.getTime())) {
                            const pad = (n: number) =>
                                n.toString().padStart(2, "0");
                            formatted = `${pad(date.getDate())}/${pad(
                                date.getMonth() + 1
                            )}/${date.getFullYear()} ${pad(
                                date.getHours()
                            )}:${pad(date.getMinutes())}`;
                        } else {
                            formatted = submittedAt;
                        }
                    }
                    return <span className="text-sm">{formatted}</span>;
                },
            },
            {
                accessorKey: "status",
                header: "Status",
                cell: ({ row }) => {
                    const status = row.getValue("status") as ProfileStatus;
                    return (
                        <Badge
                            variant="secondary"
                            className={getStatusColor(status)}
                        >
                            {status}
                        </Badge>
                    );
                },
                filterFn: exactFilter,
            },
            {
                id: "actions",
                header: "Actions",
                cell: ({ row }) => {
                    return (
                        <ActionCell row={row} onStatusChange={onStatusChange} />
                    );
                },
            },
        ],
        [onEditProfile]
    );

    const table = useReactTable({
        data: profiles ?? [],
        columns,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        onGlobalFilterChange: setGlobalFilter,
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection,
            globalFilter,
        },
    });

    // const roleFilter = table.getColumn("role")?.getFilterValue() as string;
    const statusFilter = table.getColumn("status")?.getFilterValue() as string;

    return (
        <div className="space-y-4 w-full">
            <div className="flex sm:flex-row flex-col sm:justify-between sm:items-center gap-4">
                <div className="flex flex-1 items-center space-x-2">
                    <div className="relative flex-1 max-w-sm">
                        <Search className="top-1/2 left-3 absolute size-4 text-muted-foreground -translate-y-1/2" />
                        <Input
                            placeholder="Search users..."
                            value={globalFilter ?? ""}
                            onChange={(event) =>
                                setGlobalFilter(String(event.target.value))
                            }
                            className="shadow-sm pl-9"
                        />
                    </div>
                </div>
                <div className="flex items-center space-x-2">
                    <Button
                        variant="outline"
                        className="shadow-sm border cursor-pointer"
                    >
                        <Download className="mr-2 size-4" />
                        Export
                    </Button>
                </div>
            </div>

            <div className="gap-2 sm:gap-4 grid sm:grid-cols-4 mt-6">
                <div className="space-y-2">
                    <Label
                        htmlFor="status-filter"
                        className="font-medium text-sm"
                    >
                        Status
                    </Label>
                    <Select
                        value={statusFilter || ""}
                        onValueChange={(value) =>
                            table
                                .getColumn("status")
                                ?.setFilterValue(value === "all" ? "" : value)
                        }
                    >
                        <SelectTrigger
                            className="w-full cursor-pointer"
                            id="status-filter"
                        >
                            <SelectValue placeholder="Select Status" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">All Status</SelectItem>
                            <SelectItem value="Active">Active</SelectItem>
                            <SelectItem value="Pending">Pending</SelectItem>
                            <SelectItem value="Error">Error</SelectItem>
                            <SelectItem value="Inactive">Inactive</SelectItem>
                        </SelectContent>
                    </Select>
                </div>
                <div className="space-y-2">
                    <Label
                        htmlFor="column-visibility"
                        className="font-medium text-sm"
                    >
                        Column Visibility
                    </Label>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild id="column-visibility">
                            <Button
                                variant="outline"
                                className="w-full cursor-pointer"
                            >
                                Columns <ChevronDown className="ml-2 size-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                            {table
                                .getAllColumns()
                                .filter((column) => column.getCanHide())
                                .map((column) => {
                                    return (
                                        <DropdownMenuCheckboxItem
                                            key={column.id}
                                            className="capitalize"
                                            checked={column.getIsVisible()}
                                            onCheckedChange={(value) =>
                                                column.toggleVisibility(!!value)
                                            }
                                        >
                                            {column.id}
                                        </DropdownMenuCheckboxItem>
                                    );
                                })}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>

            <div className="border rounded-md">
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
                                                      header.column.columnDef
                                                          .header,
                                                      header.getContext()
                                                  )}
                                        </TableHead>
                                    );
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={
                                        row.getIsSelected() && "selected"
                                    }
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

            <div className="flex justify-between items-center space-x-2 py-4">
                <div className="flex items-center space-x-2">
                    <Label htmlFor="page-size" className="font-medium text-sm">
                        Show
                    </Label>
                    <Select
                        value={`${table.getState().pagination.pageSize}`}
                        onValueChange={(value) => {
                            table.setPageSize(Number(value));
                        }}
                    >
                        <SelectTrigger
                            className="w-20 cursor-pointer"
                            id="page-size"
                        >
                            <SelectValue
                                placeholder={
                                    table.getState().pagination.pageSize
                                }
                            />
                        </SelectTrigger>
                        <SelectContent side="top">
                            {[10, 20, 30, 40, 50].map((pageSize) => (
                                <SelectItem
                                    key={pageSize}
                                    value={`${pageSize}`}
                                >
                                    {pageSize}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
                <div className="hidden sm:block flex-1 text-muted-foreground text-sm">
                    {table.getFilteredSelectedRowModel().rows.length} of{" "}
                    {table.getFilteredRowModel().rows.length} row(s) selected.
                </div>
                <div className="flex items-center space-x-6 lg:space-x-8">
                    <div className="hidden sm:flex items-center space-x-2">
                        <p className="font-medium text-sm">Page</p>
                        <strong className="text-sm">
                            {table.getState().pagination.pageIndex + 1} of{" "}
                            {table.getPageCount()}
                        </strong>
                    </div>
                    <div className="flex items-center space-x-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => table.previousPage()}
                            disabled={!table.getCanPreviousPage()}
                            className="cursor-pointer"
                        >
                            Previous
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => table.nextPage()}
                            disabled={!table.getCanNextPage()}
                            className="cursor-pointer"
                        >
                            Next
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}
