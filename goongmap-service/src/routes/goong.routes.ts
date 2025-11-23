import { Router } from 'express';
import { getDirections } from '../controllers/direction.controllers';
import { getDistanceMatrix } from '../controllers/distanceMatrix.controllers';
import { getTripInstructions } from '../controllers/trip.controllers';
import { validate } from '../config/validate.middleware';
import {
  directionSchema,
  distanceMatrixSchema,
  tripSchema,
} from '../validators/map.validators';

const router = Router();

/**
 * @swagger
 * components:
 *   schemas:
 *     ErrorResponse:
 *       type: object
 *       properties:
 *         error:
 *           type: string
 *           description: Error type
 *         message:
 *           type: string
 *           description: Detailed error message
 *         status:
 *           type: integer
 *           description: HTTP status code
 *         timestamp:
 *           type: string
 *           format: date-time
 *         path:
 *           type: string
 *         validationErrors:
 *           type: object
 *           additionalProperties:
 *             type: string
 *     Location:
 *       type: object
 *       properties:
 *         lat:
 *           type: number
 *         lng:
 *           type: number
 *     Distance:
 *       type: object
 *       properties:
 *         text:
 *           type: string
 *         value:
 *           type: number
 *     Duration:
 *       type: object
 *       properties:
 *         text:
 *           type: string
 *         value:
 *           type: number
 *     Step:
 *       type: object
 *       properties:
 *         distance:
 *           $ref: '#/components/schemas/Distance'
 *         duration:
 *           $ref: '#/components/schemas/Duration'
 *         endLocation:
 *           $ref: '#/components/schemas/Location'
 *         startLocation:
 *           $ref: '#/components/schemas/Location'
 *         htmlInstructions:
 *           type: string
 *         maneuver:
 *           type: string
 *         polyline:
 *           type: object
 *           properties:
 *             points:
 *               type: string
 *         travelMode:
 *           type: string
 *     Leg:
 *       type: object
 *       properties:
 *         distance:
 *           $ref: '#/components/schemas/Distance'
 *         duration:
 *           $ref: '#/components/schemas/Duration'
 *         endAddress:
 *           type: string
 *         endLocation:
 *           $ref: '#/components/schemas/Location'
 *         startAddress:
 *           type: string
 *         startLocation:
 *           $ref: '#/components/schemas/Location'
 *         steps:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/Step'
 *     Route:
 *       type: object
 *       properties:
 *         bounds:
 *           type: object
 *           properties:
 *             northeast:
 *               $ref: '#/components/schemas/Location'
 *             southwest:
 *               $ref: '#/components/schemas/Location'
 *         legs:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/Leg'
 *         overviewPolyline:
 *           type: object
 *           properties:
 *             points:
 *               type: string
 *         summary:
 *           type: string
 *         warnings:
 *           type: array
 *           items:
 *             type: string
 *         waypointOrder:
 *           type: array
 *           items:
 *             type: integer
 *     GeocodedWaypoint:
 *       type: object
 *       properties:
 *         geocoderStatus:
 *           type: string
 *         placeId:
 *           type: string
 *     GoongDirectionResponse:
 *       type: object
 *       properties:
 *         geocodedWaypoints:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/GeocodedWaypoint'
 *         routes:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/Route'
 *     DistanceElement:
 *       type: object
 *       properties:
 *         distance:
 *           $ref: '#/components/schemas/Distance'
 *         duration:
 *           $ref: '#/components/schemas/Duration'
 *         status:
 *           type: string
 *     DistanceRow:
 *       type: object
 *       properties:
 *         elements:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/DistanceElement'
 *     DistanceMatrixResponse:
 *       type: object
 *       properties:
 *         rows:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/DistanceRow'
 *     TripLeg:
 *       type: object
 *       properties:
 *         distance:
 *           type: string
 *         duration:
 *           type: string
 *         steps:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/Step'
 *         summary:
 *           type: string
 *         weight:
 *           type: string
 *     TripRow:
 *       type: object
 *       properties:
 *         distance:
 *           type: string
 *         duration:
 *           type: string
 *         legs:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/TripLeg'
 *         weight:
 *           type: string
 *         weightName:
 *           type: string
 *     WayPointRow:
 *       type: object
 *       properties:
 *         distance:
 *           type: string
 *         location:
 *           type: array
 *           items:
 *             type: string
 *         placeId:
 *           type: string
 *         tripIndex:
 *           type: integer
 *         waypointIndex:
 *           type: integer
 *     TripResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *         trips:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/TripRow'
 *         waypoints:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/WayPointRow'
 */

