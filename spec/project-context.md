# Project Context

> High-level context for the project: the problem being solved, who it's for, what's in scope, and what constraints apply.

## 1. Vision

A tournament bracket management web application that allows hosts to create and manage tournament brackets and players or teams to sign up to tournaments and see their progress (or elimination) once games are played, all in a familiar and understandable bracket format. Tournament managers have their own admin view to create new tournaments, manage players/teams, and update results once a game has been completed. Players or teams can sign up for the tournament and view their placement and potential future opponents as the tournament proceeds. The goal is to have a self-hosted application that hobbyist tournament managers can easily run on their own hardware.   

## 2. Users


| Role                                                 | Description                                           | Capabilities                                                                                                                                                                  |
|------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Visitor** (unauthenticated)                        | Anyone visiting the site                              | View current tournament status                                                                                                                                                |
| **Player** (authenticated, role `USER`)              | Individual player or team representative              | Register for a tournament; manage player/team information; view current tournament status                                                                                     |
| **Tournament Manager** (authenticated, role `ADMIN`) | Tournament manager managing operations of Tournaments | Create, edit, update, or delete tournaments; view and accept or decline tournament registrations; Manage initial tournament game pairings; Enter official results after games |

## 3. Constraints

- **Database:** H2 (embedded, file-based in dev; in-memory for tests)
- **Authentication:** Spring Security — admin and user routes require login; public routes are open
- **Pre-seeded users for the demo:** one admin (`admin` / `admin`, role `ADMIN`) and twenty player accounts (`player1`/`player1` … `player20`/`player20`, role `USER`), defined in-memory so the demo can be exercised from multiple browser sessions without manual provisioning.
- **Limited participant number** — Minimum of 3 and maximum of 32 participants per tournament (single-admin demo scale; ~two dozen players in total).
- **Single elimination 2-competitor tournament only** Winner always progresses and loser is always eliminated
- **Chess** Start with a Chess tournament example for demo and testing
---

# Related Documents

- [Spec README](README.md) — process overview and workflow
- [Architecture](architecture.md) — technology stack and application structure
- [Design System](design-system.md) — theme, component usage, and visual standards
- [Use Case Template](use-cases/use-case-001-view-bracket.md) — template for feature specifications
- [Skills](skills/) — implementation, testing, and visual verification guides
