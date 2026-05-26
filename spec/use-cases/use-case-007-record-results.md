# UC-007: Record Fixture Results

---

**As a** Tournament Manager, **I want to** record the winner of each fixture **so that** the bracket progresses and players see updated standings.

**Status:** Approved
**Date:** 2026-05-21

---

## Main Flow

- From `/admin/manage` I select an `ONGOING` tournament and click **Record results**.
- I land on `/admin/results/{tournamentId}`.
- I see the tournament title and the bracket grouped by round. For each unfinished fixture with both participants known I see a fixture card with both participants and two buttons: **Player 1 wins**, **Player 2 wins**.
- I click **Player 1 wins**. The system sets that participant as the fixture's winner, advances them into `nextFixture.participant1` or `participant2` (whichever is the appropriate slot for this fixture's position), and the card re-renders showing the winner highlighted with a "Winner: …" label.
- I continue until the final fixture is decided. Setting the final's winner sets the tournament's `winner` and transitions status to `FINISHED`.

---

## Business Rules

| ID    | Rule                                                                                                                                              |
|-------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Only tournaments with status `ONGOING` accept result entries.                                                                                     |
| BR-02 | A fixture can only be decided when both `participant1` and `participant2` are set (i.e., not TBD).                                                |
| BR-03 | Once a fixture has a winner, the result cannot be changed from this view.                                                                         |
| BR-04 | Setting a winner advances that participant into the `nextFixture` (into `participant1` if the source fixture's position is even, `participant2` if odd). |
| BR-05 | Setting the winner of the final fixture sets the tournament's overall `winner` and transitions status to `FINISHED`.                              |
| BR-06 | Only ADMIN may access this view.                                                                                                                  |

---

## Acceptance Criteria

- [ ] Route requires ADMIN role.
- [ ] Each undecided fixture (with both participants set) shows two "wins" buttons.
- [ ] Each decided fixture shows the winner clearly labelled.
- [ ] Clicking a winner button persists the winner.
- [ ] The winner is propagated into the appropriate slot of `nextFixture`.
- [ ] Once the final is decided, the tournament's `winner` is the same participant and status flips to `FINISHED`.
- [ ] Attempting to record a result on a TBD fixture is impossible (the buttons are not rendered).

---

## Tests

- [ ] `RecordResultsViewTest` — UI flow per round, button availability for TBD fixtures, final winner sets tournament winner and status.
- [ ] `ResultServiceTest` — service-level propagation, slot assignment, status transition.

---

## UI / Routes

- Header: tournament title + status.
- Rounds rendered as columns; each fixture as a card with the two participants and (if decided) the result, or two action buttons if pending.

| Route                              | Access  | Notes                                                  |
|------------------------------------|---------|--------------------------------------------------------|
| `/admin/results/{tournamentId}`    | `ADMIN` | Vaadin Flow view (`RecordResultsView`).                |
