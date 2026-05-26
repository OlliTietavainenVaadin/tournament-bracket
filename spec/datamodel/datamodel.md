# Data Model

> Entity definitions and relationships. Evolves as features are added.

## Entities

| Entity          | Key Fields                                                          | Relationships                              |
|-----------------|---------------------------------------------------------------------|--------------------------------------------|
| **Tournament**  | id, title, description, status, startDate, maxParticipants, winner  | Has many Participants, has many Fixtures   |
| **Participant** | id, name, username, registrationStatus                              | Belongs to Tournament                      |
| **Fixture**     | id, round, position, playDate, participant1, participant2, winner, nextFixture | Belongs to Tournament, has two Participants |

## Entity Details

### Tournament
| Field           | Type                | Constraints                                                                        |
|-----------------|---------------------|------------------------------------------------------------------------------------|
| id              | Long                | PK, auto-generated                                                                 |
| title           | String              | Required, max 200                                                                  |
| description     | String              | Optional, max 2000                                                                 |
| status          | TournamentStatus    | Required. One of: `CREATED`, `OPEN_FOR_REGISTRATIONS`, `ONGOING`, `FINISHED`       |
| startDate       | LocalDate           | Optional                                                                           |
| maxParticipants | Integer             | Required, 3 ≤ value ≤ 32                                                           |
| winner          | Participant         | Optional. Set only when status is `FINISHED`                                       |
| participants    | List<Participant>   | Cascade-all, orphan-removal                                                        |
| fixtures        | List<Fixture>       | Cascade-all, orphan-removal                                                        |

#### Tournament status lifecycle
- `CREATED` — initial state after the admin creates the tournament; not yet visible for sign-ups.
- `OPEN_FOR_REGISTRATIONS` — players can sign up. Admin can review/accept/decline each registration.
- `ONGOING` — bracket has been generated, fixtures are scheduled, registrations are closed.
- `FINISHED` — the final winner has been set; the bracket is immutable.

The admin transitions a tournament through the lifecycle in order; backwards transitions are not supported.

### Participant
| Field              | Type                  | Constraints                                          |
|--------------------|-----------------------|------------------------------------------------------|
| id                 | Long                  | PK, auto-generated                                   |
| name               | String                | Required, max 80 — shown in the bracket              |
| username           | String                | Optional, max 80 — username of the player who registered (null for admin-created/seed participants) |
| registrationStatus | RegistrationStatus    | Required. One of `PENDING`, `ACCEPTED`, `DECLINED`   |
| tournament         | Tournament            | Many-to-one back reference                           |

Only participants with `registrationStatus = ACCEPTED` are placed in the bracket when the tournament starts. Once the tournament is `ONGOING`, registrationStatus is no longer mutable.

### Fixture
| Field        | Type        | Constraints                                                  |
|--------------|-------------|--------------------------------------------------------------|
| id           | Long        | PK, auto-generated                                           |
| round        | int         | Required, 1-based; 1 = first round                           |
| position     | int         | Required, 0-based position within the round                  |
| playDate     | LocalDate   | Optional                                                     |
| participant1 | Participant | Optional (null = TBD)                                        |
| participant2 | Participant | Optional (null = TBD)                                        |
| winner       | Participant | Optional; must equal participant1 or participant2 when set   |
| nextFixture  | Fixture     | Optional; the fixture this fixture's winner advances into    |

## Sample Data

On startup, seed the database with **20 player user accounts** (`player1` … `player20`, each with password equal to the username, role `USER`) plus the admin (`admin` / `admin`). Then seed three tournaments that exercise every status of the lifecycle:

### Tournament A — `Spring Chess Classic` (status: `FINISHED`)
8-player single-elimination bracket, fully played out. This is the demo's "history" tournament — used to verify the bracket renderer with completed results.

- Round 1 (Quarterfinals): `James` vs `Tina` (winner: Tina), `Lizzy B` vs `Reindeer Rudolph` (Lizzy B), `Benoit` vs `Mary` (Mary), `Robot Rolf` vs `Camper Vance` (Camper Vance)
- Round 2 (Semifinals): `Tina` vs `Lizzy B` (Lizzy B), `Mary` vs `Camper Vance` (Camper Vance)
- Round 3 (Final): `Lizzy B` vs `Camper Vance` (Lizzy B)
- Tournament winner: `Lizzy B`
- Participants are seed Participants with `username = null`, `registrationStatus = ACCEPTED`.

### Tournament B — `Summer Chess Cup` (status: `ONGOING`)
8-player single-elimination bracket, partially played (round 1 complete, semifinals scheduled). Demonstrates the "tournament in progress" state.

- Players: `player1` … `player8` (linked by username; participant `name` = username)
- Round 1 winners decided; Round 2 fixtures populated with the winners but no result yet; Final has both participants TBD.
- All registrations are `ACCEPTED`.

### Tournament C — `Autumn Chess Open` (status: `OPEN_FOR_REGISTRATIONS`)
`maxParticipants = 16`. Demonstrates the registration workflow.

- 6 players already registered: `player9`..`player14` — first three `ACCEPTED`, next three `PENDING`.
- No fixtures yet (created when the tournament transitions to `ONGOING`).
