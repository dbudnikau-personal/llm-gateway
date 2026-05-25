# llm-gateway

A lightweight Anthropic-compatible API gateway that routes requests between a local LLM (via Ollama) and the real Anthropic API based on prompt complexity. Simple requests stay local — saving tokens and cost. Complex requests are forwarded to Claude.

## How it works

```
Client (Claude Code / any Anthropic SDK)
        │
        ▼
  llm-gateway :8080/v1/messages
        │
        ▼
  RoutingService
        │
        ├─ simple prompt ──► OllamaBackend → Ollama (local)
        │                                      └─ wraps response in Anthropic format
        │
        └─ complex prompt ──► AnthropicBackend → Anthropic API (Claude)
                                                    └─ response passed through
```

**Routing rules (heuristic-based, implemented in `KeywordComplexityAnalyzer`):**
- Prompt length > 500 chars → Claude
- Keywords detected (`explain`, `analyze`, `refactor`, `review`, `compare`, ...) → Claude
- `ANTHROPIC_API_KEY` not set → always local

## Architecture

The gateway is built around three SOLID-friendly abstractions:

- **`LlmBackend`** — interface implemented by `AnthropicBackend` and `OllamaBackend`. Adding a new provider means creating one new class; no existing code changes.
- **`RoutingService`** — iterates an ordered list of backends and delegates to the first one whose `canHandle()` returns true.
- **`ComplexityAnalyzer`** — interface for the routing signal. Current implementation: `KeywordComplexityAnalyzer` (length threshold + keyword matching). A future embedding-based classifier can be dropped in without touching the rest of the code.

## Prerequisites

- Java 21
- Maven 3.8+
- [Ollama](https://ollama.ai) running locally with at least one model pulled

```bash
ollama pull qwen2.5-coder:7b-instruct-q4_K_M
```

## Configuration

All configuration is in `src/main/resources/application.properties`. Secrets are provided via environment variables only — never hardcoded.

| Property | Env var | Default | Description |
|----------|---------|---------|-------------|
| `ollama.base-url` | — | `http://localhost:11434` | Ollama endpoint |
| `ollama.model.default` | — | `qwen2.5-coder:7b-instruct-q4_K_M` | Local model |
| `anthropic.api-key` | `ANTHROPIC_API_KEY` | _(empty)_ | Anthropic API key — if not set, all traffic goes local |
| `anthropic.model` | — | `claude-sonnet-4-6` | Claude model for complex requests |
| `anthropic.version` | — | `2023-06-01` | Anthropic API version header |
| `anthropic.max-tokens` | — | `8096` | Default `max_tokens` when not set by the client |
| `gateway.routing.complexity-threshold` | — | `500` | Prompt char length threshold for routing to Claude |
| `gateway.auth.token` | `GATEWAY_AUTH_TOKEN` | _(empty)_ | Bearer token to protect the gateway — leave empty to disable (warning logged on startup) |

## Running

### Maven

```bash
export ANTHROPIC_API_KEY=sk-ant-...   # optional — skip to run fully local

mvn spring-boot:run
```

### Docker

```bash
docker build -t llm-gateway .
docker run -p 8080:8080 -e ANTHROPIC_API_KEY=sk-ant-... llm-gateway
```

Gateway starts on port `8080`.

## Connecting Claude Code

Point Claude Code at the gateway instead of the Anthropic API:

```bash
ANTHROPIC_BASE_URL=http://localhost:8080 claude
```

Or set it permanently in your shell profile:

```bash
export ANTHROPIC_BASE_URL=http://localhost:8080
```

## Model aliases

The gateway maps friendly model names to local Ollama models:

| Requested model | Resolved to |
|-----------------|-------------|
| `coder` | `qwen2.5-coder:7b-instruct-q4_K_M` |
| `gemma4:31b-cloud` | `qwen2.5-coder:7b-instruct-q4_K_M` |
| _(anything else)_ | default model from config |

## Security

- The original `Authorization` header from incoming requests is **never forwarded** to Anthropic
- Only a strict whitelist of fields is forwarded: `messages`, `system`, `max_tokens`, `temperature`, `stop_sequences`, `top_p`, `top_k`, `metadata`, `stream`
- API key is injected by the gateway from its own environment — clients do not need a valid Anthropic key
- Request body is capped at 1 MB to prevent OOM attacks

## Roadmap

- [ ] Streaming responses (SSE)
- [ ] Embedding-based complexity classifier (drop-in `ComplexityAnalyzer` implementation)
- [ ] Metrics and routing observability (Micrometer + Prometheus)
- [ ] Tool calling support

## Status

Early-stage personal tool. Breaking changes possible.
