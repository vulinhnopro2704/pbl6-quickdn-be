import { isAxiosError } from 'axios';
import { goongService } from '../services/goong.services';
import { Vehicle } from '../types/direction';
import { createErrorResponse } from '../types/errorResponse';
import { Request, Response } from 'express';

const getDistanceMatrix = async (req: Request, res: Response) => {
  try {
    const { origins, destinations, vehicle } = req.query;
    const result = await goongService.getDistanceMatrix({
      origins: origins as string,
      destinations: destinations as string,
      vehicle: (vehicle || 'bike') as Vehicle,
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
              'Service Unavailable',
              'No response received from Goong API',
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

export { getDistanceMatrix };
