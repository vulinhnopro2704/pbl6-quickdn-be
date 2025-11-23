import { Vehicle } from './direction';

export interface BaseRequest {
  origin: string;
  destination: string;
  vehicle?: Vehicle;
}
export interface DirectionRequest extends BaseRequest {
  alternatives?: boolean;
}

export interface DistanceMatrixRequest {
  origins: string;
  destinations: string;
  vehicle?: Vehicle;
}

export interface TripRequest extends BaseRequest {
  waypoints: string;
  roundtrip?: boolean;
}
