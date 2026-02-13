"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "react-toastify";
import {
    Loader2,
    CheckCircle,
    XCircle,
    Eye,
    FileText,
    ClipboardList,
    ExternalLink,
    MoreHorizontal,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
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
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    getListRegistration,
    getRegistrationDetail,
    underReviewRegistrationApi,
    approveRegistrationApi,
    rejectRegistrationApi,
} from "@/apis/admin/adminApi";

type RegistrationStatus = "SUBMITTED" | "UNDER_REVIEW" | "APPROVED" | "REJECTED";
type OwnerType = "INDIVIDUAL" | "COMPANY";

interface Registration {
    profileId: number;
    registrationCode: string;
    name: string;
    email: string;
    phone: string;
    ownerType: OwnerType;
    status: RegistrationStatus;
    submittedAt: string;
    pdfFileUrl?: string;
}

interface RegistrationDetailData extends Registration {
    // All fields now come from Registration interface
    // Extra fields are optional in case we want to add detail API call later
    rejectionReason?: string | null;
    reviewedAt?: string | null;
}

interface PageInfo {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
}

const statusVariantMap: Record<RegistrationStatus, "default" | "secondary" | "destructive" | "outline"> = {
    SUBMITTED: "outline",
    UNDER_REVIEW: "secondary",
    APPROVED: "default",
    REJECTED: "destructive",
};

const statusLabelMap: Record<RegistrationStatus, string> = {
    SUBMITTED: "Submitted",
    UNDER_REVIEW: "Under Review",
    APPROVED: "Approved",
    REJECTED: "Rejected",
};

const typeLabels: Record<OwnerType, string> = {
    INDIVIDUAL: "Individual",
    COMPANY: "Company",
};

