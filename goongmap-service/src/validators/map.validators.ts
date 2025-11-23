import { z } from 'zod';

const latLngRegex = /^-?\d+(\.\d+)?,-?\d+(\.\d+)?$/;

export const directionSchema = z.object({
  query: z.object({
    origin: z
      .string({ message: 'Origin là bắt buộc' })
      .regex(latLngRegex, "Origin phải có dạng 'lat,lng'"),

    destination: z
      .string({ message: 'Destination là bắt buộc' })
      .regex(latLngRegex, "Destination phải có dạng 'lat,lng'"),

    vehicle: z
      .enum(['car', 'bike', 'taxi', 'truck', 'hd'])
      .optional()
      .default('bike'),

    alternatives: z
      .enum(['true', 'false'])
      .optional()
      .transform((val) => val === 'true'),
  }),
});

export const distanceMatrixSchema = z.object({
  query: z.object({
    origins: z
      .string({ message: 'Origins là bắt buộc' })
      .refine(
        (val) =>
          val.split('|').every((coord) => latLngRegex.test(coord.trim())),
        {
          message:
            "Origins phải là danh sách tọa độ 'lat,lng' phân cách bởi '|'",
        },
      ),

    destinations: z
      .string({ message: 'Destinations là bắt buộc' })
      .refine(
        (val) =>
          val.split('|').every((coord) => latLngRegex.test(coord.trim())),
        {
          message:
            "Destinations phải là danh sách tọa độ 'lat,lng' phân cách bởi '|'",
        },
      ),

    vehicle: z
      .enum(['car', 'bike', 'taxi', 'truck', 'hd'])
      .optional()
      .default('bike'),
  }),
});

export const tripSchema = z.object({
  query: z
    .object({
      origin: z
        .string({ message: 'Origin là bắt buộc' })
        .regex(latLngRegex, "Origin phải có dạng 'lat,lng'"),

      destination: z
        .string({ message: 'Destination không bắt buộc' })
        .regex(latLngRegex, "Destination phải có dạng 'lat,lng'")
        .optional(),

      waypoints: z
        .string({ message: 'Waypoints không bắt buộc' })
        .refine(
          (val) =>
            val.split(';').every((coord) => latLngRegex.test(coord.trim())),
          {
            message:
              "Waypoints phải là danh sách tọa độ 'lat,lng' phân cách bởi ';'",
          },
        )
        .optional(),

      roundtrip: z
        .enum(['true', 'false'])
        .optional()
        .transform((val) => val === 'true'),

      vehicle: z
        .enum(['car', 'bike', 'taxi', 'truck', 'hd'])
        .optional()
        .default('bike'),
    })
    .refine((data) => data.destination || data.waypoints, {
      message: 'Phải cung cấp ít nhất một trong destination hoặc waypoints',
      path: ['destination'],
    }),
});
