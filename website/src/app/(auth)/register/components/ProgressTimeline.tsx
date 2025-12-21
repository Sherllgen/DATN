import { cn } from "@/lib/utils";

interface ProgressTimelineProps {
    currentStep: number;
    totalSteps: number;
}

const steps = [
    { num: 1, label: "Tải hồ sơ mẫu" },
    { num: 2, label: "Upload hồ sơ" },
    { num: 3, label: "Nhận kết quả" },
];

export function ProgressTimeline({
    currentStep,
    totalSteps,
}: ProgressTimelineProps) {
    return (
        <div className="py-6">
            <div className="relative flex justify-between items-center mx-auto max-w-3xl">
                {/* Connection Lines */}
                <div className="top-6 right-0 left-0 absolute bg-muted h-0.5">
                    <div
                        className="bg-green-500 h-full transition-all duration-500"
                        style={{
                            width: `${
                                ((currentStep - 1) / (totalSteps - 1)) * 100
                            }%`,
                        }}
                    />
                </div>

                {/* Step Circles */}
                {steps.map((step) => (
                    <div
                        key={step.num}
                        className="z-10 relative flex flex-col items-center"
                    >
                        {/* Circle */}
                        <div
                            className={cn(
                                "flex justify-center items-center bg-background border-4 rounded-full w-12 h-12 transition-all duration-300",
                                currentStep >= step.num
                                    ? "border-green-500 bg-green-500"
                                    : "border-muted bg-background"
                            )}
                        >
                            <span
                                className={cn(
                                    "font-bold text-sm transition-colors",
                                    currentStep >= step.num
                                        ? "text-white"
                                        : "text-muted-foreground"
                                )}
                            >
                                {step.num}
                            </span>
                        </div>

                        {/* Label */}
                        <div
                            className={cn(
                                "mt-3 max-w-30 text-xs text-center transition-colors",
                                currentStep >= step.num
                                    ? "text-green-500 font-medium"
                                    : "text-muted-foreground"
                            )}
                        >
                            {step.label}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
