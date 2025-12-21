import { Button } from "@/components/ui/button";
import { Download } from "lucide-react";

export default function DownloadTemplate() {
    return (
        <div className="bg-white shadow pb-2 border border-gray-200 rounded-xl">
            <div className="flex flex-col items-center p-6 text-center">
                <h3 className="mb-2 font-semibold text-gray-900 text-xl">
                    Get the template
                </h3>

                <p className="mb-6 text-gray-600 text-sm">
                    Standardized .xlsx format (v2.4)
                </p>

                <Button
                    className="bg-blue-600 hover:bg-blue-700 w-full text-white"
                    size="lg"
                >
                    <Download className="mr-2 w-4 h-4" />
                    Download Template (.xlsx)
                </Button>

                <p className="mt-4 text-gray-500 text-xs">
                    File size: 24KB • Last updated: Today
                </p>
            </div>
        </div>
    );
}
