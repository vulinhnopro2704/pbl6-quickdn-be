import { Request, Response, NextFunction } from 'express';
import { ZodObject, ZodError } from 'zod';
import { createErrorResponse } from '../types/errorResponse';

export const validate =
  (schema: ZodObject) =>
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      await schema.parseAsync({
        body: req.body,
        query: req.query,
        params: req.params,
      });
      return next();
    } catch (error) {
      if (error instanceof ZodError) {
        const validationErrors = error.issues.reduce<Record<string, string>>(
          (acc, issue) => {
            acc[issue.path.join('.')] = issue.message;
            return acc;
          },
          {},
        );

        return res
          .status(400)
          .json(
            createErrorResponse(
              'Validation Error',
              'Dữ liệu đầu vào không hợp lệ',
              400,
              req.path,
              validationErrors,
            ),
          );
      }
      return next(error);
    }
  };