export default function RegistrationReviewPage() {
    const [registrations, setRegistrations] = useState<Registration[]>([]);
    const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    // Filters
    const [page, setPage] = useState(0);
    const [statusFilter, setStatusFilter] = useState<string>("SUBMITTED");

    // Detail Dialog
    const [detailDialog, setDetailDialog] = useState(false);
    const [selectedDetail, setSelectedDetail] = useState<RegistrationDetailData | null>(null);
    const [loadingDetail, setLoadingDetail] = useState(false);

    // Reject Dialog
    const [rejectDialog, setRejectDialog] = useState(false);
    const [registrationToReject, setRegistrationToReject] = useState<Registration | null>(null);
    const [rejectionReason, setRejectionReason] = useState("");

    // Approve Dialog
    const [approveDialog, setApproveDialog] = useState(false);
    const [registrationToApprove, setRegistrationToApprove] = useState<Registration | null>(null);

    const fetchRegistrations = useCallback(async () => {
        try {
            setLoading(true);
            const response = await getListRegistration({
                page,
                size: 10,
                status: statusFilter !== "ALL" ? statusFilter : undefined,
            });

            if (response?.data) {
                setRegistrations(response.data.content || []);
                setPageInfo({
                    totalElements: response.data.totalElements,
                    totalPages: response.data.totalPages,
                    currentPage: response.data.page,
                    pageSize: response.data.size,
                });
            }
        } catch (error) {
            console.error("Failed to fetch registrations:", error);
            toast.error("Failed to load registrations");
        } finally {
            setLoading(false);
        }
    }, [page, statusFilter]);

    useEffect(() => {
        fetchRegistrations();
    }, [fetchRegistrations]);

    async function viewDetails(registration: Registration) {
        try {
            // Mark as under review if submitted
            if (registration.status === "SUBMITTED") {
                setLoadingDetail(true);
                await underReviewRegistrationApi(registration.profileId);
                fetchRegistrations(); // Refresh list to update status
            }

            // Use list data directly, no API call needed
            setSelectedDetail(registration as unknown as RegistrationDetailData);
            setDetailDialog(true);
        } catch (error) {
            console.error("Failed to update status:", error);
            toast.error("Failed to update review status");
        } finally {
            setLoadingDetail(false);
        }
    }

    async function approveRegistration() {
        if (!registrationToApprove) return;
        try {
            setActionLoading(true);
            await approveRegistrationApi(registrationToApprove.profileId);
            toast.success("Registration approved successfully");
            setApproveDialog(false);
            setRegistrationToApprove(null);
            setDetailDialog(false);
            fetchRegistrations();
        } catch (error) {
            console.error("Failed to approve registration:", error);
            toast.error("Failed to approve registration");
        } finally {
            setActionLoading(false);
        }
    }

    async function rejectRegistration() {
        if (!registrationToReject || !rejectionReason.trim()) {
            toast.error("Please provide a reason for rejection");
            return;
        }
        try {
            setActionLoading(true);
            await rejectRegistrationApi(registrationToReject.profileId, rejectionReason);
            toast.success("Registration rejected");
            setRejectDialog(false);
            setRegistrationToReject(null);
            setRejectionReason("");
            setDetailDialog(false);
            fetchRegistrations();
        } catch (error) {
            console.error("Failed to reject registration:", error);
            toast.error("Failed to reject registration");
        } finally {
            setActionLoading(false);
        }
    }

    function formatDate(dateString: string) {
        return new Date(dateString).toLocaleDateString("en-US", {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    if (loading && registrations.length === 0) {
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
                    <ClipboardList className="h-8 w-8" />
                    Station Owner Registration
                </h1>
                <p className="text-muted-foreground">
                    Review and approve station owner registration applications
                </p>
            </div>

            <Tabs value={statusFilter} onValueChange={(v) => { setStatusFilter(v); setPage(0); }}>
                <TabsList>
                    <TabsTrigger value="SUBMITTED" className="cursor-pointer">Submitted</TabsTrigger>
                    <TabsTrigger value="UNDER_REVIEW" className="cursor-pointer">Under Review</TabsTrigger>
                    <TabsTrigger value="APPROVED" className="cursor-pointer">Approved</TabsTrigger>
                    <TabsTrigger value="REJECTED" className="cursor-pointer">Rejected</TabsTrigger>
                    <TabsTrigger value="ALL" className="cursor-pointer">All</TabsTrigger>
                </TabsList>
            </Tabs>

            <Card>
                <CardHeader>
                    <CardTitle>
                        Registrations {pageInfo && `(${pageInfo.totalElements})`}
                    </CardTitle>
                    <CardDescription>
                        Click on a row to view details and take action
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead>Registration Code</TableHead>
                                <TableHead>Type</TableHead>
                                <TableHead>PDF</TableHead>
                                <TableHead>Status</TableHead>
                                <TableHead>Submitted</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {registrations.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                                        No registrations found
                                    </TableCell>
                                </TableRow>
                            ) : (
                                registrations.map((reg) => (
                                    <TableRow key={reg.profileId}>
                                        <TableCell className="font-medium">{reg.name}</TableCell>
                                        <TableCell>
                                            <div>{reg.registrationCode}</div>
                                            <div className="text-xs text-muted-foreground">{reg.email}</div>
                                        </TableCell>
                                        <TableCell>{typeLabels[reg.ownerType]}</TableCell>
                                        <TableCell>
                                            {reg.pdfFileUrl ? (
                                                <a
                                                    href={reg.pdfFileUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="inline-flex items-center gap-1 text-primary hover:underline"
                                                    onClick={(e) => e.stopPropagation()}
                                                >
                                                    <FileText className="h-4 w-4" />
                                                    View
                                                </a>
                                            ) : (
                                                <span className="text-muted-foreground">-</span>
                                            )}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={statusVariantMap[reg.status]}>
                                                {statusLabelMap[reg.status]}
                                            </Badge>
                                        </TableCell>
                                        <TableCell>{formatDate(reg.submittedAt)}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="icon" className="cursor-pointer">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem
                                                        onClick={() => viewDetails(reg)}
                                                        className="cursor-pointer"
                                                    >
                                                        <Eye className="mr-2 h-4 w-4" />
                                                        View Details
                                                    </DropdownMenuItem>
                                                    {(reg.status === "SUBMITTED" || reg.status === "UNDER_REVIEW") && (
                                                        <>
                                                            <DropdownMenuItem
                                                                onClick={() => {
                                                                    setRegistrationToApprove(reg);
                                                                    setApproveDialog(true);
                                                                }}
                                                                className="cursor-pointer"
                                                            >
                                                                <CheckCircle className="mr-2 h-4 w-4" />
                                                                Approve
                                                            </DropdownMenuItem>
                                                            <DropdownMenuItem
                                                                onClick={() => {
                                                                    setRegistrationToReject(reg);
                                                                    setRejectDialog(true);
                                                                }}
                                                                className="cursor-pointer text-destructive"
                                                            >
                                                                <XCircle className="mr-2 h-4 w-4" />
                                                                Reject
                                                            </DropdownMenuItem>
                                                        </>
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

            {/* Detail Dialog */}
            <Dialog open={detailDialog} onOpenChange={setDetailDialog}>
                <DialogContent className="max-w-2xl">
                    <DialogHeader>
                        <DialogTitle>Registration Details</DialogTitle>
                        <DialogDescription>
                            Review the registration information below
                        </DialogDescription>
                    </DialogHeader>
                    {loadingDetail ? (
                        <div className="space-y-4 py-4">
                            {[1, 2, 3, 4].map((i) => (
                                <Skeleton key={i} className="h-12 w-full" />
                            ))}
                        </div>
                    ) : selectedDetail && (
                        <div className="space-y-4 py-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label className="text-muted-foreground">Name</Label>
                                    <p className="font-medium">{selectedDetail.name}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Type</Label>
                                    <p className="font-medium">{typeLabels[selectedDetail.ownerType]}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Registration Code</Label>
                                    <p className="font-medium font-mono">{selectedDetail.registrationCode}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Status</Label>
                                    <p>
                                        <Badge variant={statusVariantMap[selectedDetail.status]}>
                                            {statusLabelMap[selectedDetail.status]}
                                        </Badge>
                                    </p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Email</Label>
                                    <p className="font-medium">{selectedDetail.email}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Phone</Label>
                                    <p className="font-medium">{selectedDetail.phone}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Submitted At</Label>
                                    <p className="font-medium">{formatDate(selectedDetail.submittedAt)}</p>
                                </div>
                                {selectedDetail.pdfFileUrl && (
                                    <div>
                                        <Label className="text-muted-foreground">Registration PDF</Label>
                                        <a
                                            href={selectedDetail.pdfFileUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="flex items-center gap-2 text-primary hover:underline"
                                        >
                                            <FileText className="h-4 w-4" />
                                            View Document
                                            <ExternalLink className="h-3 w-3" />
                                        </a>
                                    </div>
                                )}
                                {selectedDetail.rejectionReason && (
                                    <div className="col-span-2">
                                        <Label className="text-muted-foreground">Rejection Reason</Label>
                                        <p className="font-medium text-destructive">{selectedDetail.rejectionReason}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setDetailDialog(false)} className="cursor-pointer">
                            Close
                        </Button>
                        {selectedDetail && (selectedDetail.status === "SUBMITTED" || selectedDetail.status === "UNDER_REVIEW") && (
                            <>
                                <Button
                                    variant="destructive"
                                    onClick={() => {
                                        setRegistrationToReject(selectedDetail);
                                        setRejectDialog(true);
                                    }}
                                    className="cursor-pointer"
                                >
                                    <XCircle className="h-4 w-4 mr-1" />
                                    Reject
                                </Button>
                                <Button
                                    onClick={() => {
                                        setRegistrationToApprove(selectedDetail);
                                        setApproveDialog(true);
                                    }}
                                    className="cursor-pointer"
                                >
                                    <CheckCircle className="h-4 w-4 mr-1" />
                                    Approve
                                </Button>
                            </>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Approve Confirmation Dialog */}
            <AlertDialog open={approveDialog} onOpenChange={setApproveDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Approve Registration?</AlertDialogTitle>
                        <AlertDialogDescription>
                            This will grant <strong>{registrationToApprove?.name}</strong> the
                            Station Owner role and send them a confirmation email.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={approveRegistration}
                            disabled={actionLoading}
                            className="cursor-pointer"
                        >
                            {actionLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Approve
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Reject Dialog */}
            <Dialog open={rejectDialog} onOpenChange={setRejectDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Reject Registration</DialogTitle>
                        <DialogDescription>
                            Provide a reason for rejecting this registration. The applicant will be notified.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="reason">Reason *</Label>
                            <Textarea
                                id="reason"
                                value={rejectionReason}
                                onChange={(e) => setRejectionReason(e.target.value)}
                                placeholder="Enter the reason for rejection..."
                                rows={4}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setRejectDialog(false)} className="cursor-pointer">
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={rejectRegistration}
                            disabled={actionLoading || !rejectionReason.trim()}
                            className="cursor-pointer"
                        >
                            {actionLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Reject
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
