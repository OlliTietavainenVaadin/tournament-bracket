# Data Model

> Entity definitions and relationships. Evolves as features are added.

## Entities

| Entity          | Key Fields                                               | Relationships                               |
|-----------------|----------------------------------------------------------|---------------------------------------------|
| **Tournament**  | id, title, description, maxParticipants, winner | Has many Participants, has many Fixtures    |
| **Participant** | id, name, image                                          | Belongs to Tournament, belongs to Fixture   |
| **Fixture**     | id, playDate, participants, status, winner               | Belongs to Tournament, has two Participants |
                             |

## Entity Details

### Tournament
| Field           | Type        | Constraints |
|-----------------|-------------|-----------|
| id              | Long        | PK, auto-generated |
| title           | String      | Required, max 200 |
| description     | String      | Optional, max 2000 |
| startDate       | Date        | Optional |
| maxParticipants | Integer     | Required, > 0 |
| winner          | Participant | Optional |

### Participant
| Field | Type   | Constraints              |
|-------|--------|--------------------------|
| id    | Long   | PK, auto-generated       |
| name  | String | Required (e.g., "James") |
| image | String | Optional, filename       |

### Fixture
| Field        | Type        | Constraints                                           |
|--------------|-------------|-------------------------------------------------------|
| id           | Long        | PK, auto-generated                                    |
| playDate     | LocalDate   | Required, must be today or in the future when created |
| participant1 | Participant | Required (FK)                                         |
| participant2 | Participant | Optional                                              |
| winner       | Participant | Optional                                              |

## Sample Data

On startup, seed the database with a single tournament "chess tournament 1", assign player pairs randomly in four initial fixtures:

- **8 players**
    - "James"
    - "Tina"
    - "Lizzy B"
    - "Reindeer Rudolph"
    - "Benoit"
    - "Mary"
    - "Robot Rolf"
    - "Camper Vance"

