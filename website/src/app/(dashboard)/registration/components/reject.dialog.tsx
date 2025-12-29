import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

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
                    <DialogTitle>Enter Reject Reason</DialogTitle>
                </DialogHeader>
                <Textarea
                    placeholder="Enter reject reason..."
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    className="my-4"
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
                        Cancel
                    </Button>
                    <Button
                        variant="default"
                        onClick={handleReject}
                        disabled={loading || !rejectReason.trim()}
                    >
                        Reject
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
