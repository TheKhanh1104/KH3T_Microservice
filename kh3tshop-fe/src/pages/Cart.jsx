import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { FaTrash } from "react-icons/fa";
import { toast } from "sonner";
import ChatBot from "../components/ChatBot"; 
import Contact from "../components/Contact";

const formatVND = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
    }).format(amount);
};

const calculateSummary = (items) => {
    if (!Array.isArray(items))
        return {
            subtotal: 0,
            discount: 0,
            shippingFee: 0,
            total: 0,
            shippingText: "Not Yet",
            minFreeShipping: 1000000,
        };

    const selectedItems = items.filter((item) => item.selected);
    const subtotal = selectedItems.reduce((sum, item) => sum + item.subtotal, 0);
    const minFreeShipping = 1000000;
    const standardShippingFee = 0;
    const discount = 0;

    const shippingFee = subtotal >= minFreeShipping ? 0 : standardShippingFee;
    const shippingText = subtotal >= minFreeShipping ? "Free" : "Not Yet";

    const total = subtotal - discount + shippingFee;

    return {
        subtotal,
        discount,
        shippingFee,
        total,
        shippingText,
        minFreeShipping,
    };
};

const Cart = () => {
    const navigate = useNavigate();
    const [cartItems, setCartItems] = useState(() => {
        const stored = localStorage.getItem("cachedCartItems");
        return stored ? JSON.parse(stored) : [];
    });
    const [select, setSelect] = useState([]);
    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem("user");
        return storedUser ? JSON.parse(storedUser) : null;
    });
    const [cart, setCart] = useState(() => {
        const stored = localStorage.getItem("cachedCart");
        return stored ? JSON.parse(stored) : null;
    });
    const [loading, setLoading] = useState(() => {
        return !!localStorage.getItem("accessToken");
    });

    const parseJsonResponse = async (response) => {
        const text = await response.text();
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch {
            return null;
        }
    };

    const fetchUser = async () => {
        try {
            const token = localStorage.getItem("accessToken");
            if (!token) {
                setLoading(false);
                return;
            }

            const res = await fetch(`/api/accounts/myinfor`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!res.ok) {
                console.error("Lỗi fetch user", res.status, res.statusText);
                setLoading(false);
                return;
            }

            const data = await parseJsonResponse(res);
            if (!data) {
                console.error("Lỗi fetch user: response rỗng hoặc không phải JSON");
                setLoading(false);
                return;
            }
            console.log("Tài khoản đang login: ", data.result);
            setUser(data.result);
        } catch (error) {
            console.error("Lỗi fetch user", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUser();
        
        // Tối ưu hóa: Load nhanh giỏ hàng song song nếu có user trong localStorage
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                if (parsed && parsed.id) {
                    fetchCartForUser(parsed.id);
                }
            } catch (e) {
                console.warn("Lỗi parse user từ localStorage:", e);
            }
        } else if (!localStorage.getItem("accessToken")) {
            setLoading(false);
        }
    }, []);

    const fetchCartForUser = async (userId) => {
        try {
            const token = localStorage.getItem("accessToken");
            if (!token) {
                setLoading(false);
                return;
            }
            const res = await fetch(
                `/api/carts/account/${userId}`,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (!res.ok) {
                console.error("Lỗi fetch cart", res.status, res.statusText);
                setLoading(false);
                return;
            }

            const data = await parseJsonResponse(res);
            if (!data) {
                console.error("Lỗi fetch cart: response rỗng hoặc không phải JSON");
                setLoading(false);
                return;
            }
            console.log("Cart của user: ", data.result);
            setCart(data.result);
            localStorage.setItem("cachedCart", JSON.stringify(data.result));
        } catch (error) {
            console.error("Lỗi fetch cart: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user?.id) {
            fetchCartForUser(user.id);
        }
    }, [user]);

    const hanldeFetchCart = async () => {
        try {
            setLoading(true);
            const token = localStorage.getItem("accessToken");
            const res = await fetch(
                `/api/cart-details/cart/${cart.id}`,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (!res.ok) {
                console.error("Lỗi fetch cart details:", res.status, res.statusText);
                setLoading(false);
                return;
            }
            const data = await res.json();
            console.log("Cart API: ", data);
            const items = (Array.isArray(data)
                ? data
                : data.result || data.cartDetails || []).map((item) => ({
                    ...item,
                    selected: item.selected !== undefined ? item.selected : item.isSelected,
                }));
            setCartItems(items);
            localStorage.setItem("cachedCartItems", JSON.stringify(items));
        } catch (err) {
            console.error("Lỗi hanldeFetchCart: ", err);
        } finally {
            setLoading(false);
        }
    };


    const handleToggleSelect = async (cartDetailId) => {
        const updatedItems = cartItems.map((item) =>
            item.id === cartDetailId ? { ...item, selected: !item.selected } : item
        );

        setCartItems(updatedItems);

        try {
            const token = localStorage.getItem("accessToken");
            await fetch(`/api/cart-details/${cartDetailId}/select`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    selected: updatedItems.find((i) => i.id === cartDetailId).selected,
                }),
            });
        } catch (err) {
            console.error("Lỗi update select: ", err);
        }
    };

    useEffect(() => {
        const selectedItems = cartItems.filter((item) => item.selected);
        setSelect(selectedItems);
    }, [cartItems]);

    useEffect(() => {
        console.log("Select state đã cập nhật:", select);
    }, [select]);

    const handleToggleIncrease = async (cartDetailId, priceAtTime) => {
        // 1. Cập nhật lạc quan state cartItems ngay lập tức
        const updatedItems = cartItems.map((item) =>
            item.id === cartDetailId
                ? { ...item, quantity: item.quantity + 1, subtotal: item.subtotal + priceAtTime }
                : item
        );
        setCartItems(updatedItems);
        localStorage.setItem("cachedCartItems", JSON.stringify(updatedItems));

        // 2. Cập nhật lạc quan state cart ngay lập tức
        let updatedCart = null;
        if (cart) {
            updatedCart = {
                ...cart,
                totalQuantity: cart.totalQuantity + 1,
                totalAmount: cart.totalAmount + priceAtTime
            };
            setCart(updatedCart);
            localStorage.setItem("cachedCart", JSON.stringify(updatedCart));
            // Phát CustomEvent mang theo data để Header cập nhật ngay tức thì 0ms!
            window.dispatchEvent(new CustomEvent("cartUpdated", { detail: updatedCart }));
        }

        // 3. Gọi API đồng bộ ngầm
        try {
            const token = localStorage.getItem("accessToken");
            Promise.all([
                fetch(`/api/cart-details/${cartDetailId}/increase-quantity`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }),
                fetch(`/api/carts/update/${cart.id}/increase`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ price: priceAtTime }),
                })
            ]);
        } catch (err) {
            console.error("Lỗi update quantity: ", err);
        }
    };

    const handleToggleDecrease = async (cartDetailId, priceAtTime) => {
        const currentItem = cartItems.find((i) => i.id === cartDetailId);
        if (!currentItem) return;

        if (currentItem.quantity <= 1) {
            handleDelete(cartDetailId, currentItem.quantity, currentItem.subtotal);
            return;
        }

        // 1. Cập nhật lạc quan state cartItems ngay lập tức
        const updatedItems = cartItems.map((item) =>
            item.id === cartDetailId
                ? { ...item, quantity: item.quantity - 1, subtotal: item.subtotal - priceAtTime }
                : item
        );
        setCartItems(updatedItems);
        localStorage.setItem("cachedCartItems", JSON.stringify(updatedItems));

        // 2. Cập nhật lạc quan state cart ngay lập tức
        let updatedCart = null;
        if (cart) {
            updatedCart = {
                ...cart,
                totalQuantity: cart.totalQuantity - 1,
                totalAmount: cart.totalAmount - priceAtTime
            };
            setCart(updatedCart);
            localStorage.setItem("cachedCart", JSON.stringify(updatedCart));
            window.dispatchEvent(new CustomEvent("cartUpdated", { detail: updatedCart }));
        }

        // 3. Gọi API đồng bộ ngầm
        try {
            const token = localStorage.getItem("accessToken");
            Promise.all([
                fetch(`/api/cart-details/${cartDetailId}/decrease-quantity`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }),
                fetch(`/api/carts/update/${cart.id}/decrease`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ price: priceAtTime }),
                })
            ]);
        } catch (err) {
            console.error("Lỗi decrease quantity: ", err);
        }
    };

    const handleDelete = async (cartDetailId, quantity, subtotal) => {
        // 1. Cập nhật lạc quan state cartItems ngay lập tức (xóa item)
        const updatedItems = cartItems.filter((item) => item.id !== cartDetailId);
        setCartItems(updatedItems);
        localStorage.setItem("cachedCartItems", JSON.stringify(updatedItems));

        // 2. Cập nhật lạc quan state cart ngay lập tức
        let updatedCart = null;
        if (cart) {
            updatedCart = {
                ...cart,
                totalQuantity: Math.max(0, cart.totalQuantity - quantity),
                totalAmount: Math.max(0, cart.totalAmount - subtotal)
            };
            setCart(updatedCart);
            localStorage.setItem("cachedCart", JSON.stringify(updatedCart));
            window.dispatchEvent(new CustomEvent("cartUpdated", { detail: updatedCart }));
        }

        // 3. Gọi API đồng bộ ngầm
        try {
            const token = localStorage.getItem("accessToken");
            Promise.all([
                fetch(`/api/cart-details/delete/${cartDetailId}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }),
                fetch(`/api/carts/update/${cart.id}/delete`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ price: subtotal, quantity: quantity }),
                })
            ]);
        } catch (err) {
            console.error("Lỗi xóa sản phẩm: ", err);
        }
    };

    useEffect(() => {
        if (cart?.id) {
            hanldeFetchCart();
        } else {
            setLoading(false);
        }
    }, [cart?.id]);

    const summary = calculateSummary(cartItems);

    const handleCheckout = () => {
        if (cartItems.length === 0) {
            toast.warning("Giỏ hàng rỗng!!!");
        } else if (select.length === 0) {
            toast.warning("Vui lòng chọn sản phẩm muốn thanh toán!!!");
        } else {
            localStorage.setItem("cartItems", JSON.stringify(cartItems));
            navigate("/checkout", {
                state: { userId: user.id, select: select },
            });
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen bg-gray-50">
                <div className="flex flex-col items-center gap-4">
                    <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-red-500 border-solid"></div>
                    <p className="text-gray-500 font-semibold text-lg animate-pulse">Loading your cart...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen py-10">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
                    <div className="lg:col-span-2">
                        <div className="flex justify-between items-center mb-10">
                            <h1 className="text-4xl font-bold text-gray-900">Cart</h1>
                            <span className="text-sm font-semibold text-gray-500 cursor-pointer hover:text-red-500">
                🔍︎ Track Order
              </span>
                        </div>
                        <div className="grid grid-cols-6 font-semibold border-b pb-3 text-gray-700 text-sm uppercase">
                            <div className="col-span-3">Item</div>
                            <div className="text-center">Quantity</div>
                            <div className="text-right">Unit Price</div>
                            <div className="text-center"></div>
                        </div>
                        {cartItems.length > 0 ? (
                            cartItems.map((item) => (
                                <div
                                    key={item.id}
                                    className="grid grid-cols-6 items-center border-b py-6"
                                >
                                    <div className="col-span-3 flex items-start space-x-4">
                                        <input
                                            type="checkbox"
                                            checked={item.selected}
                                            onChange={() => handleToggleSelect(item.id)}
                                            className="mt-2 w-4 h-4 border-gray-300 rounded"
                                        />

                                        <img
                                            src={item.productImage}
                                            alt={item.productName}
                                            className="w-24 h-24 object-cover rounded"
                                        />

                                        <div className="flex flex-col">
                                            <div className="font-semibold text-base hover:text-red-500">
                                                {item.productName}
                                            </div>
                                            <div className="text-gray-500 text-sm">
                                                {item.productName ? item.productName.split(",")[0] : ""}
                                            </div>
                                            <div className="text-gray-500 text-sm">
                                                Size: {item.sizeName}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="text-center">
                                        <div className="flex items-center justify-center border border-gray-300 rounded-full w-24 mx-auto p-1">
                                            <button
                                                className="text-lg px-2 hover:bg-gray-100 rounded-full"
                                                onClick={() =>
                                                    handleToggleDecrease(item.id, item.priceAtTime)
                                                }
                                            >
                                                -
                                            </button>

                                            <input
                                                type="number"
                                                value={item.quantity}
                                                min="1"
                                                readOnly
                                                className="w-10 text-center text-sm bg-transparent"
                                            />

                                            <button
                                                className="text-lg px-2 hover:bg-gray-100 rounded-full"
                                                onClick={() =>
                                                    handleToggleIncrease(item.id, item.priceAtTime)
                                                }
                                            >
                                                +
                                            </button>
                                        </div>
                                    </div>
                                    <div className="text-right font-semibold text-lg">
                                        {formatVND(item.subtotal)}
                                    </div>
                                    <div className="text-center">
                                        <button
                                            onClick={() =>
                                                handleDelete(item.id, item.quantity, item.subtotal)
                                            }
                                            className="text-gray-500 hover:text-red-500"
                                        >
                                            <FaTrash size={18} />
                                        </button>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="text-center py-10 text-gray-500">
                                Cart is empty.
                            </div>
                        )}

                        <div className="mt-8 flex justify-start">
                            <button
                                onClick={() => navigate("/product")}
                                className="px-6 py-3 border border-gray-300 text-gray-700 rounded-md transition font-semibold hover:bg-black hover:text-white"
                            >
                                Continue Shopping
                            </button>
                        </div>
                    </div>

                    <div className="lg:col-span-1 border-t-4 border-red-500 p-6 rounded-lg bg-gray-50 shadow-md h-fit">
                        <h2 className="text-3xl font-bold mb-6 text-red-500">Summary</h2>

                        <div className="mb-6 pb-4 border-b">
                            <div className="flex">
                                <input
                                    type="text"
                                    placeholder="Discount Code"
                                    className="flex-grow border border-gray-300 p-3 rounded-l focus:outline-none focus:ring-1 focus:ring-gray-400"
                                />
                                <button className="bg-black text-white px-4 py-3 rounded-r font-semibold hover:bg-gray-800 transition">
                                    Apply
                                </button>
                            </div>
                        </div>
                        <div className="space-y-4 mb-6">
                            <div className="flex justify-between text-lg text-gray-800">
                                <span>Subtotal:</span>
                                <span className="font-semibold">
                  {formatVND(summary.subtotal)}
                </span>
                            </div>
                            <div className="flex justify-between text-gray-600">
                                <span>Shipping fee:</span>
                                <span>{summary.shippingText}</span>
                            </div>
                            <div className="flex justify-between text-gray-600">
                                <span>Discount:</span>
                                <span>{formatVND(summary.discount)}</span>
                            </div>
                        </div>
                        <div className="flex justify-between font-bold text-xl border-t pt-4">
                            <span>Total:</span>
                            <span className="text-red-500">{formatVND(summary.total)}</span>
                        </div>
                        <button
                            onClick={handleCheckout}
                            className="w-full mt-8 bg-black text-white py-3 rounded font-bold text-lg hover:bg-gray-800 transition shadow-lg"
                        >
                            Proceed to Checkout
                        </button>
                    </div>
                </div>
            </div>
            <ChatBot/>
      <Contact/>
        </div>
    );
};

export default Cart;
