import React, { useState, useEffect } from "react";
import {
  Package,
  Clock,
  CheckCircle,
  XCircle,
  Truck,
  DollarSign,
  CheckCircle2,
  ListChecks,
} from "lucide-react";
import { useNavigate } from "react-router";
import { toast } from "sonner";

const Order = () => {
  const [activeTab, setActiveTab] = useState("all");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const userId = localStorage.getItem("userId");
  const navigate = useNavigate();

  const fetchOrders = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      let resolvedUserId = userId;
      if (!resolvedUserId) {
        try {
          const meRes = await fetch(`/api/accounts/myinfor`, {
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          });
          if (meRes.ok) {
            const meData = await meRes.json();
            resolvedUserId = meData?.result?.id ? String(meData.result.id) : null;
            if (resolvedUserId) {
              localStorage.setItem("userId", resolvedUserId);
            }
          }
        } catch (e) {
          console.warn("Could not fetch user info:", e);
        }
      }

      if (!resolvedUserId) {
        setOrders([]);
        setLoading(false);
        return;
      }

      // Thử saga API trước
      try {
        const sagaRes = await fetch(`/api/orders/saga/user/${encodeURIComponent(resolvedUserId)}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (sagaRes.ok) {
          const sagaData = await sagaRes.json();
          // Đảm bảo sagaData là array trước khi gọi .map()
          const sagaList = Array.isArray(sagaData)
            ? sagaData
            : Array.isArray(sagaData?.result)
            ? sagaData.result
            : [];

          setOrders(
            sagaList.map((order) => ({
              id: order.id,
              orderCode: order.id,
              orderDate: order.createdAt,
              statusOrder: order.status,
              paymentMethod: order.paymentMethod || "CASH",
              note: order.note || "",
              totalAmount: order.totalAmount,
              customerTrading: order.customerTrading || {
                receiverName: order.userId,
              },
              orderDetails: (Array.isArray(order.items) ? order.items : []).map((item) => ({
                id: item.id,
                productId: item.productId,
                productName: item.productName,
                quantity: item.quantity,
                unitPrice: item.unitPrice,
                totalPrice: item.unitPrice * item.quantity,
              })),
            }))
          );
          return;
        }
      } catch (sagaErr) {
        console.warn("Saga orders request failed:", sagaErr);
      }

      // Fallback: legacy orders API
      try {
        const res = await fetch(`/api/orders/account/${resolvedUserId}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (!res.ok) {
          console.error("Legacy orders API failed:", res.status);
          setOrders([]);
          return;
        }

        const data = await res.json();
        setOrders(Array.isArray(data) ? data : Array.isArray(data?.result) ? data.result : []);
      } catch (legacyErr) {
        console.warn("Legacy orders request failed:", legacyErr);
        setOrders([]);
      }
    } catch (error) {
      console.error("Error fetching orders:", error);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price);
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusInfo = (status) => {
    const statusMap = {
      PENDING: {
        label: "Pending Confirmation",
        color: "text-yellow-600",
        bgColor: "bg-yellow-50",
        icon: <Clock size={16} />,
      },
      CONFIRMED: {
        label: "Confirmed",
        color: "text-blue-600",
        bgColor: "bg-blue-50",
        icon: <CheckCircle size={16} />,
      },
      SHIPPING: {
        label: "Shipping",
        color: "text-purple-600",
        bgColor: "bg-purple-50",
        icon: <Truck size={16} />,
      },
      COMPLETED: {
        label: "Completed",
        color: "text-green-600",
        bgColor: "bg-green-50",
        icon: <CheckCircle size={16} />,
      },
      CANCELLED: {
        label: "Cancelled",
        color: "text-red-600",
        bgColor: "bg-red-50",
        icon: <XCircle size={16} />,
      },
    };
    return statusMap[status] || statusMap.PENDING;
  };

  const getPaymentMethodLabel = (method) => {
    const methodMap = {
      COD: "Cash on Delivery",
      BANKING: "Bank Transfer",
      MOMO: "MoMo Wallet",
      VNPAY: "VNPAY",
    };
    return methodMap[method] || method;
  };

  const getFilteredOrders = () => {
    if (activeTab === "all") return orders;
    if (activeTab === "pending")
      return orders.filter((o) => (o.statusOrder || o.status) === "PENDING");
    if (activeTab === "confirmed")
      return orders.filter((o) => (o.statusOrder || o.status) === "CONFIRMED");
    return orders;
  };

  const isUuid = (value) =>
    typeof value === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);

  const filteredOrders = getFilteredOrders();

  const handleCancel = async (id) => {
    toast(
      (t) => (
        <div className="p-3">
          <p className="font-semibold text-sm text-gray-900 flex items-center gap-2">
            <span className="text-red-500 text-sm">⚠️</span>
            Are you sure you want to cancel this order?
          </p>
          <div className="flex gap-3 mt-4 justify-start">
            <button
              className="
            px-4 py-2 
            bg-red-500 text-white 
            rounded-md 
            hover:bg-red-600 
            transition 
            font-medium 
            shadow-sm
          "
              onClick={async () => {
                toast.dismiss(t);

                try {
                  const token = localStorage.getItem("accessToken");

                  if (isUuid(id)) {
                    await fetch(`/api/orders/${id}/cancel`, {
                      method: "PUT",
                      headers: {
                        Authorization: `Bearer ${token}`,
                      },
                    });
                  } else {
                    await fetch(`/api/orders/status/${id}`, {
                      method: "PUT",
                      headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                      },
                      body: JSON.stringify({ statusOrder: "CANCELLED" }),
                    });
                  }

                  await fetchOrders();

                  toast.success("Cancel successful!!!");
                } catch (error) {
                  toast.error("Error to cancel!");
                }
              }}
            >
              Cancel
            </button>
            <button
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition font-medium"
              onClick={() => toast.dismiss(t)}
            >
              No
            </button>
          </div>
        </div>
      ),
      {
        duration: Infinity,
        className: "shadow-lg rounded-xl border border-gray-200",
      }
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="mb-8">
          <h1 className="text-3xl sm:text-4xl font-bold mb-2">My Orders</h1>
          <p className="text-gray-600">Manage and track your orders</p>
        </div>

        <div className="flex justify-center mb-10">
          <div className="w-full bg-white rounded-full shadow-lg p-1 flex gap-1 justify-between border border-gray-200">
            {[
              { key: "all", label: "All", icon: <ListChecks size={18} /> },
              {
                key: "pending",
                label: "Pending",
                icon: <Clock size={18} />,
              },
              {
                key: "confirmed",
                label: "Confirmed",
                icon: <CheckCircle2 size={18} />,
              },
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`
                  flex-1 px-8 py-3 rounded-full font-semibold text-base
                  flex items-center justify-center gap-2
                  transition-all duration-300 ease-in-out
                  ${
                    activeTab === tab.key
                      ? "bg-black text-white shadow-md scale-[1.03]"
                      : "text-gray-700 hover:bg-gray-100 hover:text-black"
                  }
                `}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-red-500 border-t-transparent"></div>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="text-center py-16">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mb-4">
              <Package size={40} className="text-gray-400" />
            </div>
            <h3 className="text-xl font-semibold mb-2">No Orders Yet</h3>
            <p className="text-gray-600 mb-6">
              You currently have no orders in this section
            </p>
            <button
              onClick={() => (window.location.href = "/product")}
              className="px-8 py-3 bg-black text-white rounded-full font-medium hover:bg-red-500 transition"
            >
              Shop Now
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.map((order) => {
              const statusInfo = getStatusInfo(order.statusOrder || order.status);
              return (
                <div
                  key={order.id}
                  className="bg-white rounded-lg shadow-md hover:shadow-xl transition duration-300 overflow-hidden"
                >
                  <div className="p-4 sm:p-6 border-b border-gray-100">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-4">
                      <div className="text-sm">
                        <p className="font-semibold text-gray-900">
                          Order Code: {order.orderCode || order.id}
                        </p>
                        <p className="text-gray-600 mt-1">
                          Order Date: {formatDate(order.orderDate || order.createdAt)}
                        </p>
                      </div>

                      <div
                        className={`inline-flex items-center gap-2 px-4 py-2 rounded-full ${statusInfo.bgColor} ${statusInfo.color} text-sm font-medium`}
                      >
                        {statusInfo.icon}
                        <span>{statusInfo.label}</span>
                      </div>
                    </div>

                    <div className="bg-gray-50 rounded-lg p-4 text-sm">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                        <div>
                          <span className="text-gray-600">Receiver: </span>
                          <span className="font-medium">
                            {order.customerTrading?.receiverName || "N/A"}
                          </span>
                        </div>
                        <div>
                          <span className="text-gray-600">Phone: </span>
                          <span className="font-medium">
                            {order.customerTrading?.receiverPhone || "N/A"}
                          </span>
                        </div>
                        <div className="sm:col-span-2">
                          <span className="text-gray-600">Address: </span>
                          <span className="font-medium">
                            {order.customerTrading?.receiverAddress || "N/A"}
                          </span>
                        </div>
                        <div className="flex items-center gap-2">
                          <DollarSign size={16} className="text-gray-600" />
                          <span className="font-medium">
                            {getPaymentMethodLabel(order.paymentMethod || "")}
                          </span>
                        </div>
                      </div>

                      {order.note && (
                        <div className="mt-2 pt-2 border-t border-gray-200">
                          <span className="text-gray-600">Note: </span>
                          <span className="font-medium italic">
                            {order.note}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="p-4 sm:p-6">
                    <div className="space-y-4">
                      {(order.orderDetails || order.items || []).map((detail) => (
                        <div key={detail.id} className="flex gap-4">
                          <div className="flex-1 min-w-0">
                            <h4
                              className="font-semibold text-gray-900 line-clamp-2 hover:text-red-500"
                              onClick={() =>
                                navigate(`/product/${detail.productId}`)
                              }
                            >
                              {detail.productName}
                            </h4>
                            <p className="text-sm text-gray-600 mt-1">
                              Quantity: {detail.quantity}
                            </p>
                            <p className="text-red-500 font-bold mt-1">
                              {formatPrice(detail.unitPrice)}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-sm text-gray-600">Total Price</p>
                            <p className="text-red-500 font-bold">
                              {formatPrice(detail.quantity * detail.unitPrice)}
                            </p>
                          </div>
                        </div>
                      ))}
                      <div className="flex gap-4">
                        <div className="flex-1 min-w-0">
                          <h4 className="font-semibold text-gray-900 line-clamp-2">
                            Shopping fee
                          </h4>
                        </div>
                        <div className="text-right">
                          <p className="text-red-500 font-bold">
                            {formatPrice(30000)}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="mt-6 pt-4 border-t border-gray-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                      <div className="text-right sm:text-left">
                        <p className="text-sm text-gray-600">Total Amount:</p>
                        <p className="text-2xl font-bold text-red-500">
                          {formatPrice(
                            ((order.orderDetails || order.items || []).reduce(
                              (sum, detail) => sum + (detail.totalPrice || detail.quantity * detail.unitPrice || 0),
                              0
                            )) + 30000
                          )}
                        </p>
                      </div>

                      <div className="flex gap-3 flex-wrap">
                        {(order.statusOrder || order.status) === "PENDING" && (
                          <button
                            className="px-6 py-2 bg-red-500 text-white rounded-full font-medium hover:bg-red-600 transition"
                            onClick={() => handleCancel(order.id)}
                          >
                            Cancel Order
                          </button>
                        )}

                        {(order.statusOrder || order.status) === "COMPLETED" && (
                          <button className="px-6 py-2 bg-black text-white rounded-full font-medium hover:bg-gray-800 transition">
                            Buy Again
                          </button>
                        )}

                        {(order.statusOrder || order.status) === "SHIPPING" && (
                          <button className="px-6 py-2 bg-blue-500 text-white rounded-full font-medium hover:bg-blue-600 transition">
                            Track Order
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default Order;
