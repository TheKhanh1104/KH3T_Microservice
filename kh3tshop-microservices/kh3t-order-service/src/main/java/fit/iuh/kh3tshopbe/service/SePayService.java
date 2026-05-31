package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.configuration.SePayConfig;
import fit.iuh.kh3tshopbe.dto.request.SePayRequest;
import fit.iuh.kh3tshopbe.dto.response.SePayResponse;
import fit.iuh.kh3tshopbe.entities.Invoice;
import fit.iuh.kh3tshopbe.enums.StatusPayment;
import fit.iuh.kh3tshopbe.repository.InvoiceRepository;
import fit.iuh.kh3tshopbe.saga.service.SagaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SePayService {

    @Autowired
    private SePayConfig sePayConfig;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SagaOrderService sagaOrderService;

    public SePayResponse handleCallback(SePayRequest callbackRequest, String authorizationHeader) {
        if (!sePayConfig.getApiKey().equals(authorizationHeader)) {
            return new SePayResponse(false, "Unauthorized callback " + authorizationHeader);
        }
        if (!"in".equalsIgnoreCase(callbackRequest.getTransferType())) {
            return new SePayResponse(true, "Transaction type not supported");
        }
        System.out.println("SePay callback received: code=" + callbackRequest.getCode()
                + ", content=" + callbackRequest.getContent()
                + ", description=" + callbackRequest.getDescription()
                + ", referenceCode=" + callbackRequest.getReferenceCode());

        String invoiceCode = callbackRequest.getCode();

        if (invoiceCode == null || invoiceCode.isEmpty()) {
            String content = callbackRequest.getContent();

            if (content != null && !content.isEmpty()) {
                // Bắt mã dạng cũ: INV20251206002
                Pattern pattern = Pattern.compile("(INV\\d{11,})");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    invoiceCode = matcher.group(1);
                }
            }
        }
        if (invoiceCode != null && invoiceCode.matches("INV\\d{11,}")) {
            String datePart = invoiceCode.substring(3, 11);
            String indexPart = invoiceCode.substring(11);
            invoiceCode = "INV-" + datePart + "-" + indexPart;
        }
        Invoice invoice = invoiceRepository.findByInvoiceCode(invoiceCode);
        if (invoice == null) {
            UUID sagaOrderId = extractSagaOrderId(callbackRequest);
            if (sagaOrderId != null) {
                sagaOrderService.onPaymentConfirmed(sagaOrderId);
                return new SePayResponse(true, "Saga order confirmed successfully");
            }
            return new SePayResponse(true, "Invoice not found");
        }
        if (callbackRequest.getTransferAmount() < invoice.getTotalAmount()) {
            return new SePayResponse(true, "Payment amount insufficient");
        }
        invoice.setPaymentStatus(StatusPayment.PAID);
        invoiceRepository.save(invoice);

        return new SePayResponse(true, "Payment processed successfully");
    }

    private UUID extractSagaOrderId(SePayRequest callbackRequest) {
        String[] candidates = {
                callbackRequest.getCode(),
                callbackRequest.getContent(),
            callbackRequest.getDescription(),
            callbackRequest.getReferenceCode()
        };

        for (String candidate : candidates) {
            UUID parsed = parseUuid(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        // If no UUID found, try to detect a 32-hex payment token and look up the saga order by token
        for (String candidate : candidates) {
            if (candidate == null) continue;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("[0-9a-fA-F]{32}").matcher(candidate);
            if (m.find()) {
                String token = m.group();
                java.util.UUID id = sagaOrderService.findOrderIdByPaymentToken(token);
                if (id != null) return id;
            }
            // Also try REF-... or simple reference code matching
            String ref = callbackRequest.getReferenceCode();
            if (ref != null && !ref.isBlank()) {
                java.util.UUID id = sagaOrderService.findOrderIdByPaymentToken(ref.trim());
                if (id != null) return id;
            }
        }
        return null;
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        // Some providers may send UUID without dashes (32 hex chars) embedded in text. Try to detect and parse.
        Pattern compactPattern = Pattern.compile("[0-9a-fA-F]{32}");
        Matcher compactMatcher = compactPattern.matcher(value);
        if (compactMatcher.find()) {
            String v = compactMatcher.group();
            String dashed = v.substring(0, 8) + "-" + v.substring(8, 12) + "-" + v.substring(12, 16) + "-" + v.substring(16, 20) + "-" + v.substring(20);
            try {
                return UUID.fromString(dashed);
            } catch (IllegalArgumentException ignored) {
                // fallthrough to attempt normal parse
            }
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
