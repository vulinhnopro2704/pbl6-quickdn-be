// src/app.ts
import 'dotenv/config';
import express, { Application, Request, Response } from 'express';
import swaggerUi from 'swagger-ui-express';
import { swaggerSpec } from './config/swagger';
import router from './routes/goong.routes';

const app: Application = express();
const port: number = parseInt(process.env.PORT || '3000', 10);

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Swagger UI
app.use('/api/map/docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

/**
 * @swagger
 * /:
 *   get:
 *     summary: Welcome endpoint
 *     description: Returns a welcome message
 *     responses:
 *       200:
 *         description: Successful response
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *               example: Hello from Express with TypeScript!
 */
app.get('/', (req: Request, res: Response) => {
  res.send('Hello from Express with TypeScript!');
});

app.use('/api/map', router);

app.listen(port, () => {
  console.log(`Server running`);
});
