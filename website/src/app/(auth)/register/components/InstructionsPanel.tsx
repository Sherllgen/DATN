import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BookOpen, Info } from "lucide-react";

export function InstructionsPanel() {
    return (
        <Card className="hidden lg:block top-4 sticky shadow h-fit">
            <CardHeader>
                <div className="flex items-center gap-2">
                    <BookOpen className="w-5 h-5 text-primary" />
                    <CardTitle className="text-lg">
                        Registration Guide
                    </CardTitle>
                </div>
            </CardHeader>
            <CardContent className="space-y-6 mt-2">
                {/* Step 1 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                1
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">
                            Download Template
                        </h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>
                                Download the Pdf template from the system
                            </span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>
                                Fill in all information for each section
                            </span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Step 2 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                2
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">
                            Upload Document
                        </h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Select the completed file</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Maximum file size 10MB</span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Step 3 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                3
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">
                            Receive Result
                        </h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>
                                System processes automatically in 2-5 minutes
                            </span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Email notification within 24-48 hours</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Save the registration code for tracking</span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Important Notes */}
                <div className="bg-blue-500/5 p-3 border border-blue-500/20 rounded-lg">
                    <div className="flex items-start gap-2">
                        <Info className="mt-0.5 w-4 h-4 text-blue-500 shrink-0" />
                        <div className="space-y-1">
                            <p className="font-semibold text-blue-500 text-xs">
                                Important Notes
                            </p>
                            <ul className="space-y-1 text-muted-foreground text-xs">
                                <li>
                                    • Information must be accurate and complete
                                </li>
                                <li>
                                    • Ensure the file is not corrupted or
                                    damaged
                                </li>
                                <li>• Contact hotline if you need support</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
