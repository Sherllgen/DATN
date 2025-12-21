export default function ProgressSteps() {
    return (
        <div className="bg-white shadow-sm border-b">
            <div className="mx-auto px-6 py-8 max-w-7xl">
                {/* Step indicators */}
                <div className="mb-8">
                    <div className="relative flex justify-between items-center px-10">
                        {/* Progress line background */}
                        <div className="top-5 right-0 left-0 z-0 absolute bg-gray-200 h-0.5"></div>

                        {/* Active progress line */}
                        <div
                            className="top-5 left-0 z-0 absolute bg-blue-600 h-0.5 transition-all duration-500"
                            style={{ width: "16.66%" }}
                        ></div>

                        {/* Step 1 - Active */}
                        <div className="z-10 relative flex flex-col items-center gap-3">
                            <div className="flex justify-center items-center bg-blue-600 shadow-lg rounded-full ring-4 ring-blue-100 w-10 h-10 font-semibold text-white transition-all">
                                1
                            </div>
                            <div className="text-center">
                                <div className="font-semibold text-blue-600 text-sm">
                                    Download
                                </div>
                            </div>
                        </div>

                        {/* Step 2 - Inactive */}
                        <div className="z-10 relative flex flex-col items-center gap-3">
                            <div className="flex justify-center items-center bg-white shadow border-2 border-gray-300 rounded-full w-10 h-10 font-semibold text-gray-400 transition-all">
                                2
                            </div>
                            <div className="text-center">
                                <div className="font-medium text-gray-400 text-sm">
                                    Upload Data
                                </div>
                            </div>
                        </div>

                        {/* Step 3 - Inactive */}
                        <div className="z-10 relative flex flex-col items-center gap-3">
                            <div className="flex justify-center items-center bg-white shadow border-2 border-gray-300 rounded-full w-10 h-10 font-semibold text-gray-400 transition-all">
                                3
                            </div>
                            <div className="text-center">
                                <div className="font-medium text-gray-400 text-sm">
                                    Review
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Current step info */}
                <div className="flex justify-between items-center bg-blue-50 px-4 py-3 border border-blue-100 rounded-lg">
                    <div className="flex items-center gap-3">
                        <div className="flex justify-center items-center bg-blue-600 rounded-full w-8 h-8 font-bold text-white text-sm">
                            1
                        </div>
                        <div>
                            <div className="font-semibold text-gray-900">
                                Step 1: Download Template
                            </div>
                            <div className="text-gray-600 text-sm">
                                Get the official equipment roster template
                            </div>
                        </div>
                    </div>
                    <div className="bg-white px-3 py-1 border border-gray-200 rounded-full font-medium text-gray-500 text-sm">
                        Step 1 of 3
                    </div>
                </div>
            </div>
        </div>
    );
}
