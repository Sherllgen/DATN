"use client";

import Link from "next/link";
import Image from "next/image";
import { ArrowRight, Play, Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { DotPattern } from "@/components/dot-pattern";

export function HeroSection() {
    return (
        <section className="relative pt-20 sm:pt-32 pb-16 overflow-hidden">
            {/* Background Pattern */}
            <div className="absolute inset-0">
                {/* Dot pattern overlay using reusable component */}
                <DotPattern
                    className="opacity-100"
                    size="lg"
                    fadeStyle="ellipse"
                />
            </div>

            <div className="relative mx-auto px-4 sm:px-6 lg:px-8 container">
                <div className="mx-auto max-w-4xl text-center">
                    {/* Main Headline */}
                    <h1 className="mb-6 font-bold text-4xl sm:text-6xl lg:text-7xl tracking-tight">
                        Centralized Management for EV Charging Stations
                    </h1>

                    {/* Subheading */}
                    <p className="mx-auto mb-10 max-w-2xl text-muted-foreground text-lg sm:text-xl">
                        Track charger status, control pricing, and gain insights
                        through real-time data and analytics — all in one
                        system.
                    </p>

                    {/* CTA Buttons */}
                    <div className="flex sm:flex-row flex-col sm:justify-center gap-4">
                        <Button
                            size="lg"
                            className="group text-base transition-colors duration-200 cursor-pointer"
                            asChild
                        >
                            <Link
                                href="/register"
                                className="inline-flex items-center"
                            >
                                Get Started
                                <ArrowRight className="opacity-0 group-hover:opacity-100 ml-2 w-4 h-4 transition-transform -translate-x-1 group-hover:translate-x-0 duration-200 ease-out" />
                            </Link>
                        </Button>
                    </div>
                </div>

                {/* Hero Image/Visual */}
                <div className="hidden mx-auto mt-20 max-w-6xl">
                    <div className="group relative">
                        {/* Top background glow effect - positioned above the image */}
                        <div className="top-2 lg:-top-8 left-1/2 absolute bg-primary/50 blur-3xl mx-auto rounded-full w-[90%] h-24 lg:h-80 -translate-x-1/2 transform"></div>

                        <div className="relative bg-card shadow-2xl border rounded-xl">
                            {/* Light mode dashboard image */}
                            <Image
                                src="/dashboard-light.png"
                                alt="Dashboard Preview - Light Mode"
                                width={1200}
                                height={800}
                                className="dark:hidden block rounded-xl w-full object-cover"
                                priority
                            />

                            {/* Dark mode dashboard image */}
                            <Image
                                src="/dashboard-dark.png"
                                alt="Dashboard Preview - Dark Mode"
                                width={1200}
                                height={800}
                                className="hidden dark:block rounded-xl w-full object-cover"
                                priority
                            />

                            {/* Bottom fade effect - gradient overlay that fades the image to background */}
                            <div className="bottom-0 left-0 absolute bg-gradient-to-b from-background/0 via-background/70 to-background rounded-b-xl w-full h-32 md:h-40 lg:h-48"></div>

                            {/* Overlay play button for demo */}
                            <div className="absolute inset-0 flex justify-center items-center">
                                <Button
                                    size="lg"
                                    className="p-0 rounded-full w-16 h-16 hover:scale-105 transition-transform cursor-pointer"
                                    asChild
                                >
                                    <a href="#" aria-label="Watch demo video">
                                        <Play className="fill-current w-6 h-6" />
                                    </a>
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
