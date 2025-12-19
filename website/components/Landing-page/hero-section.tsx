import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import Link from "next/link";

const HeroSection = () => {
    return (
        <section className="flex flex-col flex-1 justify-between gap-12 sm:gap-16 lg:gap-24 pt-8 sm:pt-16 lg:pt-24 min-h-[calc(100dvh-4rem)] overflow-x-hidden">
            {/* Hero Content */}
            <div className="flex flex-col items-center gap-8 mx-auto px-4 sm:px-6 lg:px-8 max-w-7xl text-center">
                <div className="flex items-center gap-2.5 bg-muted px-3 py-2 border rounded-full">
                    <Badge>Smart & Automated</Badge>
                    <span className="text-muted-foreground">
                        Nền tảng quản lý trạm sạc xe máy điện
                    </span>
                </div>

                <h1 className="font-bold text-3xl sm:text-4xl lg:text-5xl text-balance leading-[1.29167]">
                    Quản Lý Trạm Sạc Xe Máy Điện
                    <br />
                    <span className="relative pb-2">
                        Thông Minh
                        <svg
                            width="223"
                            height="12"
                            viewBox="0 0 223 12"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            className="max-sm:hidden bottom-0 absolute inset-x-0 w-full translate-y-1/2"
                        >
                            <path
                                d="M1.11716 10.428C39.7835 4.97282 75.9074 2.70494 114.894 1.98894C143.706 1.45983 175.684 0.313587 204.212 3.31596C209.925 3.60546 215.144 4.59884 221.535 5.74551"
                                stroke="url(#paint0_linear_10365_68643)"
                                strokeWidth="2"
                                strokeLinecap="round"
                            />
                            <defs>
                                <linearGradient
                                    id="paint0_linear_10365_68643"
                                    x1="18.8541"
                                    y1="3.72033"
                                    x2="42.6487"
                                    y2="66.6308"
                                    gradientUnits="userSpaceOnUse"
                                >
                                    <stop stopColor="var(--primary)" />
                                    <stop
                                        offset="1"
                                        stopColor="var(--primary-foreground)"
                                    />
                                </linearGradient>
                            </defs>
                        </svg>
                    </span>{" "}
                    & Hiệu Quả
                </h1>

                <p className="max-w-3xl text-muted-foreground">
                    Quản lý trạm sạc xe máy điện dễ dàng với nền tảng của chúng
                    tôi: theo dõi trạng thái trạm theo thời gian thực, quản lý
                    người dùng, thanh toán tự động và tối ưu hiệu suất vận hành
                    <br />
                    tất cả trong một hệ thống duy nhất.
                </p>

                <Button size="lg" asChild>
                    <Link href="#">Đăng ký ngay</Link>
                </Button>
            </div>

            {/* Image */}
            <img
                src="https://cdn.shadcnstudio.com/ss-assets/blocks/marketing/hero/image-19.png"
                alt="Dishes"
                className="w-full min-h-67 object-cover"
            />
        </section>
    );
};

export default HeroSection;
