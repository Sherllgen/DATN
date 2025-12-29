"use client";

import { useState } from "react";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { InstructionsPanel } from "./components/InstructionsPanel";
import { ProgressTimeline } from "./components/ProgressTimeline";
import { Step1Download } from "./components/Step1Download";
import { Step2Upload } from "./components/Step2Upload";
import { Step3Result } from "./components/Step3Result";
import { LandingNavbar } from "@/app/landing/components/navbar";
import { submitRegistrationApi } from "@/apis/authApi/authApi";
import CheckStatus from "./components/checkStatus";
import { Button } from "@/components/ui/button";

export default function RegisterPage() {
    const [currentStep, setCurrentStep] = useState(1);
    const [uploadedFile, setUploadedFile] = useState<File | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [registrationStatus, setRegistrationStatus] = useState<
        "success" | "error" | null
    >(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [mainScreen, setMainScreen] = useState<"form" | "status">("form");

    const totalSteps = 3;

    const handleDownloadTemplate = () => {
        // Download PDF template
        const link = document.createElement("a");
        link.href = "/templates/registration-form.pdf";
        link.download = "mau-ho-so-dang-ky-tram-sac.pdf";
        link.click();

        setTimeout(() => {
            setCurrentStep(2);
        }, 500);
    };

    const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            setUploadedFile(file);
        }
    };

    const handleSubmit = async () => {
        if (!uploadedFile) return;

        setIsProcessing(true);
        try {
            await submitRegistrationApi(uploadedFile);
            setRegistrationStatus("success");
            setCurrentStep(3);
        } catch (error: Error | any) {
            setRegistrationStatus("error");
            setErrorMessage(error.response.data.message);
            setCurrentStep(3);
        } finally {
            setIsProcessing(false);
        }
    };

    const handleReset = () => {
        setCurrentStep(1);
        setUploadedFile(null);
        setRegistrationStatus(null);
    };

    return (
        <div className="bg-gray-50 min-h-screen">
            {/* Navigation */}
            <LandingNavbar />

            {/* Main Content */}
            <div className="flex justify-center items-center p-4 min-h-[calc(100vh-73px)]">
                <div className="gap-6 grid lg:grid-cols-[1fr_900px] w-full max-w-7xl">
                    {/* Instructions Panel */}
                    <InstructionsPanel />

                    {/* Main Form Card */}
                    {mainScreen === "form" && (
                        <Card className="shadow w-full">
                            <CardHeader>
                                <CardTitle className="text-2xl">
                                    Charging Station Registration
                                </CardTitle>
                                <CardDescription>
                                    Complete 3 steps to register as a charging
                                    station owner
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {/* Progress Timeline */}
                                <ProgressTimeline
                                    currentStep={currentStep}
                                    totalSteps={totalSteps}
                                />

                                {/* Step Content */}
                                <div className="flex flex-col justify-center min-h-75">
                                    {currentStep === 1 && (
                                        <Step1Download
                                            onDownload={handleDownloadTemplate}
                                        />
                                    )}

                                    {currentStep === 2 && (
                                        <Step2Upload
                                            uploadedFile={uploadedFile}
                                            isProcessing={isProcessing}
                                            onFileUpload={handleFileUpload}
                                            onSubmit={handleSubmit}
                                            onBack={() => setCurrentStep(1)}
                                        />
                                    )}

                                    {currentStep === 3 && (
                                        <Step3Result
                                            status={registrationStatus}
                                            errorMessage={errorMessage}
                                            onReset={handleReset}
                                        />
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {mainScreen === "status" && <CheckStatus />}
                </div>
            </div>
        </div>
    );
}
