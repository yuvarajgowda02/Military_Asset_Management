# Military Asset Management System (MAMS)

A full-stack system for commanders and logistics personnel to track the
movement, assignment and expenditure of military assets (vehicles, weapons,
ammunition) across multiple bases, with role-based access control and a
full API audit trail.

Stack: **Java 17 + Spring Boot 3 (backend) · React 18 (frontend) · MySQL 8 (database)**

---

## 1. Project layout

```
military-asset-management/
├── backend/            Spring Boot REST API (Java 17, Maven)
├── frontend/            React 18 SPA (Create React App)
├── database/
│   ├── schema.sql        Table definitions only
│   ├── seed.sql           Demo data only
│   └── dump.sql           schema.sql + seed.sql combined (import this one)
└── README.md         (this file)
```

---

## 2. Tech stack & justification

**Backend — Java 17 / Spring Boot 3**
- Spring Web for REST controllers, Spring Data JPA (Hibernate) for the
  persistence layer, Spring Security + JJWT for stateless JWT authentication
  and method-level RBAC (`@PreAuthorize`), Spring AOP for a cross-cutting
  audit-log aspect, Bean Validation for request DTO validation.
- Spring Boot's opinionated defaults, mature security module, and first-class
  JPA support make it a fast, production-realistic choice for a
  transactional, role-gated logistics system like this one.

**Frontend — React 18**
- A component-based SPA fits the multi-page, form-heavy, role-aware nature
  of the UI (Dashboard, Purchases, Transfers, Assignments/Expenditures,
  Administration). React Router handles client-side routing and
  role-protected routes; Axios handles API calls with a JWT interceptor.
  Plain CSS (no heavy UI kit) keeps the bundle small (~80 KB gzip) while
  still being fully responsive (flex/grid layouts, a collapsing nav on
  narrow screens).

**Database — MySQL 8 (relational)**
- Asset movement is inherently transactional and relational: every
  purchase, transfer, assignment and expenditure references a specific
  base, equipment type and (optionally) a user, and the dashboard metrics
  are aggregates *over* those relationships and date ranges. A relational
  schema with foreign keys guarantees referential integrity (you cannot
  transfer/assign equipment that doesn't exist, or attribute a transaction
  to a base that was deleted) and lets the dashboard use simple, provably
  correct SQL/JPA aggregate queries instead of hand-rolled consistency
  logic that a document store would require. InnoDB also gives us ACID
  transactions, which matter when money and accountability are involved.

