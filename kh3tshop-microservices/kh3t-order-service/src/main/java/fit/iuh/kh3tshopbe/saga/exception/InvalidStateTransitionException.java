package fit.iuh.kh3tshopbe.saga.exception;

import fit.iuh.kh3tshopbe.saga.domain.OrderStatus;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(OrderStatus currentStatus, OrderStatus newStatus) {
        super("Invalid transition from " + currentStatus + " to " + newStatus);
    }
}