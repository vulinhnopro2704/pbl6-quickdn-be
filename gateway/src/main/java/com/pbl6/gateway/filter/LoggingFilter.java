package com.pbl6.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        boolean shouldLog = !uri.contains("prometheus") && !uri.contains("grafana") && !uri.contains("swagger");

        if (!shouldLog) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap request and response for logging
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        String requestId = getOrGenerateRequestId(httpRequest);
        String vietnamTime = ZonedDateTime.now(VIETNAM_ZONE).format(FORMATTER);
        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logIncomingRequest(requestWrapper, requestId);

            // Add request ID to response header
            responseWrapper.setHeader(REQUEST_ID_HEADER, requestId);

            // Process the request
            chain.doFilter(requestWrapper, responseWrapper);

            // Log outgoing response
            long duration = System.currentTimeMillis() - startTime;
            logOutgoingResponse(requestWrapper, responseWrapper, requestId, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logError(requestWrapper, requestId, duration, e);
            throw e;
        } finally {
            // Copy cached response to original response
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return (requestId != null && !requestId.isEmpty()) 
            ? requestId 
            : UUID.randomUUID().toString();
    }

    private void logIncomingRequest(HttpServletRequest request, String requestId) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        String userAgent = request.getHeader("User-Agent");
        String targetService = determineTargetService(uri);

        log.info("╔═══════════════════════════════════════════════════════════════");
        log.info("║ [GATEWAY] INCOMING REQUEST");
        log.info("╠═══════════════════════════════════════════════════════════════");
        log.info("║ Request ID     : {}", requestId);
        log.info("║ Client IP      : {}", clientIp);
        log.info("║ Method         : {}", method);
        log.info("║ Path           : {}", fullUrl);
        log.info("║ User-Agent     : {}", userAgent);
        log.info("║ Forwarding to  : {} Service", targetService);
        log.info("╚═══════════════════════════════════════════════════════════════");
    }

    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response, 
                                     String requestId, long duration) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String targetService = determineTargetService(uri);
        String statusEmoji = status < 400 ? "✓" : "✗";
        String logLevel = status < 400 ? "SUCCESS" : "ERROR";

        log.info("╔═══════════════════════════════════════════════════════════════");
        log.info("║ [GATEWAY] OUTGOING RESPONSE - {}", logLevel);
        log.info("╠═══════════════════════════════════════════════════════════════");
        log.info("║ Request ID     : {}", requestId);
        log.info("║ Method         : {}", method);
        log.info("║ Path           : {}", uri);
        log.info("║ Target Service : {}", targetService);
        log.info("║ Status         : {} {} {}", status, getStatusText(status), statusEmoji);
        log.info("║ Duration       : {} ms", duration);
        log.info("╚═══════════════════════════════════════════════════════════════");
    }

    private void logError(HttpServletRequest request, String requestId, long duration, Exception e) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.error("╔═══════════════════════════════════════════════════════════════");
        log.error("║ [GATEWAY] REQUEST FAILED");
        log.error("╠═══════════════════════════════════════════════════════════════");
        log.error("║ Request ID     : {}", requestId);
        log.error("║ Method         : {}", method);
        log.error("║ Path           : {}", uri);
        log.error("║ Duration       : {} ms", duration);
        log.error("║ Error          : {}", e.getMessage());
        log.error("╚═══════════════════════════════════════════════════════════════");
    }

    private String determineTargetService(String uri) {
        if (uri.startsWith("/api/auth")) return "AUTH";
        if (uri.startsWith("/api/order")) return "ORDER";
        if (uri.startsWith("/api/payment")) return "PAYMENT";
        if (uri.startsWith("/api/map")) return "GOONGMAP";
        if (uri.startsWith("/api/ai")) return "AI";
        if (uri.startsWith("/api/file")) return "FILE";
        return "UNKNOWN";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getStatusText(int status) {
        return switch (status / 100) {
            case 2 -> "OK";
            case 3 -> "REDIRECT";
            case 4 -> "CLIENT_ERROR";
            case 5 -> "SERVER_ERROR";
            default -> "UNKNOWN";
        };
    }
}
