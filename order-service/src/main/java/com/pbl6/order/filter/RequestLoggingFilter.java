package com.pbl6.order.filter;

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
public class RequestLoggingFilter implements Filter {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  private static final String REQUEST_ID_HEADER = "X-Request-ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

    String requestId = getOrGenerateRequestId(httpRequest);
    String vietnamTime = ZonedDateTime.now(VIETNAM_ZONE).format(FORMATTER);
    long startTime = System.currentTimeMillis();

    try {
      // Log incoming request
      logIncomingRequest(requestWrapper, requestId, vietnamTime);

      // Add request ID to response
      responseWrapper.setHeader(REQUEST_ID_HEADER, requestId);

      // Process request
      chain.doFilter(requestWrapper, responseWrapper);

      // Log outgoing response
      long duration = System.currentTimeMillis() - startTime;
      logOutgoingResponse(requestWrapper, responseWrapper, requestId, duration);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      logError(requestWrapper, requestId, duration, e);
      throw e;
    } finally {
      responseWrapper.copyBodyToResponse();
    }
  }

  private String getOrGenerateRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    return (requestId != null && !requestId.isEmpty()) ? requestId : UUID.randomUUID().toString();
  }

  private void logIncomingRequest(
      HttpServletRequest request, String requestId, String vietnamTime) {
    String clientIp = getClientIp(request);
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullUrl = queryString != null ? uri + "?" + queryString : uri;
    String contentType = request.getContentType();
    String origin = request.getHeader("X-Forwarded-For") != null ? "Gateway" : "Direct";

    log.info("┌─────────────────────────────────────────────────────────────");
    log.info("│ [ORDER-SERVICE] REQUEST RECEIVED");
    log.info("├─────────────────────────────────────────────────────────────");
    log.info("│ Request ID     : {}", requestId);
    log.info("│ Time (VN)      : {}", vietnamTime);
    log.info("│ Origin         : {}", origin);
    log.info("│ Client IP      : {}", clientIp);
    log.info("│ Method         : {}", method);
    log.info("│ Path           : {}", fullUrl);
    log.info("│ Content-Type   : {}", contentType != null ? contentType : "N/A");
    log.info("└─────────────────────────────────────────────────────────────");
  }

  private void logOutgoingResponse(
      HttpServletRequest request, HttpServletResponse response, String requestId, long duration) {
    int status = response.getStatus();
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String vietnamTime = ZonedDateTime.now(VIETNAM_ZONE).format(FORMATTER);
    String statusEmoji = status < 400 ? "✓" : "✗";
    String logLevel = status < 400 ? "SUCCESS" : (status < 500 ? "CLIENT_ERROR" : "SERVER_ERROR");

    if (status < 400) {
      log.info("┌─────────────────────────────────────────────────────────────");
      log.info("│ [ORDER-SERVICE] RESPONSE SENT - {}", logLevel);
      log.info("├─────────────────────────────────────────────────────────────");
      log.info("│ Request ID     : {}", requestId);
      log.info("│ Time (VN)      : {}", vietnamTime);
      log.info("│ Method         : {}", method);
      log.info("│ Path           : {}", uri);
      log.info("│ Status         : {} {} {}", status, getStatusText(status), statusEmoji);
      log.info("│ Duration       : {} ms", duration);
      log.info("└─────────────────────────────────────────────────────────────");
    } else {
      log.warn("┌─────────────────────────────────────────────────────────────");
      log.warn("│ [ORDER-SERVICE] RESPONSE SENT - {}", logLevel);
      log.warn("├─────────────────────────────────────────────────────────────");
      log.warn("│ Request ID     : {}", requestId);
      log.warn("│ Time (VN)      : {}", vietnamTime);
      log.warn("│ Method         : {}", method);
      log.warn("│ Path           : {}", uri);
      log.warn("│ Status         : {} {} {}", status, getStatusText(status), statusEmoji);
      log.warn("│ Duration       : {} ms", duration);
      log.warn("└─────────────────────────────────────────────────────────────");
    }
  }

  private void logError(HttpServletRequest request, String requestId, long duration, Exception e) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String vietnamTime = ZonedDateTime.now(VIETNAM_ZONE).format(FORMATTER);

    log.error("┌─────────────────────────────────────────────────────────────");
    log.error("│ [ORDER-SERVICE] REQUEST FAILED");
    log.error("├─────────────────────────────────────────────────────────────");
    log.error("│ Request ID     : {}", requestId);
    log.error("│ Time (VN)      : {}", vietnamTime);
    log.error("│ Method         : {}", method);
    log.error("│ Path           : {}", uri);
    log.error("│ Duration       : {} ms", duration);
    log.error("│ Error Type     : {}", e.getClass().getSimpleName());
    log.error("│ Error Message  : {}", e.getMessage());
    log.error("└─────────────────────────────────────────────────────────────");
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
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
