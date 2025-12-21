import { Button } from "@/components/ui/button";
import { CheckCircle2, Download, FileText } from "lucide-react";

interface Step1DownloadProps {
    onDownload: () => void;
}

export function Step1Download({ onDownload }: Step1DownloadProps) {
    return (
        <div className="space-y-6 text-center">
            <div className="space-y-2">
                <h3 className="font-semibold text-xl">Tải Hồ Sơ Mẫu</h3>
                <p className="text-muted-foreground">
                    Tải xuống file mẫu và điền đầy đủ thông tin trạm sạc của bạn
                </p>
            </div>
            <div className="space-y-4 mx-auto max-w-md">
                <ul className="space-y-2 text-muted-foreground text-sm text-left">
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Thông tin chủ trạm sạc</span>
                    </li>
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Địa chỉ và vị trí trạm sạc</span>
                    </li>
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Thông tin thiết bị và công suất</span>
                    </li>
                </ul>
                <Button onClick={onDownload} className="mt-4 w-full" size="lg">
                    <Download className="mr-2 w-4 h-4" />
                    Tải Hồ Sơ Mẫu
                </Button>
            </div>
        </div>
    );
}
