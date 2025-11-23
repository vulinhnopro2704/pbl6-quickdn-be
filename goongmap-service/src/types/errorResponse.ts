export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
  timestamp?: string;
  path?: string;
  validationErrors?: Record<string, string>;
}

export function createErrorResponse(
  error: string,
  message: string,
  status: number,
  path?: string,
  validationErrors?: Record<string, string>,
): ErrorResponse {
  return {
    error,
    message,
    status,
    timestamp: new Date().toISOString(),
    ...(path && { path }),
    ...(validationErrors && { validationErrors }),
  };
}
