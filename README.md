# project-spec-template

> A reusable project specification template for spec-driven development with LLMs.

---

## What This Is

This repository is a **GitHub Template** for starting new software projects with a consistent, navigable spec structure.

It provides a standard set of documents that:

- any LLM coding agent can pick up and understand immediately, without being re-briefed
- any human can navigate from `README.md` to any detail in the project
- keep progress always visible and up to date
- have consistent names, sections, and order across all projects

---

## How to Use

### Option A — GitHub UI (recommended)

Click the **"Use this template"** button at the top of this repository on GitHub.
This creates a new repository with the full structure and no git history from this template.

### Option B — GitHub CLI

```bash
gh repo create [new-project-name] --template rmr-rodrigues/project-spec-template --private
```

### Option C — Manual clone

```bash
git clone https://github.com/rmr-rodrigues/project-spec-template [new-project-name]
cd [new-project-name]
rm -rf .git
git init
git add .
git commit -m "init: project spec from template"
```

After creating the project, follow the step-by-step workflow in **[TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)**.

---

## Repository Structure

```
project/
├── README.md                        ← Hub: one-liner, status, quick links, key decisions
├── AGENTS.md                        ← LLM agent instructions: stack, commands, style, workflow
├── TODO.md                          ← Live progress tracker with checkboxes
├── TEMPLATE_GUIDE.md                ← How to fill in this template (read first, then delete or keep)
│
└── docs/
    ├── 01-product-brief.md          ← Vision, problem, target users, value proposition
    ├── 02-mvp-scope.md              ← Entities, in/out of scope, validation rules
    ├── 03-technical-architecture.md ← Stack, infrastructure, key decisions
    ├── 04-implementation-phases.md  ← Phase summaries with goals and definitions of done
    └── phases/
        └── phase-01.md              ← Full task breakdown for Phase 1 (generated on-demand)
```

**File names are fixed.** Do not rename them — consistency across projects is what allows agents to navigate any project without project-specific instructions.

---

## Document Roles

| File | Purpose | Updated when |
|---|---|---|
| `README.md` | Navigation hub for the project | Phase changes, key decisions |
| `AGENTS.md` | LLM agent context: stack, commands, code style, domain vocabulary | Stack confirmed, commands added |
| `TODO.md` | Live task checklist | Tasks completed, phases advance |
| `docs/01-product-brief.md` | Vision, problem, users, differentiators | Rarely — only if product direction changes |
| `docs/02-mvp-scope.md` | MVP entities, validation rules, in/out of scope | Scope decisions made |
| `docs/03-technical-architecture.md` | Stack decisions, infrastructure, deployment | Tech decisions made |
| `docs/04-implementation-phases.md` | Phase plan with goals and definitions of done | Phase scope changes |
| `docs/phases/phase-XX.md` | Executable task breakdown for one phase | Generated on-demand; should not change once started |

---

## Workflow Overview

```
1. Brainstorm idea with LLM
           ↓
2. Fill in docs/01-product-brief.md
           ↓
3. Fill in docs/02-mvp-scope.md
           ↓
4. Fill in docs/03-technical-architecture.md
           ↓
5. Fill in docs/04-implementation-phases.md  (phase summaries only)
           ↓
6. Update README.md, AGENTS.md, TODO.md
           ↓
7. Generate docs/phases/phase-01.md  (when ready to implement)
           ↓
8. Execute with LLM coding agent, marking TODO.md as tasks complete
           ↓
9. Generate docs/phases/phase-02.md  (when Phase 1 is complete)
           ↓
          ...
```

Full step-by-step instructions, including LLM prompts for each step, are in **[TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)**.

---

## Supported Project Types

This template is universal. It works for:

- Web apps (frontend only)
- Mobile apps (Android / iOS)
- APIs / Backend services
- Full-stack (web + backend)
- Monorepo multi-app projects
- CLI tools and scripts

See the **Adapting for Different Project Types** section in `TEMPLATE_GUIDE.md` for guidance on what to include or omit per project type.

---

## Project Template for New Projects

The `README.md` that belongs in each new project (with placeholders) is embedded inside `docs/` and `TEMPLATE_GUIDE.md`. When you create a new project from this template, the first step is to replace the content of `README.md` with the project-specific version described in `TEMPLATE_GUIDE.md`.