/**
 * @swagger
 * /api/map/directions:
 *   get:
 *     summary: Get directions between two points
 *     tags: [Map]
 *     parameters:
 *       - in: query
 *         name: origin
 *         schema:
 *           type: string
 *         required: true
 *         description: Starting point (latitude,longitude)
 *       - in: query
 *         name: destination
 *         schema:
 *           type: string
 *         required: true
 *         description: Destination point (latitude,longitude)
 *       - in: query
 *         name: vehicle
 *         schema:
 *           type: string
 *           enum: [bike, car, taxi, truck, hd]
 *           default: bike
 *         description: Type of vehicle
 *       - in: query
 *         name: alternatives
 *         schema:
 *           type: boolean
 *           default: false
 *         description: Whether to return alternative routes
 *     responses:
 *       200:
 *         description: Successful response
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/GoongDirectionResponse'
 *       400:
 *         description: Bad Request
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       500:
 *         description: Internal Server Error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get('/directions', validate(directionSchema), getDirections);

/**
 * @swagger
 * /api/map/distance-matrix:
 *   get:
 *     summary: Get distance matrix between origins and destinations
 *     tags: [Map]
 *     parameters:
 *       - in: query
 *         name: origins
 *         schema:
 *           type: string
 *           pattern: ^-?\d+(\.\d+)?,-?\d+(\.\d+)?(\|(-?\d+(\.\d+)?,-?\d+(\.\d+)?))*$
 *         required: true
 *         description: "Starting points (latitude,longitude separated by |). Example: 21.028511,105.804817|21.028511,105.804817"
 *       - in: query
 *         name: destinations
 *         schema:
 *           type: string
 *           pattern: ^-?\d+(\.\d+)?,-?\d+(\.\d+)?(\|(-?\d+(\.\d+)?,-?\d+(\.\d+)?))*$
 *         required: true
 *         description: "Destination points (latitude,longitude separated by |). Example: 21.028511,105.804817|21.028511,105.804817"
 *       - in: query
 *         name: vehicle
 *         schema:
 *           type: string
 *           enum: [bike, car, taxi, truck, hd]
 *           default: bike
 *         description: Type of vehicle
 *     responses:
 *       200:
 *         description: Successful response
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DistanceMatrixResponse'
 *       400:
 *         description: Bad Request
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       500:
 *         description: Internal Server Error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get(
  '/distance-matrix',
  validate(distanceMatrixSchema),
  getDistanceMatrix,
);

/**
 * @swagger
 * /api/map/trip:
 *   get:
 *     summary: Get trip instructions (TSP)
 *     description: Get trip instructions with optimal waypoint ordering. At least one of destination or waypoints must be provided.
 *     tags: [Map]
 *     parameters:
 *       - in: query
 *         name: origin
 *         schema:
 *           type: string
 *           pattern: ^-?\d+(\.\d+)?,-?\d+(\.\d+)?$
 *         required: true
 *         description: "Starting point (latitude,longitude). Example: 21.028511,105.804817"
 *       - in: query
 *         name: destination
 *         schema:
 *           type: string
 *           pattern: ^-?\d+(\.\d+)?,-?\d+(\.\d+)?$
 *         required: false
 *         description: "Destination point (latitude,longitude). Example: 21.028511,105.804817. At least one of destination or waypoints is required."
 *       - in: query
 *         name: waypoints
 *         schema:
 *           type: string
 *           pattern: ^-?\d+(\.\d+)?,-?\d+(\.\d+)?(;(-?\d+(\.\d+)?,-?\d+(\.\d+)?))*$
 *         required: false
 *         description: "Intermediate waypoints (latitude,longitude separated by ;). Example: 21.028511,105.804817;21.028511,105.804817. At least one of destination or waypoints is required."
 *       - in: query
 *         name: roundtrip
 *         schema:
 *           type: boolean
 *           default: false
 *         description: Whether the trip is a round trip
 *       - in: query
 *         name: vehicle
 *         schema:
 *           type: string
 *           enum: [bike, car, taxi, truck, hd]
 *           default: bike
 *         description: Type of vehicle
 *     responses:
 *       200:
 *         description: Successful response
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TripResponse'
 *       400:
 *         description: Bad Request
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       500:
 *         description: Internal Server Error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get('/trip', validate(tripSchema), getTripInstructions);
export default router;
