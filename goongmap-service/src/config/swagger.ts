import swaggerJsdoc from 'swagger-jsdoc';

const swaggerDefinition = {
  openapi: '3.0.0',
  info: {
    title: 'Map Service API',
    version: '1.0.0',
    description: 'API documentation for Map Service',
    contact: {
      name: 'API Support',
    },
  },
  servers: [
    {
      url: 'http://localhost:3000',
      description: 'Development server',
    },
    {
      url: 'https://quickdn.undo.it',
      description: 'Production server',
    },
  ],
};

const options: swaggerJsdoc.Options = {
  swaggerDefinition,
  apis:
    process.env.NODE_ENV === 'production'
      ? ['./dist/routes/*.js', './dist/app.js']
      : ['./src/routes/*.ts', './src/app.ts'],
};

export const swaggerSpec = swaggerJsdoc(options);
