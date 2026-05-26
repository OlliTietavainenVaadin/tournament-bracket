# UC-008: Log in and log out

---

**As a** Player or Tournament Manager, **I want to** log in and log out **so that** I can access role-protected views and end my session when I'm done.

**Status:** Implemented
**Date:** 2026-05-26

---

## Main Flow

### Logging in
- I (as a Visitor) open a protected route — e.g. `/signup` (Player) or `/admin` (Tournament Manager).
- The system redirects me to `/login`.
- I see a login form with **Username** and **Password** fields and a **Log in** button.
- I submit valid credentials.
- I am redirected back to the originally requested route, now authenticated.
- I see a **session indicator** in the application's top bar showing my username and a **Log out** action.

### Logging out
- While logged in (Player or Tournament Manager), I click **Log out** in the top bar.
- The system terminates my session and redirects me to the public tournament list (`/`).
- The top bar no longer shows the session indicator; instead it shows a **Log in** link.
- Re-visiting a protected route once more redirects me to `/login`.

### Failed login
- I open `/login` and submit an unknown username or wrong password.
- The login form shows an inline error message; I remain on `/login` and can retry.

---

## Business Rules

| ID    | Rule                                                                                                                                                 |
|-------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Authentication uses the pre-seeded in-memory users (`admin`/`admin` with role `ADMIN`; `player1`…`player20` with role `USER`).                       |
| BR-02 | Public routes (`/`, `/view/{tournamentId}`, `/login`) are accessible without authentication.                                                         |
| BR-03 | `/signup` requires role `USER` or `ADMIN`. `/admin/**` requires role `ADMIN`.                                                                        |
| BR-04 | Visiting a protected route while unauthenticated redirects to `/login` and, after a successful login, returns the user to the originally requested URL. |
| BR-05 | Logout invalidates the HTTP session and clears the security context.                                                                                 |
| BR-06 | A failed login keeps the user on `/login` and shows an error indicator; it never reveals whether the username or the password was wrong.             |
| BR-07 | The session indicator (username + Log out) is visible on every authenticated view; a Log in link is visible on every public view to anonymous users. |

---

## Acceptance Criteria

- [ ] Anonymous users visiting `/login` see a login form with username and password fields.
- [ ] Submitting `admin` / `admin` logs the user in with role `ADMIN` and grants access to `/admin` and `/signup`.
- [ ] Submitting `player1` / `player1` (any `playerN`) logs the user in with role `USER` and grants access to `/signup` but **not** `/admin`.
- [ ] Submitting an invalid username or password keeps the user on `/login` and displays the form's built-in error indicator.
- [ ] After login, the top bar displays the current username and a **Log out** action.
- [ ] Clicking **Log out** ends the session and redirects to `/` (public tournament list); subsequent navigation to a protected route redirects to `/login`.
- [ ] An authenticated `USER` who navigates directly to `/admin` is denied access (redirected to `/login` or to a Forbidden page — Vaadin Flow default behavior is acceptable).
- [ ] Public views (`/`, `/view/{tournamentId}`) remain reachable when logged out, and display a **Log in** action in the top bar.

---

## Tests

- [x] `LoginLogoutTest` — Browserless tests covering:
  - Anonymous access to a protected route redirects to login (`/signup` and `/admin`).
  - Successful login as `ADMIN` grants `/admin` access; the session indicator shows the username.
  - Successful login as `USER` grants `/signup` access but `/admin` is denied.
  - Top bar shows username + Log out when authenticated; Log in link when anonymous.
  - Logout clears the session — after logging out, navigating to a protected route again redirects to `/login`.
- [ ] `SecurityConfigTest` *(optional)* — verifies the pre-seeded users from `SecurityConfig` can authenticate via `AuthenticationManager`.

---

## UI / Routes

- A top bar (`MainLayout`) is applied to every Vaadin Flow view.
  - Left side: app title link (`Tournaments`) → `/`.
  - Right side, when anonymous: **Log in** link → `/login`.
  - Right side, when authenticated: `Signed in as {username}` text, and a **Log out** button.
- The login form is the existing Vaadin `LoginForm` rendered by `LoginView` at `/login`.

| Route        | Access                                  | Notes                                                                                       |
|--------------|-----------------------------------------|---------------------------------------------------------------------------------------------|
| `/login`     | public (`@AnonymousAllowed`)            | Vaadin Flow view. Shows `LoginForm`. `?error` query parameter switches the form to error.   |
| `/logout`    | authenticated                           | Handled by Spring Security's default logout endpoint, triggered via the Log out button.     |
| `/`          | public                                  | Tournament list — top bar shows the appropriate session indicator.                          |
| `/signup`    | `USER` or `ADMIN`                       | Already protected; unauthenticated access redirects to `/login`.                            |
| `/admin/**`  | `ADMIN`                                 | Already protected; non-admin authenticated access is denied.                                |
