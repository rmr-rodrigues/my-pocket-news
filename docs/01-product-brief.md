# 01 — Product Brief

---

## Core Concept

My Pocket News is a personal Android app to:

> capture URL → extract article content → summarise via LLM → read later

Focused on:

- Frictionless capture via the Android Share menu from any browser or app
- Offline-readable summaries stored locally on the device
- Privacy-first: no cloud account required, the user brings their own LLM key

---

## Problem Statement

The problem is not the lack of read-later apps —
it is that existing apps store the full article without helping the user decide what is worth reading.

A user shares dozens of links per week. By the time they open the app, the context is lost and they must re-read the full article to decide if it is relevant. There is no quick way to triage saved content without consuming it entirely.

---

## Target Users

| User type | Role / Context | Technical level |
|---|---|---|
| Primary user | Individual who saves news articles and blog posts from mobile | Non-technical |
| Power user | Developer or researcher who wants to configure their own LLM provider and model | Technical |

---

## Target Use Cases

- Sharing a news article URL from Chrome/Firefox and receiving a summary notification minutes later
- Opening the app to browse a list of saved article summaries for quick triage
- Reading a full extracted article body without ads or paywalls (best-effort)
- Configuring a preferred LLM provider (e.g. OpenRouter, OpenAI) and API key in settings

---

## Value Proposition

Users get:

- Automatic summaries of saved articles without manual effort
- A personal news digest that respects their LLM provider choice
- All data stored on-device — no account, no subscription, no server
- Fast triage: know in 3 sentences whether an article is worth reading in full

---

## Key Differentiators

1. **Bring-your-own LLM** — works with any OpenAI-compatible provider; the user controls cost and model
2. **Local-only storage** — no cloud sync, no account, no data leaving the device except to the LLM API
3. **Background processing** — the summary is ready when the user opens the app; no waiting on demand

---

## Product Philosophy

- Capture must be zero-friction: the share action is the entire UI for saving
- The app is a reading assistant, not a social or collaborative tool
- Summaries are generated once and stored; no re-processing without explicit user action
- Simplicity over features: one entity (Article), one flow (share → summarise → read)

---

## Product Scope Boundaries

This product is not:

- A social or sharing platform — no user accounts, no public feeds
- A full-text RSS reader or podcast player
- A replacement for the original article — the source URL is always preserved
- A tool that renders JavaScript-heavy SPAs or bypasses paywalls programmatically

---

## Data Ownership and Retention

Core principle: **the data belongs to the user and never leaves the device except to the configured LLM API.**

- All articles and summaries are stored in a local Room database on the device
- The LLM API key is stored in Android EncryptedSharedPreferences, never transmitted elsewhere
- Uninstalling the app removes all data permanently — no cloud backup in v1

---

## Competitor / Market Context

| Alternative | Overlap | Why users would choose My Pocket News instead |
|---|---|---|
| Pocket / Instapaper | Save articles for later | My Pocket News adds automatic LLM summaries; no account needed |
| Readwise Reader | Save + highlight + AI summary | My Pocket News is free, local-only, and uses the user's own LLM key |
| Plain browser bookmarks | Save URLs | My Pocket News extracts and summarises content automatically |

**Market gap:** A local-only, account-free read-later app that automatically summarises articles using the user's own LLM provider.

---

## Open Questions

- [ ] Should the app support bulk re-summarisation (e.g. when the user changes LLM provider)?
- [ ] Should there be a maximum number of stored articles before older ones are purged?
