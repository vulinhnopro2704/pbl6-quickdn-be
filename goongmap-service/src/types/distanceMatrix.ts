
export interface Distance {
  text: string;
  value: number;
}

export interface Duration extends Distance {}
export interface DistanceElement {
  distance: Distance;
  duration: Duration;
  status: string;
}
export interface DistanceRow {
  elements: DistanceElement[];
}
export interface DistanceMatrixResponse {
  rows: DistanceRow[];
}
