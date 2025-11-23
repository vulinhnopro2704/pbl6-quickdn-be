import httpClient from '../config/httpClient';
import { GoongDirectionResponse } from '../types/direction';
import { DistanceMatrixResponse } from '../types/distanceMatrix';
import {
  DirectionRequest,
  DistanceMatrixRequest,
  TripRequest,
} from '../types/request';
import { TripResponse } from '../types/trip';

export const goongService = {
  getDirections: async (
    params: DirectionRequest,
  ): Promise<GoongDirectionResponse> => {
    const {
      origin,
      destination,
      vehicle = 'bike',
      alternatives = false,
    } = params;

    const response = await httpClient.get<GoongDirectionResponse>(
      '/direction',
      {
        params: {
          origin,
          destination,
          vehicle,
          alternatives,
        },
      },
    );
    return response.data;
  },

  getDistanceMatrix: async (
    params: DistanceMatrixRequest,
  ): Promise<DistanceMatrixResponse> => {
    const { origins, destinations, vehicle = 'bike' } = params;
    const response = await httpClient.get<DistanceMatrixResponse>(
      '/distancematrix',
      {
        params: {
          origins,
          destinations,
          vehicle,
        },
      },
    );
    return response.data;
  },

  getTripInstructions: async (params: TripRequest): Promise<TripResponse> => {
    const {
      origin,
      destination,
      vehicle = 'bike',
      waypoints,
      roundtrip = false,
    } = params;
    const response = await httpClient.get<TripResponse>('/trip', {
      params: {
        origin,
        destination,
        vehicle,
        waypoints,
        roundtrip,
      },
    });
    return response.data;
  },
};
