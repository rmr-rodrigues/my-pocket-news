# 02 — MVP Scope

<!-- This document defines the exact scope of the first shippable version.
     It covers which entities are modeled, what data is required,
     what blocks publication/completion, and what is only a warning.
     Be specific and opinionated. Vague scope leads to scope creep. -->

---

## Core MVP User Flow

<!-- The end-to-end journey a user takes in the MVP.
     Number the steps. Keep it to the happy path only. -->

1. [Step 1 — e.g. "Create or join an organization"]
2. [Step 2 — e.g. "Create a project"]
3. [Step 3 — e.g. "Import data"]
4. [Step 4 — e.g. "Visualize and correct"]
5. [Step 5 — e.g. "Add required metadata"]
6. [Step 6 — e.g. "Publish or export"]

---

## MVP Entities

<!-- For each entity in the domain, define its fields, GTFS/domain requirements,
     and whether missing data blocks completion or is just a warning.
     Add or remove entities as needed for this project. -->

### [Entity 1 — e.g. "Organization"]

<!-- One sentence describing what this entity represents. -->

| Field | Required by domain | Required to publish/complete | Notes |
|---|---|---|---|
| [field_name] | yes / no | yes / no | [e.g. "Blocks publication if missing"] |
| [field_name] | yes / no | yes / no | [e.g. "Warning if missing, defaults acceptable"] |
| [field_name] | yes / no | no | Optional |

Notes:

- [important constraint or business rule for this entity]
- [another constraint]

---

### [Entity 2 — e.g. "Stop / Asset / Item"]

| Field | Required by domain | Required to publish/complete | Notes |
|---|---|---|---|
| [field_name] | yes / no | yes / no | |
| [field_name] | yes / no | yes / no | |
| [field_name] | yes / no | no | Optional |

Notes:

- [constraint]

---

### [Entity 3 — e.g. "Route / Flow / Process"]

| Field | Required by domain | Required to publish/complete | Notes |
|---|---|---|---|
| [field_name] | yes / no | yes / no | |
| [field_name] | yes / no | yes / no | |

Notes:

- [constraint]

---

### [Entity 4 — add more as needed]

---

## What Is NOT in MVP Scope

<!-- Explicit list of features and capabilities that are intentionally excluded.
     This prevents "but can't we just add..." conversations. -->

The following are explicitly out of the first version:

- [excluded feature 1 — e.g. "fare rules and fare attributes"]
- [excluded feature 2]
- [excluded feature 3]
- [excluded feature 4]
- [excluded feature 5]

---

## Import / Input Formats — v1

<!-- What data formats does the product accept as input in the first version? -->

Supported:

| Format | Notes |
|---|---|
| [format 1 — e.g. CSV] | [e.g. flexible column mapping] |
| [format 2 — e.g. KML] | [e.g. extracts points and linestrings] |
| [format 3] | |

Not supported in v1 (explicitly deferred):

- [format A]
- [format B]

---

## Export / Output Formats — v1

<!-- What does the product produce as output? -->

Supported:

| Format | Notes |
|---|---|
| [format 1 — e.g. GTFS zip] | [e.g. hosted at stable URL] |
| [format 2 — e.g. GeoJSON] | [e.g. generated on demand] |

Not supported in v1:

- [format A]

---

## Validation Rules

<!-- Define which issues block the user from completing/publishing
     and which are warnings shown but do not block. -->

### Completion Blockers (hard errors)

These prevent the output from being generated or published:

- [blocker 1 — e.g. "Required field X missing on entity Y"]
- [blocker 2]
- [blocker 3]
- [blocker 4]

### Warnings (soft errors)

These are shown to the user but do not block completion:

- [warning 1 — e.g. "Entity exists but is not linked to any other entity"]
- [warning 2]
- [warning 3]

---

## Multi-Tenant and Organization Model

<!-- How are users, teams, and data organized?
     Skip or simplify this section if the product is single-user. -->

For v1:

- [tenant rule 1 — e.g. "Each user belongs to one organization"]
- [tenant rule 2 — e.g. "Each organization owns one or more projects"]
- [tenant rule 3 — e.g. "Users within an organization can view and edit all projects"]

Role model in v1:

| Role | Permissions |
|---|---|
| Admin | [e.g. manage org settings, invite users, publish] |
| Editor | [e.g. edit and validate, cannot publish] |
| Viewer | [e.g. read-only access] |

---

## Versioning and Publication Model

<!-- How does the product handle versions of the output?
     What happens when the user publishes again? -->

**This section may be TBD.** Document known requirements and open questions.

### Known requirements

- [requirement 1 — e.g. "Published output must be available at a stable URL"]
- [requirement 2 — e.g. "Client controls when to publish"]
- [requirement 3]

### Scenarios to support

1. [scenario 1 — e.g. "Simple correction: publish, find error, correct, republish"]
2. [scenario 2 — e.g. "Seasonal versions: activate a saved snapshot"]
3. [scenario 3]

### Open questions

- [question 1 — e.g. "Should the platform retain named snapshots?"]
- [question 2]

### Interim decision

[State the simplest model that works for v1 and defer the rest.]

---

## Open Questions

<!-- Unresolved MVP-level questions. Remove once resolved. -->

- [ ] [open question 1]
- [ ] [open question 2]
