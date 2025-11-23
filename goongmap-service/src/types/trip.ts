import { Step } from './direction';

export interface TripParams {
  origin: string;
  destination: string;
  waypoints: string;
  vehicle?: string;
  roundtrip?: boolean;
}

export interface TripLeg {
  distance: string;
  duration: string;
  steps: Step[];
  summary: string;
  weight: string;
}

export interface TripRow {
  distance: string;
  duration: string;
  legs: TripLeg[];
  weight: string;
  weightName: string;
}

export interface WayPointRow {
  distance: string;
  location: [string, string];
  placeId: string;
  tripIndex: number;
  waypointIndex: number;
}

export interface TripResponse {
  code: string;
  trips: TripRow[];
  waypoints: WayPointRow[];
}
