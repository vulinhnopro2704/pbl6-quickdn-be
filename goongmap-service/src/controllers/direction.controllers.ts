import { Request, Response } from 'express';
import { createErrorResponse } from '../types/errorResponse';
import { goongService } from '../services/goong.services';
import { Vehicle } from '../types/direction';
import { isAxiosError } from 'axios';

const getDirections = async (req: Request, res: Response) => {
  try {
    const { origin, destination, vehicle, alternatives } = req.query;
    const result = await goongService.getDirections({
      origin: origin as string,
      destination: destination as string,
      vehicle: (vehicle || 'bike') as Vehicle,
      alternatives: alternatives ? alternatives === 'true' : false,
    });
    return res.status(200).json(result);
  } catch (error) {
    if (isAxiosError(error)) {
      if (error.response) {
        const { status, data } = error.response;
        return res
          .status(status)
          .json(
            createErrorResponse(
              'Error from Goong API',
              data.message ||
                'An error occurred while fetching directions from Goong API',
              status,
              req.path,
            ),
          );
      } else if (error.request) {
        return res
          .status(503)
          .json(
            createErrorResponse(
              'Service  Unavailable',
              'No response received from  Goong API',
              503,
              req.path,
            ),
          );
      }
    }
    return res
      .status(500)
      .json(
        createErrorResponse(
          'Internal Server Error',
          error instanceof Error ? error.message : 'Unknown error',
          500,
          req.path,
        ),
      );
  }
};

export { getDirections };
