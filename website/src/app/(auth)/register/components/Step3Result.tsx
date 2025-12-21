import { Button } from "@/components/ui/button";
import { CheckCircle2, AlertCircle } from "lucide-react";

interface Step3ResultProps {
    status: "success" | "error" | null;
    onReset: () => void;
}

export function Step3Result({ status, onReset }: Step3ResultProps) {
    return (
        <div className="space-y-6 text-center">
            {status === "success" ? (
                <>
                    <div className="flex justify-center items-center bg-green-500/10 mx-auto rounded-full w-20 h-20">
                        <CheckCircle2 className="w-10 h-10 text-green-500" />
                    </div>
                    <div className="space-y-2">
                        <h3 className="font-semibold text-green-500 text-xl">
                            Đăng Ký Thành Công!
                        </h3>
                        <p className="text-muted-foreground">
                            Hồ sơ của bạn đã được gửi thành công. Chúng tôi sẽ
                            xem xét và phản hồi trong vòng 24-48 giờ.
                        </p>
                    </div>
                    <div className="bg-green-500/5 mx-auto p-4 border border-green-500/20 rounded-lg max-w-md">
                        <p className="text-muted-foreground text-sm">
                            Mã đăng ký:{" "}
                            <span className="font-mono font-semibold">
                                REG-{Date.now()}
                            </span>
                        </p>
                        <p className="mt-2 text-muted-foreground text-xs">
                            Vui lòng kiểm tra email để theo dõi trạng thái đăng
                            ký
                        </p>
                    </div>
                </>
            ) : (
                <>
                    <div className="flex justify-center items-center bg-red-500/10 mx-auto rounded-full w-20 h-20">
                        <AlertCircle className="w-10 h-10 text-red-500" />
                    </div>
                    <div className="space-y-2">
                        <h3 className="font-semibold text-red-500 text-xl">
                            Đăng Ký Thất Bại
                        </h3>
                        <p className="text-muted-foreground">
                            Có lỗi xảy ra trong quá trình xử lý. Vui lòng kiểm
                            tra lại file và thử lại.
                        </p>
                    </div>
                    <div className="bg-red-500/5 mx-auto p-4 border border-red-500/20 rounded-lg max-w-md">
                        <p className="text-muted-foreground text-sm">
                            Lỗi: File không đúng định dạng hoặc thiếu thông tin
                            bắt buộc
                        </p>
                    </div>
                </>
            )}
            <Button
                onClick={onReset}
                variant={status === "success" ? "default" : "outline"}
                size="lg"
                className="w-full max-w-md"
            >
                {status === "success" ? "Hoàn Tất" : "Thử Lại"}
            </Button>
        </div>
    );
}
