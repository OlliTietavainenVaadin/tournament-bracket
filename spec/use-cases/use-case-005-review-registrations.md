# UC-005: Review Tournament Registrations

---

**As a** Tournament Manager, **I want to** review pending registrations and accept or decline each one **so that** only approved players appear in the bracket.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- From `/admin/manage` I select a tournament that is `OPEN_FOR_REGISTRATIONS` and click **Review registrations**.
- I land on `/admin/registrations/{tournamentId}`.
- I see the tournament title plus the current accepted / pending / max counts.
- I see a grid of every Participant with columns: player name, username, registration status, and an action column with **Accept** / **Decline** buttons (enabled only for `PENDING` rows).
- I click **Accept** on a pending registration. The row's status updates to `Accepted`, the accepted count increases, and the action buttons for that row become disabled.
- I click **Decline** on a pending registration. The row's status updates to `Declined`.

---

## Business Rules

| ID    | Rule                                                                                                                                        |
|-------|---------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Only the tournament owner (admin) can review registrations.                                                                                 |
| BR-02 | Only registrations in `PENDING` state may be accepted or declined; `ACCEPTED` / `DECLINED` rows cannot be changed from this view.           |
| BR-03 | A tournament cannot have more `ACCEPTED` registrations than `maxParticipants`. Accept actions that would exceed the limit are rejected.     |
| BR-04 | When a tournament is not in `OPEN_FOR_REGISTRATIONS` status, no changes can be made (the action buttons are disabled).                      |

---

## Acceptance Criteria

- [ ] Route requires ADMIN role.
- [ ] The grid lists every Participant for the tournament with their registration status.
- [ ] **Accept** and **Decline** buttons are enabled only for `PENDING` rows and only when the tournament is `OPEN_FOR_REGISTRATIONS`.
- [ ] Accepting a registration changes its status to `ACCEPTED`.
- [ ] Declining a registration changes its status to `DECLINED`.
- [ ] Accepting a participant beyond `maxParticipants` fails with an error notification.
- [ ] The header reflects the current accepted / pending / max counts after each action.

---

## Tests

- [ ] `ReviewRegistrationsViewTest` — grid contents, accept/decline interactions, max-participant guard, role guard.
- [ ] `RegistrationServiceTest` — backend rules for accept/decline.

---

## UI / Routes

- Header: tournament title + counts.
- Grid with action column.

| Route                                  | Access                  | Notes                                                  |
|----------------------------------------|-------------------------|--------------------------------------------------------|
| `/admin/registrations/{tournamentId}`  | `ADMIN`                 | Vaadin Flow view (`ReviewRegistrationsView`).         |
