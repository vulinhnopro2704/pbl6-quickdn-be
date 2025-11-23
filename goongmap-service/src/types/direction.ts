export type Vehicle = 'bike' | 'car' | 'taxi' | 'truck' | 'hd';


export interface Location {
  lat: number;
  lng: number;
}

export interface Bounds {
  northeast?: Location;
  southwest?: Location;
}

export interface Distance {
  text: string;
  value: number;
}

export interface Duration {
  text: string;
  value: number;
}

export interface Polyline {
  points: string;
}

export interface Step {
  distance: Distance;
  duration: Duration;
  endLocation: Location;
  startLocation: Location;
  htmlInstructions: string;
  maneuver: string;
  polyline: Polyline;
  travelMode: string;
}

export interface Leg {
  distance: Distance;
  duration: Duration;
  endAddress: string;
  endLocation: Location;
  startAddress: string;
  startLocation: Location;
  steps: Step[];
}

export interface Route {
  bounds: Bounds;
  legs: Leg[];
  overviewPolyline: Polyline;
  summary: string;
  warnings: string[];
  waypointOrder: number[];
}

export interface GeocodedWaypoint {
  geocoderStatus: string;
  placeId: string;
}

export interface GoongDirectionResponse {
  geocodedWaypoints: GeocodedWaypoint[];
  routes: Route[];
}
