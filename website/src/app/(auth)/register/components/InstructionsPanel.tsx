import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BookOpen, Info } from "lucide-react";

export function InstructionsPanel() {
    return (
        <Card className="hidden lg:block top-4 sticky shadow h-fit">
            <CardHeader>
                <div className="flex items-center gap-2">
                    <BookOpen className="w-5 h-5 text-primary" />
                    <CardTitle className="text-lg">Hướng Dẫn Đăng Ký</CardTitle>
                </div>
            </CardHeader>
            <CardContent className="space-y-6">
                {/* Step 1 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                1
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">Tải Hồ Sơ Mẫu</h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Tải file Excel mẫu từ hệ thống</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Điền đầy đủ thông tin theo từng mục</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Kiểm tra kỹ trước khi lưu file</span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Step 2 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                2
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">Upload Hồ Sơ</h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Chọn file đã điền thông tin</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Hỗ trợ định dạng .xlsx, .xls, .pdf</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Dung lượng tối đa 10MB</span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Step 3 Guide */}
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <div className="flex justify-center items-center bg-primary/10 rounded-full w-8 h-8 shrink-0">
                            <span className="font-semibold text-primary text-sm">
                                3
                            </span>
                        </div>
                        <h4 className="font-semibold text-sm">Nhận Kết Quả</h4>
                    </div>
                    <ul className="space-y-2 ml-10 text-muted-foreground text-xs">
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Hệ thống xử lý tự động trong 2-5 phút</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Nhận thông báo qua email trong 24-48h</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="bg-primary/20 mt-1.5 rounded-full w-1 h-1 shrink-0" />
                            <span>Lưu lại mã đăng ký để theo dõi</span>
                        </li>
                    </ul>
                </div>

                <div className="border-t" />

                {/* Important Notes */}
                <div className="bg-blue-500/5 p-3 border border-blue-500/20 rounded-lg">
                    <div className="flex items-start gap-2">
                        <Info className="mt-0.5 w-4 h-4 text-blue-500 shrink-0" />
                        <div className="space-y-1">
                            <p className="font-semibold text-blue-500 text-xs">
                                Lưu ý quan trọng
                            </p>
                            <ul className="space-y-1 text-muted-foreground text-xs">
                                <li>• Thông tin phải chính xác và đầy đủ</li>
                                <li>
                                    • Đảm bảo file không bị lỗi hoặc hư hỏng
                                </li>
                                <li>• Liên hệ hotline nếu cần hỗ trợ</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
