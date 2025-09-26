import { Test } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import * as request from 'supertest';
import { AppModule } from '../../src/app.module';

describe('EMS Backend (e2e)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const module = await Test.createTestingModule({ imports: [AppModule] }).compile();
    app = module.createNestApplication();
    app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }));
    await app.init();
  });

  afterAll(async () => app.close());

  it('/healthz (GET)', async () => {
    await request(app.getHttpServer()).get('/healthz').expect(200);
  });

  it('/api/encounters (POST)', async () => {
    const res = await request(app.getHttpServer())
      .post('/api/encounters')
      .send({ patientId: 'P-001', location: { lat: 36.64, lng: 127.49 }, condition: 'cardiac' })
      .expect(201);
    expect(res.body).toHaveProperty('encounterId');
  });

  it('/api/recommend (POST)', async () => {
    const res = await request(app.getHttpServer())
      .post('/api/recommend')
      .send({ encounterId: 'E-001', urgency: 'critical', location: { lat: 36.64, lng: 127.49 } })
      .expect(201); // Nest 기본은 201로 설정됨. 필요 시 @HttpCode(200) 사용
    expect(Array.isArray(res.body.recommendations)).toBe(true);
  });
});
