import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { config } from './common/config';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.enableCors({
    origin: config.corsOrigin === '*' ? true : config.corsOrigin,
    credentials: config.corsOrigin !== '*'
  });

  app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }));

  const swagger = new DocumentBuilder()
    .setTitle('EMS Copilot Korea API')
    .setVersion('0.1.0')
    .addBearerAuth()
    .build();
  const doc = SwaggerModule.createDocument(app, swagger);
  SwaggerModule.setup('/docs', app, doc);

  await app.listen(config.port);
  console.log(`🚀 EMS backend running on http://localhost:${config.port}`);
  console.log(`📘 Swagger at         http://localhost:${config.port}/docs`);
}
bootstrap();
