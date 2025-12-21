import { Button } from "@/components/ui/button";
import { CheckCircle2, Download, FileText } from "lucide-react";

interface Step1DownloadProps {
    onDownload: () => void;
}

export function Step1Download({ onDownload }: Step1DownloadProps) {
    return (
        <div className="space-y-6 text-center">
            <div className="space-y-2">
                <h3 className="font-semibold text-xl">Download Template</h3>
                <p className="text-muted-foreground">
                    Download the template file and fill in your charging station
                    information
                </p>
            </div>
            <div className="space-y-4 mx-auto max-w-md">
                <ul className="space-y-2 text-muted-foreground text-sm text-left">
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Charging station owner information</span>
                    </li>
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Station address and location</span>
                    </li>
                    <li className="flex items-start gap-2">
                        <CheckCircle2 className="mt-0.5 w-4 h-4 text-primary shrink-0" />
                        <span>Equipment information and capacity</span>
                    </li>
                </ul>
                <Button onClick={onDownload} className="mt-4 w-full" size="lg">
                    <Download className="mr-2 w-4 h-4" />
                    Download Template
                </Button>
            </div>
        </div>
    );
}
