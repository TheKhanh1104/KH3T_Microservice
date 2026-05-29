import React from "react"
import { useState, useEffect } from "react"
import { useNavigate } from "react-router"
import { toast } from "sonner";

const ResetPassword = () => {
    const navigate = useNavigate();
    const [otp, setOtp] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [timeLeft, setTimeLeft] = useState(() => {
        const expiry = sessionStorage.getItem("otpExpiry");
        if (expiry) {
            const remaining = Math.floor((parseInt(expiry) - Date.now()) / 1000);
            return remaining > 0 ? remaining : 0;
        }
        return 120;
    });
    const [canResend, setCanResend] = useState(false);
    const [redirectCountdown, setRedirectCountdown] = useState(null);

    // Update timeLeft if session changes (e.g., initial load)
    useEffect(() => {
        const expiry = sessionStorage.getItem("otpExpiry");
        if (expiry) {
            const remaining = Math.floor((parseInt(expiry) - Date.now()) / 1000);
            setTimeLeft(remaining > 0 ? remaining : 0);
        }
    }, []);

    useEffect(() => {
        const token = sessionStorage.getItem("resetToken");
        if (!token) {
            toast.error("Vui lòng thực hiện yêu cầu quên mật khẩu trước!");
            navigate("/forgot_password");
        }
    }, [navigate]);

    // Countdown logic for OTP
    useEffect(() => {
        if (timeLeft <= 0) {
            setCanResend(true);
            return;
        }
        setCanResend(false);
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    setCanResend(true);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(timer);
    }, [timeLeft]);

    // Countdown logic for Redirection
    useEffect(() => {
        if (redirectCountdown === null) return;
        if (redirectCountdown <= 0) {
            navigate("/login");
            return;
        }
        const timer = setInterval(() => {
            setRedirectCountdown((prev) => prev - 1);
        }, 1000);
        return () => clearInterval(timer);
    }, [redirectCountdown, navigate]);

    const handleResendOtp = async () => {
        const email = sessionStorage.getItem("resetEmail");
        if (!email) {
            toast.error("Không tìm thấy email để gửi lại mã!");
            return;
        }

        setLoading(true);
        try {
            const response = await fetch("/api/auth/forgot-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: email }),
            });

            const data = await response.json();
            if (response.ok) {
                sessionStorage.setItem("resetToken", data.result.token);
                sessionStorage.setItem("otp", data.result.otp); // Only if dev/test

                // Cập nhật lại thời gian hết hạn mới
                const expiryTime = Date.now() + 120 * 1000;
                sessionStorage.setItem("otpExpiry", expiryTime.toString());

                toast.success("Mã OTP mới đã được gửi vào email của bạn!");
                setTimeLeft(120);
                setCanResend(false);
            } else {
                toast.error(data.message || "Gửi lại mã thất bại!");
            }
        } catch (error) {
            toast.error("Lỗi kết nối Server!");
        } finally {
            setLoading(false);
        }
    };

    const handleResetSubmit = async () => {
        if (!otp || !newPassword) {
            toast.warning("Vui lòng nhập đầy đủ OTP và mật khẩu mới.");
            return;
        }
        const token = sessionStorage.getItem("resetToken");

        setLoading(true);
        try {
            const response = await fetch("/api/auth/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    token: token,
                    otp: otp,
                    newPassword: newPassword
                }),
            });

            const data = await response.json();
            console.log("Reset Password Response:", data);

            // Chấp nhận cả code 1000 (mới) và 0 (cũ) hoặc nếu message chứa chữ "successfully"
            if (response.ok && (data.code === 1000 || data.code === 0 || data.result?.toLowerCase().includes("successfully"))) {
                toast.success("Mật khẩu đã được đổi thành công!");

                // CLEAN UP SESSION
                sessionStorage.removeItem("resetToken");
                sessionStorage.removeItem("otp");
                sessionStorage.removeItem("resetEmail");
                sessionStorage.removeItem("otpExpiry");

                // Start 5s redirect countdown
                setRedirectCountdown(5);
            } else {
                // Nếu là lỗi thật sự
                toast.error(data.message || data.result || "Mã OTP không chính xác hoặc đã hết hạn!");
            }
        } catch (error) {
            console.error("Error:", error);
            toast.error("Lỗi kết nối Server!");
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? "0" : ""}${secs}`;
    };

    return (
        <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
            <div className="bg-gray-100 rounded-3xl shadow-2xl overflow-hidden max-w-4xl w-full grid md:grid-cols-2">

                {/* Left side - Form */}
                <div className="p-8 md:p-12">
                    <h2 className="font-bold text-4xl mb-3">Reset Password</h2>

                    {redirectCountdown !== null ? (
                        <div className="text-center py-10">
                            <div className="text-green-500 text-6xl mb-4">✓</div>
                            <p className="text-xl font-semibold text-gray-800">Thành công!</p>
                            <p className="text-gray-600 mt-2">
                                Bạn sẽ được tự động chuyển về trang đăng nhập sau <span className="font-bold text-red-500">{redirectCountdown}s</span>...
                            </p>
                        </div>
                    ) : (
                        <div className="grid gap-6">
                            {/* Input OTP */}
                            <div>
                                <div className="flex justify-between items-center mb-2">
                                    <label className="text-gray-700 font-medium">Mã OTP (6 số):</label>
                                    <span className={`text-sm font-bold ${timeLeft < 30 ? "text-red-500" : "text-gray-500"}`}>
                                        {formatTime(timeLeft)}
                                    </span>
                                </div>
                                <input
                                    type="text"
                                    value={otp}
                                    onChange={(e) => setOtp(e.target.value)}
                                    className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:border-red-400 transition tracking-widest"
                                    placeholder="XXXXXX"
                                    maxLength={6}
                                />
                                <div className="mt-2 flex justify-end">
                                    <button
                                        onClick={handleResendOtp}
                                        disabled={!canResend || loading}
                                        className={`text-sm font-semibold underline ${canResend ? "text-blue-600 hover:text-blue-800" : "text-gray-400 cursor-not-allowed"}`}
                                    >
                                        Gửi lại mã OTP
                                    </button>
                                </div>
                            </div>

                            {/* Input New Password */}
                            <div>
                                <div className="flex items-center gap-2 mb-2">
                                    <label className="text-gray-700 font-medium">Mật khẩu mới:</label>
                                </div>
                                <input
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    className="w-full border-2 border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:border-red-400 transition"
                                    placeholder="Nhập mật khẩu mới..."
                                />
                            </div>

                            <button
                                onClick={handleResetSubmit}
                                disabled={loading || timeLeft <= 0}
                                className={`w-full py-4 rounded-lg text-white font-bold text-lg transition mt-4 ${
                                    loading || timeLeft <= 0 ? "bg-gray-500 cursor-not-allowed" : "bg-red-500 hover:bg-red-600"
                                }`}
                            >
                                {loading ? "ĐANG XỬ LÝ..." : "XÁC NHẬN ĐỔI MẬT KHẨU"}
                            </button>

                            <button
                                className="text-sm text-gray-500 hover:underline text-center mt-2"
                                onClick={() => {
                                    sessionStorage.removeItem("resetToken");
                                    sessionStorage.removeItem("resetEmail");
                                    navigate("/forgot_password");
                                }}
                            >
                                Quay lại nhập Email khác
                            </button>
                        </div>
                    )}
                </div>

                {/* Right side - Image */}
                <div className="hidden md:flex items-center justify-center bg-gradient-to-br from-red-400 to-red-500 p-12">
                    <div className="relative">
                        <div className="absolute inset-0 bg-red-300 rounded-full blur-3xl opacity-50"></div>
                        <img
                            src="https://i.postimg.cc/J0TgG6NZ/Thiet-ke-chua-co-ten-(6).png"
                            alt="Profile"
                            className="relative rounded-full w-80 h-80 object-cover border-8 border-white shadow-2xl"
                        />
                    </div>
                </div>

            </div>
        </div>
    );
};
export default ResetPassword;
