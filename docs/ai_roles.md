# AI Roles

## Use This File For

- Choosing the right Copilot model for a task
- Explaining how model depth should map to task depth
- Answering instruction questions about model behavior

Read this after `docs/index.md` when the task is about models, reasoning depth, prompt strategy, or Copilot behavior.

## Decision Order

Andrea is the final decision maker.

Copilot models support implementation, review, and reasoning.
They do not define product direction on their own.

## Fast In-Editor Models

Use for:

- Small local edits
- Short completions
- Repetitive snippets
- Inline suggestions

Relevant models from `docs/copilot-models.json`:

- GPT-3.5 Turbo
- GPT-5 mini
- Claude Haiku 4.5
- Gemini Flash 2.0
- Grok Code Fast 1

Rules:

- Keep the task narrow
- Do not trust these models with architecture decisions by default
- Prefer them when speed matters more than deep reasoning

## Balanced Working Models

Use for:

- Daily coding
- PR suggestions
- Test and documentation generation
- Mid-level debugging

Relevant models from `docs/copilot-models.json`:

- GPT-4.1
- GPT-4.5
- Claude Sonnet 4.5
- OpenAI Codex

Rules:

- These are the default models for most repository work
- They should still follow the project constraints strictly
- They should not redesign architecture unless explicitly asked

## Deep Reasoning Models

Use for:

- Repo-wide analysis
- Cross-file refactors
- Architecture review
- Complex debugging
- High-stakes review

Relevant models from `docs/copilot-models.json`:

- GPT-5
- Gemini Pro 2.5
- Claude Opus 4.6

Rules:

- Use when the task genuinely needs more context or stronger reasoning
- Claude Opus 4.6 should be used sparingly because it is in the elevated quota category
- Deep reasoning is not permission to expand scope

## Multimodal Model

Use for:

- UI debugging from screenshots
- Visual review
- Multimodal documentation

Relevant model from `docs/copilot-models.json`:

- GPT-4o (multimodal)

Rules:

- Use only when image input materially helps
- Do not route ordinary text-only coding tasks here by default

## Shared Behavioral Rules

- No feature creep
- No unnecessary abstraction
- Prefer deletion over addition
- Prefer explicit code over generic systems
- If a choice expands scope, reject it by default
- If a request is unclear, ask Andrea instead of guessing
