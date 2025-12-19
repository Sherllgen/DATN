import Header, { NavigationSection } from "./header";
import HeroSection from "./hero-section";

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
            <Header navigationData={navigationData} />

            {/* Main Content */}
            <main className="flex flex-col">
                <HeroSection />
            </main>
        </div>
    );
}
