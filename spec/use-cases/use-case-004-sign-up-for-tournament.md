# UC-004: Sign up for a Tournament

---

**As a** Player, **I want to** sign up for an open Tournament **so that** I can participate.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- I am logged in with a `USER` role.
- I open `/signup`.
- I see a list of all Tournaments currently `OPEN_FOR_REGISTRATIONS`. For each Tournament I see the title, description, accepted/max participant count, and my current registration status (`Not registered`, `Pending`, `Accepted`, `Declined`).
- For Tournaments I'm not yet registered for, I click **Sign up** on the row.
- The system creates a `PENDING` registration for me on that Tournament and shows a confirmation notification.
- The row's status updates to `Pending` and the **Sign up** button is hidden for that row.

---

## Business Rules

| ID    | Rule                                                                                                                                    |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Registrations are only allowed for Tournaments whose status is `OPEN_FOR_REGISTRATIONS`. Other statuses cannot accept registrations.    |
| BR-02 | A Tournament cannot accept new registrations once its accepted-participant count reaches `maxParticipants`.                             |
| BR-03 | A Player can register at most once per Tournament — re-submitting is rejected.                                                          |
| BR-04 | Only authenticated users with role `USER` (or `ADMIN` impersonating) may access the sign-up view.                                       |
| BR-05 | When a player registers, the Participant's `name` is set to the logged-in username and `registrationStatus` is `PENDING`.               |

---

## Acceptance Criteria

- [ ] Anonymous users navigating to `/signup` are redirected to the login page.
- [ ] Logged-in players see all Tournaments in `OPEN_FOR_REGISTRATIONS` status (and only those).
- [ ] Clicking **Sign up** creates a `PENDING` Participant linked to the logged-in user.
- [ ] If the player tries to register a second time for the same Tournament, the action is blocked and an error notification is shown.
- [ ] If `maxParticipants` accepted-or-pending registrations have been made, the **Sign up** button is disabled for that Tournament and a "Full" indicator is shown.
- [ ] The player's current registration status for each Tournament is visible.
- [ ] The list updates immediately when a registration succeeds.

---

## Tests

- [ ] `SignUpForTournamentTest` — list filtering by status, sign-up creates pending registration, double-registration rejected, full tournament rejected, status displayed per row.
- [ ] `RegistrationServiceTest` — service-level business rules independent of UI.

---

## UI / Routes

- A heading "Sign up for a tournament".
- A `Grid` of open tournaments showing title, description, participants, my status, and a sign-up button column.

| Route      | Access                              | Notes                                                |
|------------|-------------------------------------|------------------------------------------------------|
| `/signup`  | Authenticated (`USER` or `ADMIN`)  | Vaadin Flow view (`SignUpView`).                    |
