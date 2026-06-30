# Production Deployment Guide

ProcessPro is package-configured to deploy easily via Docker Compose or Kubernetes.

## Docker Compose Deployment

To build and run the production containers:

```bash
docker compose up -d --build
```

This starts:
- **processpro-postgres**: Database service with persistent volume mappings.
- **processpro-backend**: Spring Boot application in a JVM-optimized JRE container.
- **processpro-nginx**: Packages the compiled React production bundle and proxies `/api/*` and `/auth/*` traffic.

---

## Health Checks & Monitoring

The backend exposes health and metrics endpoints via Spring Boot Actuator:
- **Actuator Health Check**: `http://localhost:8080/actuator/health`
- **Application Metrics**: `http://localhost:8080/actuator/metrics`

---

## Production Security Checklists
1. Update `JWT_SECRET` in `.env` with a 256-bit cryptographically secure string.
2. Change default database password `dev_only` in production configs.
3. Configure SSL/TLS certificates inside the Nginx container proxy.
