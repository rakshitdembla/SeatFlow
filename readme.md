# SeatFlow

A backend for an event ticket booking platform — built with Spring Boot to demonstrate production-style backend engineering: authentication, concurrency control, payment integration, and background processing, not just CRUD.

## What it does

A user registers, verifies their email, browses events, locks and books seats, pays through Razorpay, and receives a confirmation email with their tickets. An admin creates and manages events, scoped to only the ones they created. Seats abandoned mid-checkout are automatically reclaimed by a background job.

## Design highlights

A few decisions worth a closer look — the parts that go beyond a typical tutorial CRUD app.

**Concurrency-safe seat booking**
Seat selection uses `SELECT ... FOR UPDATE` with a consistent row-locking order, so two users racing for the same seat can't both succeed, and multi-seat bookings can't deadlock against each other.

**Stateless access tokens, revocable refresh tokens**
Access tokens are short-lived, signed JWTs. Refresh tokens are opaque, hashed at rest, rotate on every use, and can be revoked server-side on logout or password reset — something a pure JWT can't do.

**Idempotent payment verification**
Confirming a Razorpay payment is safe to retry — a booking that's already `CONFIRMED` returns its existing state instead of erroring or generating duplicate tickets.

**Transaction boundaries that survive rollback**
When a payment fails verification, the failure state commits in an independent `REQUIRES_NEW` transaction, specifically so it isn't undone when the outer transaction rolls back and the error propagates to the client.

**Ownership-scoped resources, deliberately different status codes**
Admins can only touch events they created (`403` — events are public knowledge). Users can only see their own bookings (`404`, not `403`, for anyone else's — indistinguishable from "doesn't exist," to avoid leaking who booked what).

**Abuse-resistant OTP flows**
Email verification and resend share a Redis-backed cooldown. Forgot-password always returns an identical response regardless of whether the email is registered, to prevent account enumeration.

**Self-healing seat state**
A scheduled job finds seats locked past their checkout window with no completed payment and releases them — availability stays accurate with no manual cleanup.

## Tech stack

Java 21 · Spring Boot 3.5 · Spring Security · JWT (jjwt) · Spring Data JPA · PostgreSQL · Redis · Razorpay Java SDK · Gmail SMTP · JUnit 5 · Mockito

## Project structure

```
src/main/java/.../event_ticket_booking/
├── config          Security, Razorpay configuration
├── controller      REST endpoints
├── service         Business logic
├── repository      Spring Data JPA repositories
├── entity          JPA entities
├── dto             Request/response DTOs
├── mapper          Entity <-> DTO conversion
├── exception       Custom exceptions + global handler
├── security        JWT filter, token service, user principal
├── enums           Status/role enums
└── scheduler       Seat unlock scheduler
```

## API overview

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth/**` | Register, login, verify email, refresh token, forgot/reset password, logout — public except `logout` |
| Events (admin) | `/api/admin/events/**` | Create/update/delete/list own events — requires `ADMIN` role |
| Events (public) | `/api/events/**` | Browse events, view details, view seat map — no auth required |
| Bookings | `/api/bookings/**` | Create a booking, view booking history and tickets — requires login |
| Payments | `/api/payments/**` | Verify a Razorpay payment, mark a payment as failed — requires login |

## Running locally

**Prerequisites:** Java 21, PostgreSQL, Redis, a Gmail account with an [App Password](https://myaccount.google.com/apppasswords), and a [Razorpay](https://razorpay.com) test account.

1. Create a Postgres database and a `.env` file in the project root (see `.env.example` for the full list — database credentials, JWT secret, mail credentials, Razorpay keys).
2. Start Postgres and Redis.
3. Run the app:
   ```
   ./mvnw spring-boot:run
   ```

The app starts on `http://localhost:8080` with the `dev` profile active by default.

## Running tests

```
./mvnw test
```
