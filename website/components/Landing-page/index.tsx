import Header, { NavigationSection } from "./header";
import HeroSection from "./hero-section";
import FeaturesSection from "./features-section";
import GallerySection from "./gallery-section";
import HowItWorksSection from "./how-it-works-section";

const navigationData: NavigationSection[] = [
    {
        title: "Home",
        href: "#",
    },
    {
        title: "Products",
        href: "#",
    },
    {
        title: "About Us",
        href: "#",
    },
    {
        title: "Contacts",
        href: "#",
    },
];

export default function LandingPage() {
    return (
        <div className="relative">
            {/* Header Section */}
            <Header
                navigationData={navigationData}
                className="bg-transparent"
            />

            {/* Main Content */}
            <main className="flex flex-col">
                <HeroSection />
                {/* <FeaturesSection /> */}
                {/* <HowItWorksSection /> */}
                {/* <GallerySection /> */}
            </main>
        </div>
    );
}
