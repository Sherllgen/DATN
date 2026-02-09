"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "react-toastify";
import {
    Search,
    Loader2,
    Lock,
    Unlock,
    Trash2,
    MoreHorizontal,
    Users,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Skeleton } from "@/components/ui/skeleton";
import {
    getListAccounts,
    lockAccountApi,
    unlockAccountApi,
    deleteAccountApi,
} from "@/apis/admin/adminApi";

type UserStatus = "PENDING" | "ACTIVE" | "BLOCKED" | "DELETED";

interface Account {
    id: number;
    email: string;
    fullName: string | null;
    phoneNumber: string | null;
    status: UserStatus;
    roles: string[];
    createdAt: string;
}

interface PageInfo {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
}

const statusVariantMap: Record<UserStatus, "default" | "secondary" | "destructive" | "outline"> = {
    ACTIVE: "default",
    PENDING: "outline",
    BLOCKED: "destructive",
    DELETED: "secondary",
};

const statusLabelMap: Record<UserStatus, string> = {
    ACTIVE: "Active",
    PENDING: "Pending",
    BLOCKED: "Blocked",
    DELETED: "Deleted",
};

export default function AccountManagementPage() {
    const [accounts, setAccounts] = useState<Account[]>([]);
    const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    // Filters
    const [page, setPage] = useState(0);
    const [statusFilter, setStatusFilter] = useState<string>("ALL");
    const [roleFilter, setRoleFilter] = useState<string>("ALL");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchInput, setSearchInput] = useState("");

    // Dialogs
    const [deleteDialog, setDeleteDialog] = useState(false);
    const [accountToDelete, setAccountToDelete] = useState<Account | null>(null);

    const fetchAccounts = useCallback(async () => {
        try {
            setLoading(true);
            const response = await getListAccounts({
                page,
                size: 10,
                status: statusFilter !== "ALL" ? statusFilter : undefined,
                role: roleFilter !== "ALL" ? roleFilter : undefined,
                search: searchQuery || undefined,
            });

            if (response?.data) {
                setAccounts(response.data.content || []);
                setPageInfo({
                    totalElements: response.data.totalElements,
                    totalPages: response.data.totalPages,
                    currentPage: response.data.page,
                    pageSize: response.data.size,
                });
            }
        } catch (error) {
            console.error("Failed to fetch accounts:", error);
            toast.error("Failed to load accounts");
        } finally {
            setLoading(false);
        }
    }, [page, statusFilter, roleFilter, searchQuery]);

    useEffect(() => {
        fetchAccounts();
    }, [fetchAccounts]);

    function handleSearch() {
        setSearchQuery(searchInput);
        setPage(0);
    }

    async function lockAccount(account: Account) {
        try {
            setActionLoading(true);
            await lockAccountApi(account.id);
            toast.success("Account locked successfully");
            fetchAccounts();
        } catch (error) {
            console.error("Failed to lock account:", error);
            toast.error("Failed to lock account");
        } finally {
            setActionLoading(false);
        }
    }

    async function unlockAccount(account: Account) {
        try {
            setActionLoading(true);
            await unlockAccountApi(account.id);
            toast.success("Account unlocked successfully");
            fetchAccounts();
        } catch (error) {
            console.error("Failed to unlock account:", error);
            toast.error("Failed to unlock account");
        } finally {
            setActionLoading(false);
        }
    }

    async function deleteAccount() {
        if (!accountToDelete) return;
        try {
            setActionLoading(true);
            await deleteAccountApi(accountToDelete.id);
            toast.success("Account deleted successfully");
            setDeleteDialog(false);
            setAccountToDelete(null);
            fetchAccounts();
        } catch (error) {
            console.error("Failed to delete account:", error);
            toast.error("Failed to delete account");
        } finally {
            setActionLoading(false);
        }
    }

    function formatDate(dateString: string) {
        return new Date(dateString).toLocaleDateString("en-US", {
            year: "numeric",
            month: "short",
            day: "numeric",
        });
    }

    function getRoleBadge(roles: string[]) {
        const role = roles[0] || "USER";
        const roleLabels: Record<string, string> = {
            SUPER_ADMIN: "Super Admin",
            STATION_OWNER: "Station Owner",
            STAFF: "Staff",
            USER: "User",
        };
        return roleLabels[role] || role;
    }

    if (loading && accounts.length === 0) {
        return (
            <div className="px-4 lg:px-6 space-y-6">
                <div>
                    <Skeleton className="h-8 w-64 mb-2" />
                    <Skeleton className="h-4 w-96" />
                </div>
                <Card>
                    <CardContent className="p-6 space-y-4">
                        {[1, 2, 3, 4, 5].map((i) => (
                            <Skeleton key={i} className="h-16 w-full" />
                        ))}
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="px-4 lg:px-6 space-y-6">
            <div>
                <h1 className="text-3xl font-bold flex items-center gap-2">
                    <Users className="h-8 w-8" />
                    Account Management
                </h1>
                <p className="text-muted-foreground">
                    Manage user accounts, lock/unlock access, and delete accounts
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Filters</CardTitle>
                    <CardDescription>
                        Search and filter user accounts
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="flex flex-col sm:flex-row gap-4">
                        <div className="flex-1 flex gap-2">
                            <Input
                                placeholder="Search by email or name..."
                                value={searchInput}
                                onChange={(e) => setSearchInput(e.target.value)}
                                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                            />
                            <Button onClick={handleSearch} className="cursor-pointer">
                                <Search className="h-4 w-4" />
                            </Button>
                        </div>
                        <Select value={statusFilter} onValueChange={(v) => { setStatusFilter(v); setPage(0); }}>
                            <SelectTrigger className="w-[150px]">
                                <SelectValue placeholder="Status" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">All Status</SelectItem>
                                <SelectItem value="ACTIVE">Active</SelectItem>
                                <SelectItem value="PENDING">Pending</SelectItem>
                                <SelectItem value="BLOCKED">Blocked</SelectItem>
                                <SelectItem value="DELETED">Deleted</SelectItem>
                            </SelectContent>
                        </Select>
                        <Select value={roleFilter} onValueChange={(v) => { setRoleFilter(v); setPage(0); }}>
                            <SelectTrigger className="w-[150px]">
                                <SelectValue placeholder="Role" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">All Roles</SelectItem>
                                <SelectItem value="SUPER_ADMIN">Super Admin</SelectItem>
                                <SelectItem value="STATION_OWNER">Station Owner</SelectItem>
                                <SelectItem value="USER">User</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>
                        Accounts {pageInfo && `(${pageInfo.totalElements})`}
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Email</TableHead>
                                <TableHead>Name</TableHead>
                                <TableHead>Role</TableHead>
                                <TableHead>Status</TableHead>
                                <TableHead>Created</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {accounts.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                                        No accounts found
                                    </TableCell>
                                </TableRow>
                            ) : (
                                accounts.map((account) => (
                                    <TableRow key={account.id}>
                                        <TableCell className="font-medium">{account.email}</TableCell>
                                        <TableCell>{account.fullName || "-"}</TableCell>
                                        <TableCell>{getRoleBadge(account.roles)}</TableCell>
                                        <TableCell>
                                            <Badge variant={statusVariantMap[account.status]}>
                                                {statusLabelMap[account.status]}
                                            </Badge>
                                        </TableCell>
                                        <TableCell>{formatDate(account.createdAt)}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="icon" className="cursor-pointer">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    {account.status === "ACTIVE" && (
                                                        <DropdownMenuItem
                                                            onClick={() => lockAccount(account)}
                                                            disabled={actionLoading}
                                                            className="cursor-pointer"
                                                        >
                                                            <Lock className="mr-2 h-4 w-4" />
                                                            Lock Account
                                                        </DropdownMenuItem>
                                                    )}
                                                    {account.status === "BLOCKED" && (
                                                        <DropdownMenuItem
                                                            onClick={() => unlockAccount(account)}
                                                            disabled={actionLoading}
                                                            className="cursor-pointer"
                                                        >
                                                            <Unlock className="mr-2 h-4 w-4" />
                                                            Unlock Account
                                                        </DropdownMenuItem>
                                                    )}
                                                    {account.status !== "DELETED" && (
                                                        <DropdownMenuItem
                                                            onClick={() => {
                                                                setAccountToDelete(account);
                                                                setDeleteDialog(true);
                                                            }}
                                                            disabled={actionLoading}
                                                            className="cursor-pointer text-destructive"
                                                        >
                                                            <Trash2 className="mr-2 h-4 w-4" />
                                                            Delete Account
                                                        </DropdownMenuItem>
                                                    )}
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>

                    {/* Pagination */}
                    {pageInfo && pageInfo.totalPages > 1 && (
                        <div className="flex items-center justify-between mt-4">
                            <p className="text-sm text-muted-foreground">
                                Page {pageInfo.currentPage + 1} of {pageInfo.totalPages}
                            </p>
                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                                    disabled={page === 0}
                                    className="cursor-pointer"
                                >
                                    Previous
                                </Button>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage((p) => p + 1)}
                                    disabled={page >= pageInfo.totalPages - 1}
                                    className="cursor-pointer"
                                >
                                    Next
                                </Button>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={deleteDialog} onOpenChange={setDeleteDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Account?</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete the account for{" "}
                            <strong>{accountToDelete?.email}</strong>? This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={deleteAccount}
                            disabled={actionLoading}
                            className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                        >
                            {actionLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
