# Template Guide

<!-- This file explains how to set up a new project from this template.
     It is for human use during initial project setup.
     Agents read AGENTS.md — this file is not part of the project spec. -->

---

## What This Template Is

A **project spec template** for spec-driven development with LLMs. It provides a consistent,
navigable structure so any LLM coding agent can pick up context immediately without being
re-briefed, and progress is always visible.

---

## Step 1 — Create the repository

```bash
# GitHub "Use this template" button (recommended)
# Or via CLI:
gh repo create [new-project-name] --template rmr-rodrigues/project-spec-template --private
```

---

## Step 2 — Fill in the product brief

Use a conversational LLM with this prompt:

```
Ask me one question at a time so we can develop a thorough, step-by-step spec
for this idea. Each question should build on my previous answers.
Our goal is a detailed specification I can hand off to a developer.
Only one question at a time.

Here's the idea: [YOUR IDEA]
```

When the brainstorm concludes:

```
Compile our findings into a complete product brief following this structure:
[paste the sections from docs/01-product-brief.md]
```

---

## Step 3 — Define MVP scope, architecture, and phases

Repeat the same pattern for each doc, passing the previous docs as context:

- `docs/02-mvp-scope.md` — entities, in/out of scope, validation rules
- `docs/03-technical-architecture.md` — stack, hosting, key decisions with justification
- `docs/04-implementation-phases.md` — phases that build on each other; each phase produces something testable

---

## Step 4 — Update the root files

- **`README.md`** — replace template content with project-specific version; set phase status to `Not Started`
- **`AGENTS.md`** — fill in Project Type, Tech Stack, Domain Vocabulary; update Repository State
- **`TODO.md`** — set current phase name; add rows for each phase from `04-implementation-phases.md`
- **`TEMPLATE_GUIDE.md`** — delete this file from project repos (keep only in `project-spec-template`)

---

## Step 5 — Generate Phase 1 task breakdown

Only when ready to implement. Use a reasoning model (`o3`, `claude-3.7-sonnet`):

```
Based on the full spec below, generate a complete, executable task breakdown
for Phase 1. Include reference implementations for each task.
Each task must have a verification step. Follow the structure in
docs/phases/phase-01/phase-01.md.

[paste all four docs]
```

Save as `docs/phases/phase-01/phase-01.md`.
Create `docs/phases/phase-01/todo-01.md` from the task list in that document.
Update `TODO.md` root index with a link to `todo-01.md`.

---

## Step 6 — Execute

1. Point the agent to `AGENTS.md` and `docs/phases/phase-01/phase-01.md`
2. Execute tasks in order, group by group
3. Mark tasks complete in `todo-01.md` as you go
4. Run the phase verification checklist at the end

When a phase is complete: update `README.md`, generate the next phase's files, repeat.

---

## Keeping the Template Updated

When you improve this workflow:

1. Update the relevant file in `project-spec-template`
2. Manually apply the improvement to existing projects if beneficial

The template evolves based on what works. It is not frozen.
