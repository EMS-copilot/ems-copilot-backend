EMS Copilot Korea – Backend (NestJS)


충청권 EMS 라우팅 PoC의 백엔드 전용 레포입니다.


NestJS + TypeScript 기반의 경량 API로, 환자 접촉 생성과 병원 추천을 제공합니다.


✨ 핵심 기능
- POST /api/encounters : 환자 접촉 생성 (UUID 발급)

- POST /api/recommend : 환자 상태/위치 기반 병원 추천

- GET /healthz : 헬스체크

- Swagger /docs : API 문서 & 테스트 콘솔


📁 폴더 구조

.
├── .github/workflows/ci.yml          # Lint/Test/Build CI
├── test/
│   ├── e2e/app.e2e-spec.ts           # supertest 기반 E2E
│   └── jest-e2e.json
├── src/
│   ├── common/config.ts              # env 로딩, CORS/PORT 등
│   ├── main.ts                       # 부트스트랩, Swagger, ValidationPipe
│   ├── app.module.ts                 # 루트 모듈
│   └── modules/
│       ├── health/health.controller.ts
│       ├── audit/audit.service.ts    # 인메모리 감사 로그 (DB 교체 예정)
│       ├── hospitals/hospitals.mock.ts
│       ├── encounters/
│       │   ├── dto/create-encounter.dto.ts
│       │   ├── encounters.controller.ts
│       │   └── encounters.service.ts
│       └── recommend/
│           ├── dto/recommend.dto.ts
│           ├── recommend.controller.ts
│           └── recommend.service.ts
├── .eslintrc.json
├── .gitignore
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── package.json
├── tsconfig.json
├── tsconfig.build.json
└── nest-cli.json


🧰 기술 스택
- Runtime/Framework: Node.js 18, NestJS 10, Express

- Lang/Tooling: TypeScript, ESLint, Jest + supertest

- Docs: Swagger (OpenAPI) /docs

- CI: GitHub Actions (lint → build → test)

- Container: Dockerfile, docker-compose


🚀 빠른 시작 (로컬)

# 1) 의존성 설치
npm ci

# 2) 환경변수 파일 생성
# Windows
copy .env.example .env
# macOS/Linux
cp .env.example .env

# 3) 개발 서버 실행
npm run dev
# → http://localhost:4000/docs (Swagger)
기본 포트: 4000


⚙️ 환경 변수

.env (예시는 .env.example 참고):

CORS
src/common/config.ts 에서 CORS_ORIGIN='*'이면 모든 오리진 허용, 아니면 해당 오리진만 허용 + credentials 사용.


📚 API 스펙 (요약)


1) 헬스체크

GET /healthz → 200 OK
{ "status": "ok", "ts": "2025-09-26T01:23:45.678Z" }


2) 환자 접촉 생성

- POST /api/encounters → 201 Created

- Body (DTO: CreateEncounterDto)

{
  "patientId": "P-001",
  "location": { "lat": 36.6424, "lng": 127.489 },
  "condition": "cardiac" // cardiac|trauma|respiratory|stroke|other
}

- response
{
  "encounterId": "40c7b9c9-5c9c-4d0e-8d5b-2d6d6f4f9e3a",
  "timestamp": "2025-09-26T01:23:45.678Z",
  "message": "환자 접촉 기록이 성공적으로 생성되었습니다"
}


3) 병원 추천

- POST /api/recommend → 201 Created

- Body (DTO: RecommendDto)

{
  "encounterId": "E-001",
  "urgency": "critical",     // critical|urgent|normal
  "location": { "lat": 36.64, "lng": 127.49 }
}


- Response

{
  "encounterId": "E-001",
  "timestamp": "2025-09-26T01:23:45.678Z",
  "policy": { "highRisk": true, "rejectAllowed": false },
  "recommendations": [
    {
      "rank": 1,
      "hospitalId": "h1",
      "name": "충북대학교병원",
      "eta": 11,
      "acceptProbability": 100,
      "doorToTreatment": 32,
      "reasons": ["높은 가용 병상", "빠른 도착 가능", "높은 수용 확률", "중증 환자 우선 배치"]
    }
  ]
}


오류 응답 (ValidationPipe 기본 형식)

{
  "statusCode": 400,
  "message": ["urgency must be one of the following values: critical, urgent, normal"],
  "error": "Bad Request"
}
전체 스키마/요청 샘플은 Swagger /docs 에서 확인 및 실행 가능.



🧪 테스트

E2E 테스트(supertest):
npm test
# 또는
npm run test:e2e
- 테스트 위치: test/e2e/app.e2e-spec.ts



🧹 스크립트

npm run dev     # 개발 서버 (watch)
npm run build   # 빌드 (dist/)
npm start       # 프로덕션 실행 (node dist/main.js)
npm run lint    # ESLint
npm test        # E2E 테스트


🐳 Docker
빌드 & 실행

docker compose up --build
# api: http://localhost:4000


Dockerfile 개요

stage: deps → builder → runner

dist/만 러너 이미지에 포함하여 경량 실행


🔐 보안 / 로깅 / 규정 준수

감사 로그(Audit): 현재 인메모리(AuditService) 저장.
운영 전환 시 DB/WORM(S3 Glacier 등) + 암호화/무결성 검증 필요.

인증/인가: PoC 단계로 비활성화. 운영 시 JWT/RBAC 또는 게이트웨이 적용.

입력 검증: ValidationPipe({ whitelist: true, transform: true }) 전역 적용.

CORS: .env에서 오리진 제한 가능.

🔧 확장 로드맵 (제안)

데이터베이스: PostgreSQL + Prisma/Drizzle → Encounters/Audit 영속화

병원 리소스: 실시간 가용량(OR/ICU/전문의) 연동

추천 엔진: ETA/혼잡도/전문센터 스코어링, 가중치 튜닝, 학습 기반 모델

인증: OAuth2/JWT + 조직/역할 기반 권한 (EMS, 병원, 관리자)

관측성: pino/winston + OpenTelemetry, Grafana/Loki, Prometheus

레이트리밋 & 캐시: Redis

배포: Docker + GitHub Actions → 클라우드(EC2/ECS/Kubernetes)

🤝 컨트리뷰션 가이드

이슈 → 브랜치 생성 (예: feat/recommend-score-boost)

커밋 규칙(권장): Conventional Commits

feat: …, fix: …, chore: …, refactor: …, test: …

PR 올리면 CI가 lint/build/test 실행

코드 스타일: ESLint 규칙 준수, DTO/타입 엄격 적용


🔍 프런트엔드 연동 팁

- 프런트 .env.local:

NEXT_PUBLIC_API_URL=http://localhost:4000

- 예시:
await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/recommend`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ encounterId, urgency, location })
});


🆘 트러블슈팅

포트 충돌: .env에서 PORT 변경

CORS 에러: CORS_ORIGIN 값을 프런트 도메인으로 설정

Swagger 404: dev 서버가 다른 포트/경로인지 확인 (/docs)

테스트 실패: E2E는 실제 서버 부팅 없이 Nest 테스트 앱 인스턴스로 동작 (환경 차이 확인)