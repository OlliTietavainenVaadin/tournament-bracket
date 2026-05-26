# UC-006: Start Tournament

---

**As a** Tournament Manager, **I want to** start a tournament once the right players are accepted **so that** the bracket is generated and play can begin.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- From `/admin/manage` I select a tournament in `OPEN_FOR_REGISTRATIONS` status and click **Start tournament**.
- A confirmation dialog tells me how many accepted players will be in the bracket and what the bracket size will be (next power of two; missing slots become byes).
- I click **Start**.
- The system:
  - sets status to `ONGOING`,
  - shuffles the accepted participants and seeds them into a single-elimination bracket of size 2ⁿ (smallest power of two ≥ accepted count),
  - assigns byes for missing slots (a `null` participant on one side of a first-round fixture means the present participant auto-advances; if both sides are null the fixture is skipped),
  - persists all fixtures with their `round`, `position`, and `nextFixture` references.
- I am taken to `/admin/results/{tournamentId}` (UC-007) to record results.

---

## Business Rules

| ID    | Rule                                                                                                                                    |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Only tournaments with status `OPEN_FOR_REGISTRATIONS` can be started.                                                                   |
| BR-02 | A tournament needs at least 3 `ACCEPTED` participants to start.                                                                         |
| BR-03 | A tournament with more than 32 `ACCEPTED` participants cannot start (matches `maxParticipants` upper bound).                            |
| BR-04 | The bracket is single-elimination. Bracket size is the smallest power of two ≥ accepted-participant count; missing slots receive byes. |
| BR-05 | Participants are paired randomly (shuffled) into the first round.                                                                       |
| BR-06 | Starting a tournament is irreversible.                                                                                                  |
| BR-07 | A first-round fixture where one side has no participant immediately advances the present participant to the next fixture as the winner. |

---

## Acceptance Criteria

- [ ] Route requires ADMIN role.
- [ ] Starting a tournament transitions its status from `OPEN_FOR_REGISTRATIONS` to `ONGOING`.
- [ ] Fixtures are created for every round of the bracket.
- [ ] The number of first-round fixtures equals `bracketSize / 2`.
- [ ] Every fixture (except the final) has `nextFixture` pointing to its parent.
- [ ] Each accepted participant appears in exactly one first-round slot.
- [ ] Byes auto-advance their player.
- [ ] Starting with fewer than 3 accepted participants is rejected with an error notification.

---

## Tests

- [ ] `StartTournamentServiceTest` — bracket sizing for 3, 4, 5, 8, 11, 16 participants; bye auto-advance; status transition; rejection cases.
- [ ] `ManageTournamentsTest` — start action triggers service and navigates.

---

## UI / Routes

- Confirmation dialog with summary of accepted participants and resulting bracket size.

| Route                                  | Access                  | Notes                                                  |
|----------------------------------------|-------------------------|--------------------------------------------------------|
| `/admin/manage`                        | `ADMIN`                 | Lifecycle action available from the manage grid.       |
