# UC-002: Manage Tournaments

---

**As a** Tournament Manager, **I want to** create, modify, delete, and progress Tournaments through their lifecycle **so that** I can run tournaments end to end.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- I navigate to `/admin/manage`.
- I see a grid listing all tournaments with their **status**, **name**, **description**, **start date**, and **accepted / max participants**.
- I click **Create tournament** to open the form. I fill in name, description, start date, and max participants. I click **Save** — the tournament is created in `CREATED` status.
- I select an existing tournament. Depending on its status, the toolbar shows one of:
  - `CREATED` → **Open for registrations** (advance to `OPEN_FOR_REGISTRATIONS`), **Edit**, **Delete**
  - `OPEN_FOR_REGISTRATIONS` → **Review registrations** (UC-005), **Start tournament** (UC-006), **Edit**, **Delete**
  - `ONGOING` → **Record results** (UC-007)
  - `FINISHED` → no lifecycle action; the tournament is read-only.
- I click **Delete**, confirm in a dialog, and the tournament is removed.

---

## Business Rules

| ID    | Rule                                                                                       |
|-------|--------------------------------------------------------------------------------------------|
| BR-01 | Each tournament must have at least a Name and `maxParticipants` between 3 and 32.          |
| BR-02 | A new tournament's start date can't be in the past, but it can be undefined.               |
| BR-03 | Only Tournament Managers can access the Manage Tournaments view.                           |
| BR-04 | Tournament Manager can delete a Tournament only after confirmation.                        |
| BR-05 | A tournament can only progress forward through statuses (`CREATED` → `OPEN_FOR_REGISTRATIONS` → `ONGOING` → `FINISHED`). |
| BR-06 | Tournaments in `ONGOING` or `FINISHED` cannot be deleted from this view.                   |
| BR-07 | Tournaments in `ONGOING` or `FINISHED` cannot be edited (title/description/dates).         |

---

## Acceptance Criteria

- [ ] Admin can see a grid of all Tournaments including their status and participant counts.
- [ ] Admin can create a new Tournament; the tournament starts in `CREATED` status.
- [ ] Admin can edit `CREATED` and `OPEN_FOR_REGISTRATIONS` tournaments.
- [ ] Admin can delete `CREATED` and `OPEN_FOR_REGISTRATIONS` tournaments after a confirmation dialog.
- [ ] Admin can advance a `CREATED` tournament to `OPEN_FOR_REGISTRATIONS`.
- [ ] Lifecycle action buttons are only shown when applicable to the selected tournament's status.
- [ ] Route requires the ADMIN role.

---

## Tests

- [x] `ManageTournamentsTest` — grid display, create, edit, delete, anonymous access, status transitions, validation rules.

---

## UI / Routes

| Route             | Access                              | Notes                                                          |
|-------------------|-------------------------------------|----------------------------------------------------------------|
| `/admin/manage`   | Limited to ADMIN (Tournament Manager) | Vaadin Flow view. Toolbar buttons enable based on selection.  |
