# UC-001: View bracket

**As a** visitor, **I want to** see the current status of the Tournament **so that** I know who is going to play next.

**Status:** Not implemented
**Date:** 2026-03-27

---

## Main Flow

- I open the tournament website at `/`
- I see a visual depiction of the tournament bracket with the current status of the fixtures
- 
- 

---

## Business Rules

| ID | Rule                                                                                                                                                                                     |
|----|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Each fixture links visually to the preceding two fixtures (if present) where the winner is highlighted                                                                                   |
| BR-02 | Each fixture links to the following fixture (unless the fixture is the final one) with either known participants listed or placeholder(s) for the participants if they are still unknown |

---

## Acceptance Criteria

- [ ] Landing page shows current status of the Tournament
- [ ] The tournament bracket shows the full structure of the Tournament, with placeholders for participants for Fixtures where some or all participants have not yet been decided
- [ ] Page is accessible without authentication

---

## UI / Routes

- Clean, tournament-themend light layout

| Route | Access | Notes                   |
|-------|--------|-------------------------|
| `/` | public | Flow-based landing page |