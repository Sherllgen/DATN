import {
    approveRegistrationApi,
    rejectRegistrationApi,
    underReviewRegistrationApi,
} from "@/apis/admin/adminApi";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { EllipsisVertical, Eye } from "lucide-react";
import { useState } from "react";
import { toast } from "react-toastify";
import RejectDialog from "./reject.dialog";

interface ActionCellProps {
    row: any;
    onStatusChange?: () => void;
}

export default function ActionCell({ row, onStatusChange }: ActionCellProps) {
    const profile = row.original;
    const [loading, setLoading] = useState(false);
    const [openRejectDialog, setOpenRejectDialog] = useState(false);
    const [rejectReason, setRejectReason] = useState("");

    const handleReview = async () => {
        setLoading(true);
        try {
            await underReviewRegistrationApi(profile.profileId);
            if (onStatusChange) {
                onStatusChange();
            }
        } catch (e: any) {
            toast.error(
                e.response?.data?.message || "Failed to mark as reviewing"
            );
        } finally {
            setLoading(false);
        }
    };
    const handleApprove = async () => {
        setLoading(true);
        try {
            await approveRegistrationApi(profile.profileId);
            if (onStatusChange) {
                onStatusChange();
            }
        } catch (e: any) {
            toast.error(
                e.response?.data?.message || "Failed to mark as approved"
            );
        } finally {
            setLoading(false);
        }
    };
    const handleReject = async () => {
        setLoading(true);
        try {
            await rejectRegistrationApi(profile.profileId, rejectReason);
            setOpenRejectDialog(false);
            setRejectReason("");
            if (onStatusChange) {
                onStatusChange();
            }
        } catch (e: any) {
            toast.error(
                e.response?.data?.message || "Failed to mark as rejected"
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center gap-2">
            <Button
                variant="ghost"
                size="icon"
                className="w-8 h-8 cursor-pointer"
                disabled={loading}
            >
                <Eye className="size-4" />
                <span className="sr-only">View profile</span>
            </Button>

            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="w-8 h-8 cursor-pointer"
                        disabled={loading}
                    >
                        <EllipsisVertical className="size-4" />
                        <span className="sr-only">More actions</span>
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                    <DropdownMenuItem
                        className="cursor-pointer"
                        onClick={handleReview}
                        disabled={loading}
                    >
                        Mark as reviewing
                    </DropdownMenuItem>
                    <DropdownMenuItem
                        className="cursor-pointer"
                        onClick={handleApprove}
                        disabled={loading}
                    >
                        Approve
                    </DropdownMenuItem>
                    <DropdownMenuItem
                        className="cursor-pointer"
                        onClick={() => setOpenRejectDialog(true)}
                        disabled={loading}
                    >
                        Reject
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                </DropdownMenuContent>
            </DropdownMenu>

            {/* Dialog nhập lý do reject sử dụng shadcn-ui Dialog */}
            <RejectDialog
                openRejectDialog={openRejectDialog}
                setOpenRejectDialog={setOpenRejectDialog}
                rejectReason={rejectReason}
                setRejectReason={setRejectReason}
                handleReject={handleReject}
                loading={loading}
            />
        </div>
    );
}
