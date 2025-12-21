import { FaCircleExclamation } from "react-icons/fa6";

export default function HowItWorks() {
    return (
        <div className="bg-white shadow pb-2 border border-gray-200 rounded-xl">
            <div className="p-6">
                <div className="flex items-center gap-2 mb-6">
                    <div className="flex justify-center items-center rounded-full w-5 h-5">
                        <FaCircleExclamation color="blue" />
                    </div>
                    <h2 className="font-semibold text-lg">How it works</h2>
                </div>

                <div className="space-y-6">
                    {/* Step 1 */}
                    <div className="flex gap-4">
                        <div className="flex justify-center items-center bg-blue-50 rounded-full w-8 h-8 font-semibold text-blue-600 shrink-0">
                            1
                        </div>
                        <div>
                            <h3 className="mb-1 font-semibold text-gray-900">
                                Download the Template
                            </h3>
                            <p className="text-gray-600 text-sm">
                                Get the official .xlsx spreadsheet. This file
                                contains all the necessary headers and data
                                validation for our system.
                            </p>
                        </div>
                    </div>

                    {/* Step 2 */}
                    <div className="flex gap-4">
                        <div className="flex flex-shrink-0 justify-center items-center bg-blue-50 rounded-full w-8 h-8 font-semibold text-blue-600">
                            2
                        </div>
                        <div>
                            <h3 className="mb-1 font-semibold text-gray-900">
                                Fill in Equipment Data
                            </h3>
                            <p className="text-gray-600 text-sm">
                                Enter serial numbers, models, and locations.
                                Please do not modify the header rows to ensure
                                smooth processing.
                            </p>
                        </div>
                    </div>

                    {/* Step 3 */}
                    <div className="flex gap-4">
                        <div className="flex flex-shrink-0 justify-center items-center bg-blue-50 rounded-full w-8 h-8 font-semibold text-blue-600">
                            3
                        </div>
                        <div>
                            <h3 className="mb-1 font-semibold text-gray-900">
                                Save and Upload
                            </h3>
                            <p className="text-gray-600 text-sm">
                                Save the file locally. In the next step, you
                                will upload this file to the dashboard.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
