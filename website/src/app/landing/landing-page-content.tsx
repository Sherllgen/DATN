"use client";

import React from "react";
import { LandingNavbar } from "./components/navbar";
import { HeroSection } from "./components/hero-section";
import { LogoCarousel } from "./components/logo-carousel";
import { StatsSection } from "./components/stats-section";
import { FeaturesSection } from "./components/features-section";
import { TeamSection } from "./components/team-section";
import { TestimonialsSection } from "./components/testimonials-section";
import { BlogSection } from "./components/blog-section";
import { PricingSection } from "./components/pricing-section";
import { CTASection } from "./components/cta-section";
import { ContactSection } from "./components/contact-section";
import { FaqSection } from "./components/faq-section";
import { LandingFooter } from "./components/footer";
import {
    LandingThemeCustomizer,
    LandingThemeCustomizerTrigger,
} from "./components/landing-theme-customizer";
import { AboutSection } from "./components/about-section";

export function LandingPageContent() {
    const [themeCustomizerOpen, setThemeCustomizerOpen] = React.useState(false);

    return (
        <div className="bg-background min-h-screen">
            {/* Gradient layer */}
            <div className="absolute inset-0">
                <div className="top-[-75%] left-[0%] absolute bg-purple-500/3 blur-3xl rounded-full w-[60rem] h-[70rem]" />
                <div className="top-[40%] right-[60%] absolute bg-emerald-400/3 blur-3xl rounded-full w-[40rem] h-[40rem]" />
                <div className="right-[0%] bottom-[-25%] absolute bg-pink-500/3 blur-3xl rounded-full w-[40rem] h-[50rem]" />
            </div>

            {/* Navigation */}
            <LandingNavbar />

            {/* Main Content */}
            <main>
                <HeroSection />
                {/* <LogoCarousel />
                <StatsSection />
                <AboutSection />
                <FeaturesSection />
                <TeamSection />
                <PricingSection />
                <TestimonialsSection />
                <BlogSection />
                <FaqSection />
                <CTASection />
                <ContactSection /> */}
            </main>

            {/* Footer */}
            {/* <LandingFooter /> */}

            {/* Theme Customizer */}
            {/* <LandingThemeCustomizerTrigger
                onClick={() => setThemeCustomizerOpen(true)}
            />
            <LandingThemeCustomizer
                open={themeCustomizerOpen}
                onOpenChange={setThemeCustomizerOpen}
            /> */}
        </div>
    );
}
