# UC-002: Manage Tournaments

---

**As a** Tournament Manager, **I want to** create, modify, and delete Tournaments **so that** tournaments can be played.

**Status:** Implemented
**Date:** 2026-04-07

---

## Main Flow

- I navigate to /admin/manage
- I see a table listing all open and closed Tournaments 
- I click "Create tournament" to open a form for creating a new Tournament
- I fill in the Tournament's name, description, and starting date. 
- I click "Open for registrations" and the Tournament is saved, added to the table, and ready for sign-ups
- Once a new Tournament is saved, 
- I can open an existing tournament for editing. This allows me to update its name, description, and starting date.

---

## Business Rules

| ID    | Rule                                                                           |
|-------|--------------------------------------------------------------------------------|
| BR-01 | Each tournament must have at least a Name                                      |
| BR-02 | A new Tournament's starting date can't be in the past, but it can be undefined |
| BR-03 | Only Tournament Managers can access the Manage Tournaments view                |
| BR-04 | Tournament Manager can delete a Tournament, but only after confirmation        |

---

## Acceptance Criteria

- [x] Tournament Manager can see a list of all Tournaments
- [x] Tournament Manager can add a new Tournament with name, description, and starting date
- [x] Tournament Manager can edit an existing Tournament
- [x] Tournament Manager can delete an existing Tournament
- [x] Before deletion, Tournament Manager must confirm they are sure they want to do a deletion
- [x] Route requires the ADMIN role to access

---

## Tests

> Write browserless tests that verify the acceptance criteria above. See `architecture.md` § Testing for conventions.

- [x] ManageTournamentsTest

---

## UI / Routes

| Route      | Access                                | Notes |
|------------|---------------------------------------|-------|
| `[/admin]` | Limited to ADMIN (Tournament Manager) | [Vaadin @Route] |