### How the schema supports the requirements
- `purchases`, `transfers`, `assignments`, `expenditures` are append-only
  ledger tables — nothing is ever mutated in place (aside from an
  assignment's `status`/`returned_date` when equipment is returned), so the
  full history of every movement is always reconstructable and auditable.
- **Opening Balance** for a period = net of every purchase / transfer-in /
  transfer-out / expenditure recorded *before* the period start.
- **Net Movement** = Purchases + Transfer In − Transfer Out *within* the
  period.
- **Closing Balance** = Opening Balance + Net Movement − Expended (within
  the period).
- **Assigned** = quantity of currently-assigned (not yet returned) items as
  of the period end date.
- All of this is computed on the fly by `DashboardService`, so it is always
  consistent with the underlying transactions — there is no separate
  "balance" table that could drift out of sync.
- `audit_logs` captures every API call (who, what role, which endpoint,
  HTTP method, status code, timestamp, IP) via a Spring AOP aspect around
  every controller method, satisfying the "all transactions must be logged
  for auditing purposes" requirement without scattering logging code
  throughout the services.

---

## 3. Role-Based Access Control

| Role | Access |
|---|---|
| **ADMIN** | Full access to every base, every endpoint, user/base/equipment management, and the audit log. |
| **BASE_COMMANDER** | Dashboard, Purchases, Transfers, Assignments & Expenditures — scoped to their own base only (enforced server-side, not just hidden in the UI). |
| **LOGISTICS_OFFICER** | Purchases and Transfers only, scoped to their own base. No Dashboard, no Assignments/Expenditures, no Administration. |

RBAC is enforced in two layers:
1. **Route/endpoint level** — `@PreAuthorize("hasRole(...)")` /
   `hasAnyRole(...)` on every controller.
2. **Data level** — `SecurityUtils.restrictedBaseId()` forces every
   query and every write for non-admins onto their own `base_id`,
   regardless of what the client sends, so a Base Commander cannot read or
   write another base's data even by tampering with request parameters.

Passwords are BCrypt-hashed; authentication is stateless JWT (`Authorization: Bearer <token>`), validated on every request by `JwtAuthFilter`.

---

## 4. Running the project

### Prerequisites
- Java 17+, Maven 3.8+
- Node.js 18+ and npm
- MySQL 8.x running locally (or reachable)

### 4.1 Database

**Option A — Docker (fastest):**
```bash
docker compose up -d
```
This starts MySQL 8 pre-loaded with `database/dump.sql` (schema + demo data)
on `localhost:3306`, user `root` / password `root` — matching the backend's
default `application.properties` exactly, so no further configuration is
needed.

**Option B — existing local MySQL:**
```bash
mysql -u root -p < database/dump.sql
```

This creates the `military_asset_db` database, all tables, and demo data
(3 bases, 3 equipment types, 4 users, sample purchases/transfers/
assignments/expenditures).

> The backend can also seed this same demo data automatically on first run
> (see `DataSeeder.java`) if you'd rather start from an empty schema and let
> `spring.jpa.hibernate.ddl-auto=update` create the tables — in that case
> skip `dump.sql`/`seed.sql` and only the DB itself needs to exist:
> `CREATE DATABASE military_asset_db;`. Don't run both the SQL seed *and*
> the Java seeder against the same empty database — pick one.

### 4.2 Backend

```bash
cd backend
# edit src/main/resources/application.properties if your MySQL
# username/password/port differ from the defaults (root / root / 3306)
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`

### 4.3 Frontend

```bash
cd frontend
npm install
npm start
```

Opens `http://localhost:3000`. It talks to the API via the `REACT_APP_API_URL`
env var in `frontend/.env` (defaults to `http://localhost:8080/api`).

### 4.4 Demo logins

| Username | Password | Role | Base |
|---|---|---|---|
| `admin` | `Admin@123` | ADMIN | — |
| `commander.alpha` | `Commander@123` | BASE_COMMANDER | Fort Alpha |
| `logistics.alpha` | `Logistics@123` | LOGISTICS_OFFICER | Fort Alpha |
| `commander.bravo` | `Commander@123` | BASE_COMMANDER | Fort Bravo |

---

## 5. Key API endpoints

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |
| GET/POST/PUT/DELETE | `/api/bases` | GET: authenticated · writes: ADMIN |
| GET/POST/PUT/DELETE | `/api/equipment-types` | GET: authenticated · writes: ADMIN |
| GET/POST | `/api/users` | ADMIN |
| GET/POST | `/api/purchases` | ADMIN, BASE_COMMANDER, LOGISTICS_OFFICER |
| GET/POST | `/api/transfers` | ADMIN, BASE_COMMANDER, LOGISTICS_OFFICER |
| GET/POST | `/api/assignments`, `PATCH /api/assignments/{id}/return` | ADMIN, BASE_COMMANDER |
| GET/POST | `/api/expenditures` | ADMIN, BASE_COMMANDER |
| GET | `/api/dashboard/metrics` | ADMIN, BASE_COMMANDER |
| GET | `/api/dashboard/movement-details` | ADMIN, BASE_COMMANDER (Net Movement drill-down) |
| GET | `/api/audit-logs` | ADMIN |

All list endpoints (`purchases`, `transfers`, `assignments`, `expenditures`,
`dashboard/*`) accept optional query params: `baseId`, `equipmentTypeId`,
`startDate`, `endDate` (ISO `yyyy-MM-dd`).

---

## 6. Verification performed on this build

- **Frontend:** actually installed (`npm install`) and built
  (`npm run build`) in a clean environment — compiles with zero errors.
- **Backend:** the sandbox this was built in has no access to Maven
  Central, so a full `mvn compile` could not be run here. As the next best
  check, all 65 `.java` files were compiled together with `javac` (no
  dependency jars on the classpath). Every resulting error was exclusively
  "package does not exist" for external libraries (Spring, Jakarta,
  Lombok, Jackson) — i.e. things Maven fetches — with **zero** errors
  referencing this project's own classes, meaning every cross-file
  reference inside the codebase resolves correctly and there are no syntax
  errors. Run `mvn spring-boot:run` on your machine (with normal internet
  access) to do a full build.

## 7. Notes & possible next steps

- Entities are currently returned directly from most endpoints for
  simplicity; a larger production build would introduce response DTOs to
  fully decouple the API contract from the JPA model.
- `spring.jpa.open-in-view` is left at its default (`true`) specifically so
  those directly-returned entities' lazy `@ManyToOne` associations (base,
  equipmentType, createdBy) can serialize to JSON without extra mapping
  code; switching to response DTOs would let this be turned off.
- The dashboard is intentionally "no persisted balances" — everything is
  derived from the ledger tables — which keeps it always correct, at the
  cost of scanning more rows as data grows. For very large datasets a
  materialized daily-balance table refreshed by a scheduled job would be
  the next optimization.
