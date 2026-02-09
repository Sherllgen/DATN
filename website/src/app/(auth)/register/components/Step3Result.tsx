import { Button } from "@/components/ui/button";
import { CheckCircle2, AlertCircle } from "lucide-react";

interface Step3ResultProps {
    status: "success" | "error" | null;
    errorMessage?: string | null;
    onReset: () => void;
}

export function Step3Result({
    status,
    errorMessage,
    onReset,
}: Step3ResultProps) {
    return (
        <div className="space-y-6 text-center">
            {status === "success" ? (
                <>
                    <div className="flex justify-center items-center bg-green-500/10 mx-auto rounded-full w-20 h-20">
                        <CheckCircle2 className="w-10 h-10 text-green-500" />
                    </div>
                    <div className="space-y-2">
                        <h3 className="font-semibold text-green-500 text-xl">
                            Registration Successful!
                        </h3>
                        <p className="text-muted-foreground">
                            Your application has been submitted successfully. We
                            will review and respond within 24-48 hours.
                        </p>
                    </div>
                    <div className="bg-green-500/5 mx-auto p-4 border border-green-500/20 rounded-lg max-w-md">
                        <p className="text-muted-foreground text-sm">
                            Registration Code:{" "}
                            <span className="font-mono font-semibold">
                                {/* TODO: Get registration code from API */}
                                REG-{Date.now()}
                            </span>
                        </p>
                        <p className="mt-2 text-muted-foreground text-xs">
                            Please check your email to track the registration
                            status
                        </p>
                    </div>
                </>
            ) : (
                <>
                    <div className="flex justify-center items-center bg-red-500/10 mx-auto rounded-full w-20 h-20">
                        <AlertCircle className="w-10 h-10 text-red-500" />
                    </div>
                    <div className="space-y-2">
                        <h3 className="font-semibold text-red-500 text-xl">
                            Registration Failed
                        </h3>
                        <p className="text-muted-foreground">
                            An error occurred during processing. Please check
                            the file and try again.
                        </p>
                    </div>
                    <div className="bg-red-500/5 mx-auto p-4 border border-red-500/20 rounded-lg max-w-md">
                        <p className="text-muted-foreground text-sm">
                            {errorMessage || "Unknown error occurred."}
                        </p>
                    </div>
                </>
            )}
            <Button
                onClick={onReset}
                variant={status === "success" ? "default" : "outline"}
                size="lg"
                className="w-full max-w-md"
            >
                {status === "success" ? "Complete" : "Try Again"}
            </Button>
        </div>
    );
}
