// SVG Imports
import LogoSvg from "@/assets/svgs/logo";

// Util Imports
import { cn } from "@/lib/utils";

const Logo = ({ className }: { className?: string }) => {
    return (
        <div className={cn("flex items-center gap-2.5", className)}>
            <LogoSvg className="size-8.5" />
            <span className="font-semibold text-xl">shadcn/studio</span>
        </div>
    );
};

export default Logo;
