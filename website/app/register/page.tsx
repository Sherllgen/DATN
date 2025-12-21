"use client";

import { Button } from "@/components/ui/button";
import Link from "next/link";
import ProgressSteps from "./ProgressSteps";
import HowItWorks from "./HowItWorks";
import DownloadTemplate from "./DownloadTemplate";

export default function RegisterPage() {
    return (
        <div className="bg-gray-50 min-h-screen">
            <ProgressSteps />

            {/* Button next step */}
            <div className="flex justify-end mt-6 pr-62">
                <Button className="bg-blue-700 hover:bg-blue-800">
                    <Link href="#">Next Step</Link>
                </Button>
            </div>

            {/* Main Content */}
            <div className="mx-auto px-6 py-8 max-w-7xl">
                <div className="flex gap-8">
                    <HowItWorks />
                    <DownloadTemplate />
                </div>
            </div>
        </div>
    );
}
