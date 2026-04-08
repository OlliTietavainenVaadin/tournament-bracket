# UC-001: View bracket

**As a** visitor, **I want to** see the current status of a Tournament **so that** I know who is going to play next.

**Status:** Implemented
**Date:** 2026-03-27

---

## Main Flow

- I open the tournament website at `view/{tournament-id}`, where the {tournament-id} is the unique identifier of a Tournament
- I see a visual depiction of the tournament bracket with the current status of the fixtures
- Each fixture is connected to the next one with an angled Connector Line until the last one.
- The Connector Line starts from the vertical center of the previous fixture element, and ends at the vertical center of the next fixture element.
  - See ./reference-images/001-connector-correct.jpg of how the Connector Line should look like.
  - See ./reference-images/001-connector-incorrect.jpg of how the Connector Line should *not* look like.

---

## Business Rules

| ID | Rule                                                                                                                                                                                     |
|----|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01 | Each fixture links visually to the preceding two fixtures (if present) where the winner is highlighted                                                                                   |
| BR-02 | Each fixture links to the following fixture (unless the fixture is the final one) with either known participants listed or placeholder(s) for the participants if they are still unknown |

---

## Acceptance Criteria

- [x] The view-bracket page shows current status of the Tournament with the specified id
- [x] The tournament bracket shows the full structure of the Tournament, with placeholders for participants for Fixtures where some or all participants have not yet been decided
- [x] Page is accessible without authentication
- [x] The fixtures should be visually connected to each other so that there's a line from each previous fixture to the next one
- [x] The visual connectors between the fixtures should be rendered neatly so that they are not overlapping the fixture element
- [x] The visual connectors between the fixtures should start from the vertical middle point of the fixture, and end in the vertical middle point of the following fixture so it looks like a direct continuation of the line separating the player names
- [x] Entering the view without a valid ID leads to a page that lists all open tournaments (See use case 003)

---

## UI / Routes

- Clean, tournament-themend light layout

| Route   | Access | Notes                                                       |
|---------|--------|-------------------------------------------------------------|
| `/view` | public | Flow-based landing page. Parameterized with a tournament id |