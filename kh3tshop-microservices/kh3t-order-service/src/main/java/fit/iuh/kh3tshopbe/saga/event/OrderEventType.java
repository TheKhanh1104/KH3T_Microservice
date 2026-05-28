package fit.iuh.kh3tshopbe.saga.event;

public enum OrderEventType {
    ORDER_CREATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PAYMENT_REQUESTED,
    PAYMENT_CHARGED,
    PAYMENT_FAILED,
    ORDER_SHIPPED,
    ORDER_CANCELLED
}