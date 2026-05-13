# Plataforma de Procesamiento de Eventos GPS

Arquitectura de microservicios de alta disponibilidad para el procesamiento masivo de eventos GPS en tiempo real. Construida con **Java 25**, **Spring Boot 4.0.6**, **Apache Kafka 4.1** y **TimescaleDB (PostgreSQL 18)**, demostrando el uso de Virtual Threads (Project Loom), productores/consumidores optimizados para alto throughput y persistencia en hypertables de series temporales.

---

## Arquitectura

```
python-request/.venv/scripts.py
       │  POST /gps/ingest (200 threads concurrentes)
       ▼
┌───────────────────┐     Kafka Topic      ┌─────────────────────┐     Batch Insert      ┌──────────────┐
│  gps (Producer)   │─────► [GpsData] ─────►│  gpsconsumer        │─────────────────────►│  TimescaleDB │
│  puerto :9595     │   3 particiones       │  (Consumer)         │  saveAll(1000 lote)  │  Hypertable  │
│  Virtual Threads  │                       │  Batch listener     │  reWriteBatchedInserts│  gps_events  │
└───────────────────┘                       └─────────────────────┘                      └──────────────┘
```

**Flujo de datos:**
1. El script de estrés envía peticiones HTTP concurrentes a `gps`
2. `gps` publica cada evento en el topic `GpsData` de Kafka
3. `gpsconsumer` consume lotes de hasta 500 registros y los persiste en TimescaleDB
4. TimescaleDB almacena los eventos en una hypertable particionada por `event_timestamp`

---

## Tech Stack

### gps (Producer)

| Tecnología | Detalle |
|---|---|
| Spring Boot | 4.0.6 |
| Java | 25 (Virtual Threads habilitado) |
| Concurrencia | Controladores asíncronos con `CompletableFuture` que liberan hilos de Tomcat |
| Kafka Producer | Batching 64KB, Linger 15ms, Compresión lz4, Acks=1 |
| Tomcat | max-threads: 500, accept-count: 1000 |
| Puerto | 9595 |

### gpsconsumer (Consumer)

| Tecnología | Detalle |
|---|---|
| Spring Boot | 4.0.6 |
| Java | 25 |
| Kafka Consumer | Batch listener (500 records/poll), Fetch min 64KB, Concurrency 6 |
| Deserialización | `JacksonJsonDeserializer` con trusted packages `*` para Java Records |
| Persistencia | JPA Batch inserts (size 1000), `reWriteBatchedInserts=true`, `order_inserts=true` |
| Ack mode | BATCH (manual después de persistir el lote completo) |
| Puerto | 9090 |

### Apache Kafka

| Propiedad | Valor |
|---|---|
| Versión | 4.1.0 (KRaft mode, single-node) |
| Partitions topic `GpsData` | 3 |
| Retention | 8 días |
| Segment size | 1 GB |
| UI Management | Kafka-UI en puerto 8080 |

### TimescaleDB

| Propiedad | Valor |
|---|---|
| Versión | PostgreSQL 18 con extensión TimescaleDB |
| Imagen | `timescale/timescaledb:latest-pg18` |
| Puerto | 5435 |
| Hypertable | `gps_events` particionada por `event_timestamp` |
| Init script | `init-db/01_init.sql` crea la tabla e hypertable automáticamente |

### python-request (Stress Tool)

| Propiedad | Detalle |
|---|---|
| Script | `.venv/scripts.py` |
| Librerías | `requests.Session` con `HTTPAdapter` (pool de 200 conexiones), `ThreadPoolExecutor` |
| Carga | 200 threads concurrentes durante 60 segundos |

---

## Performance Results

| Métrica | Valor |
|---|---|
| Throughput máximo | **>75,000 eventos/minuto** |
| Latencia promedio | **~150 ms** (bajo 200 threads concurrentes) |
| Tasa de peticiones | **>1,300 req/seg** |
| Errores | **0** en condiciones de estréso máximo |

---

## Requisitos previos

- **Java 25** (JDK 25+)
- **Maven** (o usar `mvnw` incluido)
- **Docker + Docker Compose**
- **Python 3** con `requests` (`pip install requests`)

---

## Despliegue

### 1. Levantar infraestructura (Kafka + TimescaleDB + Kafka-UI)

```bash
cd gps
docker compose up -d
```

Esto inicia:
- `broker` — Apache Kafka 4.1.0 (puerto `9092`)
- `kafka-ui` — Interfaz de gestión Kafka (puerto `8080`)
- `timescaledb` — TimescaleDB PostgreSQL 18 (puerto `5435`, DB: `app_db`)

El script `init-db/01_init.sql` se ejecuta automáticamente al iniciar el contenedor de TimescaleDB, creando la hypertable `gps_events`.

### 2. Compilar y ejecutar `gps` (Producer)

```bash
cd gps
./mvnw clean package -DskipTests
java -jar target/gps-0.0.1-SNAPSHOT.jar
```

El servicio se levanta en `http://localhost:9595`.

### 3. Compilar y ejecutar `gpsconsumer` (Consumer)

```bash
cd gpsconsumer
./mvnw clean package -DskipTests
java -jar target/gpsconsumer-0.0.1-SNAPSHOT.jar
```

El servicio se levanta en `http://localhost:9090`.

### 4. Ejecutar stress test

```bash
cd python-request
pip install requests
python .venv/scripts.py
```

Salida esperada:
```
🔥 Starting stress test (with Sessions) for 60s...

📊 FINAL RESULTS
Total Sent: 78453 ✅
Total Errors: 0 ❌
Avg latency: 147.23 ms
Throughput: 1307.55 req/sec
```

---

## Stress Testing

El script `python-request/.venv/scripts.py` genera una ráfaga controlada de eventos GPS:

- **Endpoint**: `POST http://localhost:9595/gps/ingest`
- **Payload**: `{ deviceId, latitude, longitude, timestamp }`
- **Concurrencia**: 200 hilos simultáneos con `ThreadPoolExecutor`
- **Duración**: 60 segundos
- **Pool HTTP**: `requests.Session` con `HTTPAdapter` de 200 conexiones reutilizables

Esto permite validar el comportamiento del sistema bajo carga máxima y verificar que no se pierden eventos ni se degrada la latencia.
