import { Button } from "@/components/ui/button";
import { CheckCircle2, Upload } from "lucide-react";

interface Step2UploadProps {
    uploadedFile: File | null;
    isProcessing: boolean;
    onFileUpload: (event: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: () => void;
    onBack: () => void;
}

export function Step2Upload({
    uploadedFile,
    isProcessing,
    onFileUpload,
    onSubmit,
    onBack,
}: Step2UploadProps) {
    return (
        <div className="space-y-6 text-center">
            <div className="space-y-2">
                <h3 className="font-semibold text-xl">Upload Document</h3>
                <p className="text-muted-foreground">
                    Upload the completed registration document
                </p>
            </div>
            <div className="space-y-4 mx-auto max-w-md">
                <div className="p-8 border-2 hover:border-primary/50 border-dashed rounded-lg transition-colors">
                    <input
                        type="file"
                        id="file-upload"
                        className="hidden"
                        accept=".xlsx,.xls,.pdf"
                        onChange={onFileUpload}
                    />
                    <label
                        htmlFor="file-upload"
                        className="flex flex-col items-center gap-2 cursor-pointer"
                    >
                        {uploadedFile ? (
                            <>
                                <CheckCircle2 className="w-12 h-12 text-primary" />
                                <p className="font-medium text-sm">
                                    {uploadedFile.name}
                                </p>
                                <p className="text-muted-foreground text-xs">
                                    {(uploadedFile.size / 1024).toFixed(2)} KB
                                </p>
                            </>
                        ) : (
                            <>
                                <Upload className="w-12 h-12 text-muted-foreground" />
                                <p className="font-medium text-sm">
                                    Click to select file
                                </p>
                                <p className="text-muted-foreground text-xs">
                                    Supported: PDF
                                </p>
                            </>
                        )}
                    </label>
                </div>
                <div className="flex gap-2">
                    <Button
                        variant="outline"
                        onClick={onBack}
                        className="flex-1"
                    >
                        Go Back
                    </Button>
                    <Button
                        onClick={onSubmit}
                        disabled={!uploadedFile || isProcessing}
                        className="flex-1"
                        size="lg"
                    >
                        {isProcessing ? (
                            <>
                                <div className="mr-2 border-2 border-current border-t-transparent rounded-full w-4 h-4 animate-spin" />
                                Processing...
                            </>
                        ) : (
                            "Submit Document"
                        )}
                    </Button>
                </div>
            </div>
        </div>
    );
}
