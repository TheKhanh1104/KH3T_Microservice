import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { toast } from "sonner";

// Helper to fetch and surface server error bodies in logs/toasts
async function safeFetch(url, options = {}, label = url) {
  const res = await fetch(url, options);
  if (!res.ok) {
    let text;
    try {
      text = await res.text();
    } catch (e) {
      text = res.statusText;
    }
    let message = text;
    try {
      const parsed = JSON.parse(text);
      if (parsed && parsed.message) message = parsed.message;
    } catch (e) {
      // not json
    }
    console.error(`${label} failed:`, res.status, message);
    throw new Error(message || `HTTP ${res.status}`);
  }
  return res;
}

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
      shippingFee: 30000,
      total: 0,
    };

  const selectedItems = items.filter((i) => i.selected);
  const subtotal = selectedItems.reduce((s, i) => s + i.subtotal, 0);
  const discount = 0;
  const shippingFee = 30000;
  const total = subtotal - discount + shippingFee;

  return { subtotal, discount, shippingFee, total };
};

const Checkout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [addresses, setAddresses] = useState([]);
  const userId = location.state?.userId ?? localStorage.getItem("userId");
  const product = location.state?.product;
  const quantity = location.state?.quantity;
  const selectedCartItems = location.state?.select || [];
  const [cartItems, setCartItems] = useState([]);
  const [payment, setPayment] = useState("");
  const [provinces, setProvinces] = useState([]);
  const [wards, setWards] = useState([]);

  const [selectedProvince, setSelectedProvince] = useState("");
  const [selectedWard, setSelectedWard] = useState("");
  const [summary, setSummary] = useState({
    subtotal: 0,
    discount: 0,
    shippingFee: 0,
    total: 0,
  });

  const [form, setForm] = useState({
    email: "",
    name: "",
    phone: "",
    address: "",
    province: "",
    district: "",
    ward: "",
    note: "",
  });
  const [formAddress, setFormAddress] = useState({
    province: "",
    delivery_address: "",
    delivery_note: "",
  });
  const [isAddAddress, setIsAddAddress] = useState(false);
  console.log(product, quantity);

  useEffect(() => {
    const fetchAddresses = async () => {
      try {
        if (!userId) return;
        const token = localStorage.getItem("accessToken");
        const res = await safeFetch(`/api/addresses/${userId}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }, 'GET /api/addresses/{userId}');

        const data = await res.json();
        setAddresses(data);
      } catch (error) {
        console.log("Lỗi fetch Addreses: ", error);
      }
    };
    if (userId) fetchAddresses();
  }, [userId]);

  useEffect(() => {
    const handleFetchCustomer = async () => {
      if (!userId) return;
      const token = localStorage.getItem("accessToken");
      const res = await safeFetch(`/api/customers/${userId}`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      }, 'GET /api/customers/{userId}');
      const text = await res.text();
      const customer = text ? JSON.parse(text) : {};
      setForm({
        name: customer.fullName || "",
        phone: customer.phoneNumber || "",
        email: customer.email || "",
      });
    };
    handleFetchCustomer();
  }, []);

  useEffect(() => {
    fetch("https://provinces.open-api.vn/api/v2/?depth=2")
      .then((res) => res.json())
      .then((data) => setProvinces(data));
  }, []);

  const handleProvinceChange = (provinceName) => {
    setSelectedProvince(provinceName);

    const province = provinces.find((p) => p.name == provinceName);

    setWards(province?.wards || []);
  };

  useEffect(() => {
    const stored = localStorage.getItem("cartItems");
    if (stored) {
      const parsed = JSON.parse(stored);
      setCartItems(parsed);
      setSummary(calculateSummary(parsed));
    }
  }, []);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });
  const handleChangeAddress = (e) =>
    setFormAddress({ ...formAddress, [e.target.name]: e.target.value });

  const handleSelectAddress = (index) => {
    if (index === "") return;

    const addr = addresses[index];

    setForm((prev) => ({
      ...prev,
      address: addr.delivery_address,
      province: addr.province,
      district: addr.city,
      ward: addr.ward || "",
      note: addr.delivery_note || "",
    }));
  };
  const handleConfirm = async () => {
    try {
      if (payment === "") {
        toast.warning("Vui lòng chọn phương thức thanh toán!!!");
      } else {
        const token = localStorage.getItem("accessToken");
        let resolvedUserId = userId;
        if (!resolvedUserId) {
          const meRes = await safeFetch(
            "/api/accounts/myinfor",
            {
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
              },
            },
            "GET /api/accounts/myinfor"
          );
          const meText = await meRes.text();
          const meData = meText ? JSON.parse(meText) : null;
          resolvedUserId = meData?.result?.id ? String(meData.result.id) : null;
          if (resolvedUserId) {
            localStorage.setItem("userId", resolvedUserId);
          }
        }

        if (!resolvedUserId) {
          toast.error("Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
          return;
        }

        const fullAddress = `${form.address}${
          form.ward ? ", " + form.ward : ""
        }, ${form.province}`;

        let requestBody;

        if (product) {
          requestBody = {
            receiverName: form.name,
            receiverPhone: form.phone,
            receiverEmail: form.email,
            receiverAddress: fullAddress,
            totalAmount: product.costPrice * quantity + 30000,
          };
        } else {
          requestBody = {
            receiverName: form.name,
            receiverPhone: form.phone,
            receiverEmail: form.email,
            receiverAddress: fullAddress,
            totalAmount: summary.total,
          };
        }

        const res = await safeFetch(
          "/api/customer-trading/create",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(requestBody),
          },
          'POST /api/customer-trading/create'
        );

        const data = await res.json();
        console.log(data);
        let orderBody;
        if (payment === "bank") {
          orderBody = {
            customerTradingId: data.id,
            note: form.note || "",
            account_id: Number(resolvedUserId),
            paymentMethod: "BANK_TRANSFER",
            items: product
              ? [
                  {
                    productId: String(product.id),
                    productName: product.name || "",
                    quantity: quantity,
                    unitPrice: product.costPrice,
                    size: String(product.sizeDetailId || ""),
                  },
                ]
              : selectedCartItems.map((item) => ({
                  productId: String(item.id),
                  productName: item.productName || "",
                  quantity: item.quantity,
                  unitPrice: item.priceAtTime,
                  size: String(item.sizeDetailId || ""),
                })),
          };
        } else {
          orderBody = {
            customerTradingId: data.id,
            note: form.note || "",
            account_id: Number(resolvedUserId),
            paymentMethod: "CASH",
            items: product
              ? [
                  {
                    productId: String(product.id),
                    productName: product.name || "",
                    quantity: quantity,
                    unitPrice: product.costPrice,
                    size: String(product.sizeDetailId || ""),
                  },
                ]
              : selectedCartItems.map((item) => ({
                  productId: String(item.id),
                  productName: item.productName || "",
                  quantity: item.quantity,
                  unitPrice: item.priceAtTime,
                  size: String(item.sizeDetailId || ""),
                })),
          };
        }

        const orderRes = await safeFetch("/api/orders/create", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(orderBody),
        }, 'POST /api/orders/create');

        const orderData = await orderRes.json();
        console.log("Order created:", orderData);
        if (orderData && orderData.id) {
          // subscribe to server-sent events for order updates
          try {
            const evtSource = new EventSource(`/api/orders/subscribe?userId=${resolvedUserId}`);
            evtSource.addEventListener("order.update", (e) => {
              try {
                const payload = JSON.parse(e.data);
                console.log("Order update SSE:", payload);
                toast(`Order ${payload.id} status: ${payload.status}`);
                if (payload.status === "PAID" || payload.status === "DELIVERED" || payload.status === "CANCELLED" || payload.status === "FAILED") {
                  evtSource.close();
                }
              } catch (err) {
                console.log("Invalid SSE payload", err);
              }
            });
            evtSource.onerror = (err) => {
              console.warn("SSE error", err);
              evtSource.close();
            };
          } catch (err) {
            console.warn("SSE unsupported", err);
          }
        }

        // determine if orderId is numeric (legacy monolith uses int ids)
        const orderId = orderData?.id;
        const isNumericId = typeof orderId === "number" || (/^\d+$/.test(String(orderId)));

        let detailsOk = true;
        if (product) {
          if (isNumericId) {
            const odRes = await safeFetch(`/api/order-details/create`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              productName: product.name,
              quantity: quantity,
              unitPrice: product.costPrice,
              totalPrice: product.costPrice * quantity,
              orderId: orderData.id,
              productId: product.id,
            }),
            }, 'POST /api/order-details/create');
            if (!odRes.ok) {
              detailsOk = false;
              const err = await odRes.text().catch(() => odRes.statusText);
              throw new Error(`Failed to create order detail: ${err}`);
            }
          } else {
            console.warn("Skipping order-details.create: orderId is non-numeric (saga flow)");
          }
        } else {
          if (isNumericId) {
            for (const item of selectedCartItems) {
              const odRes = await safeFetch(`/api/order-details/create`, {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                  productName: item.productName,
                  quantity: item.quantity,
                  unitPrice: item.priceAtTime,
                  totalPrice: item.subtotal,
                  orderId: orderData.id,
                  productId: item.id,
                }),
              }, 'POST /api/order-details/create');
              if (!odRes.ok) {
                detailsOk = false;
                const err = await odRes.text().catch(() => odRes.statusText);
                throw new Error(`Failed to create order detail: ${err}`);
              }
            }
            localStorage.removeItem("cartItems");
          } else {
            console.warn("Skipping order-details.create for cart: orderId is non-numeric (saga flow)");
            // still remove cart because saga flow will handle items server-side
            localStorage.removeItem("cartItems");
          }
        }
        // If we reach here and detailsOk is true, consider order successful
        if (detailsOk) {
          toast.success("Order successful!!");
        }

        if (payment === "bank") {
          if (isNumericId) {
            const invoiceRequest = {
              orderId: orderData.id,
              paymentMethod: "BANK_TRANSFER",
              paymentStatus: "UNPAID",
            };

            const invRes = await safeFetch("/api/invoices", {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
              },
              body: JSON.stringify(invoiceRequest),
            }, 'POST /api/invoices');

            const newInvoice = await invRes.json();
            sessionStorage.setItem(
              "paymentInfo",
              JSON.stringify({
                orderId: orderData.id,
                amount: summary.total,
                invoiceId: newInvoice.id,
                invoiceCode: newInvoice.invoiceCode,
              })
            );
            navigate(
              `/payment?orderId=${orderData.id}&amount=${summary.total}&invoiceId=${newInvoice.id}&invoiceCode=${newInvoice.invoiceCode}`
            );
          } else {
            sessionStorage.setItem(
              "paymentInfo",
              JSON.stringify({
                orderId: orderData.id,
                amount: summary.total,
                invoiceCode: orderData.id,
              })
            );
            navigate(`/payment?orderId=${orderData.id}&amount=${summary.total}`);
          }
        } else {
          navigate("/");
        }
      }
    } catch (error) {
      console.error("Error creating order:", error);
      toast.error("Failed to place order. Please try again.");
    }
  };
  const handleAddNewAddress = async () => {
    try {
      if (!userId) {
        toast.error("Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
        return;
      }

      const token = localStorage.getItem("accessToken");
      const finalDeliveryAddress = selectedWard
        ? `${formAddress.delivery_address}, ${selectedWard}`
        : `${formAddress.delivery_address}`;
      const requestBody = {
        accountId: userId,
        province: selectedProvince,
        delivery_address: finalDeliveryAddress,
        delivery_note: formAddress.delivery_note,
      };
      const res = await safeFetch(`/api/addresses/add`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(requestBody),
      }, 'POST /api/addresses/add');

      if (res.ok) {
        setFormAddress({
          city: "",
          province: "",
          delivery_address: "",
          delivery_note: "",
        });
        toast.success("Add address successfully!!");
        setIsAddAddress(false);
      }
      const resAddress = await safeFetch(
        `/api/addresses/${userId}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }, 'GET /api/addresses/{userId}'
      );
      const data = await resAddress.json();
      setAddresses(data);
    } catch (error) {
      console.error("Fail to add new address!!", error);
      toast.error("Fail to add new address!!");
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-10 grid grid-cols-1 lg:grid-cols-3 gap-10">
      <div className="lg:col-span-2 space-y-10">
        <div>
          <h1 className="text-3xl font-bold mb-5">Shipping Information</h1>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <input
              type="email"
              name="email"
              placeholder="Email"
              className="border p-3 rounded"
              onChange={handleChange}
              value={form.email}
            />
            <input
              type="text"
              name="name"
              placeholder="Full Name"
              className="border p-3 rounded"
              onChange={handleChange}
              value={form.name}
            />
            <input
              type="text"
              name="phone"
              placeholder="Phone Number"
              className="border p-3 rounded"
              onChange={handleChange}
              value={form.phone}
            />

            <div className="p-3 relative">
              {isAddAddress === false ? (
                <button
                  onClick={() => setIsAddAddress(true)}
                  className="absolute right-0 bottom-0 px-4 bg-black text-white py-2 rounded font-bold text-sm hover:bg-gray-800 transition"
                >
                  Add new address
                </button>
              ) : (
                <></>
              )}
            </div>
            {isAddAddress === false ? (
              <select
                name="address"
                className="border p-3 rounded md:col-span-2"
                onChange={(e) => handleSelectAddress(e.target.value)}
              >
                <option value="">-- Select saved address --</option>
                {addresses.map((addr, index) => (
                  <option key={index} value={index}>
                    {addr.delivery_address} ({addr.province})
                  </option>
                ))}
              </select>
            ) : (
              <div className="md:col-span-2 border p-5 rounded bg-gray-100 space-y-4">
                <h3 className="text-xl font-bold">Add New Address</h3>

                <input
                  type="text"
                  name="delivery_address"
                  placeholder="Delivery Address"
                  className="border p-3 rounded w-full"
                  onChange={handleChangeAddress}
                  value={formAddress.delivery_address}
                />

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <select
                    value={selectedProvince}
                    onChange={(e) => handleProvinceChange(e.target.value)}
                    className="border p-2 rounded"
                  >
                    <option value="">-- Select Province --</option>
                    {provinces.map((p) => (
                      <option key={p.code} value={p.name}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                  <select
                    className="border p-2 rounded"
                    disabled={!selectedProvince}
                    onChange={(e) => setSelectedWard(e.target.value)}
                  >
                    <option value="">-- Select Wards --</option>
                    {wards.map((w) => (
                      <option key={w.code} value={w.name}>
                        {w.name}
                      </option>
                    ))}
                  </select>
                </div>

                <textarea
                  name="delivery_note"
                  placeholder="Delivery note (optional)"
                  className="border p-3 rounded w-full"
                  rows="3"
                  onChange={handleChangeAddress}
                  value={formAddress.delivery_note}
                ></textarea>

                <div className="flex justify-between pt-2">
                  <button
                    onClick={() => setIsAddAddress(false)}
                    className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                  >
                    Cancel
                  </button>

                  <button
                    onClick={handleAddNewAddress}
                    className="px-4 py-2 bg-black text-white rounded hover:bg-gray-800"
                  >
                    Save Address
                  </button>
                </div>
              </div>
            )}

            <textarea
              name="note"
              placeholder="Notes (optional)"
              className="border p-3 rounded md:col-span-2"
              onChange={handleChange}
            ></textarea>
          </div>
        </div>

        <div>
          <h2 className="text-2xl font-bold mb-3">Delivery Method</h2>
          <div className="border p-4 rounded flex justify-between items-center">
            <span>Standard (3–5 business days)</span>
            <span className="font-semibold">{formatVND(30000)}</span>
          </div>
        </div>

        <div>
          <h2 className="text-2xl font-bold mb-3">Payment Method</h2>

          <div className="space-y-3">
            <label className="flex items-center gap-3 border p-3 rounded cursor-pointer">
              <input
                type="radio"
                name="payment"
                value="cash"
                onChange={(e) => setPayment(e.target.value)}
              />
              <span>Cash on Delivery (COD)</span>
            </label>

            <label className="flex items-center gap-3 border p-3 rounded cursor-pointer">
              <input
                type="radio"
                name="payment"
                value="bank"
                onChange={(e) => setPayment(e.target.value)}
              />
              <span>Bank Transfer</span>
            </label>
          </div>
        </div>
      </div>

      <div className="border-t-4 border-red-500 p-6 rounded-lg bg-gray-50 shadow-md h-fit">
        <h2 className="text-3xl font-bold mb-6 text-red-500">Order Summary</h2>

        <div className="space-y-4 text-lg">
          <div className="flex justify-between">
            <span>Subtotal:</span>
            <span className="font-semibold">{formatVND(summary.subtotal)}</span>
          </div>

          {product ? (
            <div className="flex justify-between">
              <span>Shipping fee:</span>
              <span>{formatVND(30000)}</span>
            </div>
          ) : (
            <div className="flex justify-between">
              <span>Shipping fee:</span>
              <span>{formatVND(summary.shippingFee)}</span>
            </div>
          )}

          <div className="flex justify-between">
            <span>Discount:</span>
            <span>{formatVND(summary.discount)}</span>
          </div>
        </div>

        {product ? (
          <div className="flex justify-between text-xl font-bold border-t pt-5 mt-5">
            <span>Total:</span>
            <span className="text-red-500">
              {formatVND(product.costPrice * quantity + 30000)}
            </span>
          </div>
        ) : (
          <div className="flex justify-between text-xl font-bold border-t pt-5 mt-5">
            <span>Total:</span>
            <span className="text-red-500">{formatVND(summary.total)}</span>
          </div>
        )}

        <button
          onClick={handleConfirm}
          className="w-full mt-8 bg-black text-white py-3 rounded font-bold text-lg hover:bg-gray-800 transition"
        >
          Confirm Order
        </button>
      </div>
    </div>
  );
};

export default Checkout;
