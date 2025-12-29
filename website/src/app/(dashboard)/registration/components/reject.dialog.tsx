import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";

interface RejectDialogProps {
    openRejectDialog: boolean;
    setOpenRejectDialog: (open: boolean) => void;
    rejectReason: string;
    setRejectReason: (reason: string) => void;
    handleReject: () => void;
    loading: boolean;
}

export default function RejectDialog({
    openRejectDialog,
    setOpenRejectDialog,
    rejectReason,
    setRejectReason,
    handleReject,
    loading,
}: RejectDialogProps) {
    return (
        <Dialog open={openRejectDialog} onOpenChange={setOpenRejectDialog}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle>Nhập lý do từ chối</DialogTitle>
                </DialogHeader>
                <Input
                    placeholder="Nhập lý do từ chối..."
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    className="mb-4"
                    autoFocus
                />
                <DialogFooter className="flex justify-end gap-2">
                    <Button
                        variant="outline"
                        onClick={() => {
                            setOpenRejectDialog(false);
                            setRejectReason("");
                        }}
                        disabled={loading}
                    >
                        Hủy
                    </Button>
                    <Button
                        variant="destructive"
                        onClick={handleReject}
                        disabled={loading || !rejectReason.trim()}
                    >
                        Xác nhận từ chối
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
