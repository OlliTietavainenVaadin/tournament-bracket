# UC-003: Browse tournaments

**As a** visitor, **I want to** see a list of all tournaments and their status **so that** I can pick one to follow.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- I open the tournament website at `/`.
- I see a list of all tournaments. For each tournament I see:
  - the tournament's title (clickable, takes me to the bracket view)
  - the tournament's description
  - a status badge (`Open`, `Ongoing`, `Finished`, or `Draft`)
  - the registered/accepted participant count and the maximum
  - the start date (if set)
- I click a tournament title and arrive at the View Bracket page (UC-001).

---

## Business Rules

| ID    | Rule                                                                                                            |
|-------|-----------------------------------------------------------------------------------------------------------------|
| BR-01 | Tournaments are shown sorted by status (Open first, then Ongoing, then Finished, then Draft), then by start date ascending. |
| BR-02 | Tournaments in `CREATED` (Draft) status are hidden from the list for unauthenticated visitors.                  |
| BR-03 | The status badge text and color reflect the current Tournament status.                                          |

---

## Acceptance Criteria

- [ ] The list contains a working link to the View Bracket view for each tournament.
- [ ] Each tournament card shows the status badge with the correct label.
- [ ] Each tournament card shows the participant count as `accepted / max`.
- [ ] The list is sorted by status (Open → Ongoing → Finished → Draft) then by start date.
- [ ] `CREATED` tournaments are not shown to unauthenticated visitors.
- [ ] When there are no tournaments visible, an empty-state message is shown.

---

## Tests

- [ ] `TournamentListViewTest` covering: status badge rendering, participant counts, sort order, draft hiding, empty state, link navigation.

---

## UI / Routes

- Clean, tournament-themed light layout.
- Each tournament rendered as a card with title, description, status badge, participant count, start date.

| Route | Access | Notes                                                |
|-------|--------|------------------------------------------------------|
| `/`   | public | Vaadin Flow landing page (`TournamentListView`).    |
