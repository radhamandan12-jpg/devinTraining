# Spring AI Claude Demo

Minimal sample app: Spring AI `ChatClient` calling Claude Sonnet (Anthropic),
with a Thymeleaf chat UI and per-session conversation memory.

## Run

```bash
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
```

Open http://localhost:8082 — type a message, Claude replies, history is kept
per browser session (in-memory, last 20 messages). "New conversation" clears it.

## How it works

- `spring-ai-starter-model-anthropic` auto-configures a `ChatClient.Builder`
  backed by `AnthropicChatModel` from `spring.ai.anthropic.*` in `application.yml`.
- `ChatController` builds a `ChatClient` with a `MessageChatMemoryAdvisor`,
  so prior turns are sent to the model automatically (keyed by a conversation
  id stored in the `HttpSession`).
- `index.html` renders the chat history (`chatMemory.get(conversationId)`)
  and the prompt form.

## ChatModel demo (`/chat-model`)

The chat page uses the high-level `ChatClient`. `/chat-model` shows the
lower-level `ChatModel` API instead: the controller builds a `Prompt`
(`SystemMessage` + `UserMessage` + `AnthropicChatOptions` for
temperature/max-tokens) and calls `chatModel.call(...)`, then renders the
`ChatResponse` — reply text plus token usage and model name from the metadata.

## PromptTemplate demo (`/prompt-template`)

`/prompt-template` demonstrates `PromptTemplate`: write a template with
`{placeholders}`, supply `key=value` variables, and the page shows the
rendered prompt (`promptTemplate.create(vars)` → `Prompt`) alongside
Claude's response via `chatClient.prompt(prompt)`.

## Tool calling demo (`/tools`)

`/tools` registers two `@Tool` classes on the `ChatClient` — Claude decides
when to invoke them:

- `DbTools` — JdbcTemplate against MySQL (`claude_demo.notes`, auto-created
  by `schema.sql`): `saveNote`, `listNotes`, `searchNotes`.
- `ApiTools` — `RestClient` against the JSONPlaceholder REST API:
  `fetchPost`, `fetchUser`, `fetchTodos`.

The page also renders the current contents of the `notes` table.

## MCP client demo (`/mcp`)

`/mcp` uses `spring-ai-starter-mcp-client` — the app acts as an MCP client,
spawning two stdio servers configured in `src/main/resources/mcp-servers.json`:

- **filesystem** (`mcp-server-filesystem`, npm) scoped to the workspace dir
- **github** (`github-mcp-server` binary) — needs
  `GITHUB_PERSONAL_ACCESS_TOKEN` in the environment

`ChatClient` is built with `.defaultToolCallbacks(mcpToolCallbacks)` so every
MCP tool is available to Claude. Run with Node on PATH, e.g.:

```bash
PATH=/home/ubuntu/nodejs/bin:$PATH \
GITHUB_PERSONAL_ACCESS_TOKEN=ghp_... \
ANTHROPIC_API_KEY=... ANTHROPIC_BASE_URL=... ANTHROPIC_MODEL=... \
java -jar target/spring-ai-claude-demo-1.0.0.jar
```

## RAG demo (`/rag`)

A second page at http://localhost:8082/rag demonstrates embeddings +
cosine similarity:

- Documents are embedded with a **local ONNX embedding model**
  (`spring-ai-starter-model-transformers`, all-MiniLM-L6-v2, 384 dims —
  no API key needed) and kept in an in-memory `EmbeddingStore`.
- `RagController` embeds the query, scores every document with
  `CosineSimilarity.of()`, and shows the top 3 matches with similarity %.
- Optionally, Claude answers the question using the retrieved docs as context
  (the "generate an answer" checkbox) — a minimal retrieve-then-generate RAG.

## Config

| Property | Purpose |
|---|---|
| `ANTHROPIC_API_KEY` (env) | Required. Anthropic API key. |
| `ANTHROPIC_BASE_URL` (env) | Optional API endpoint override, e.g. an LLM gateway (default `https://api.anthropic.com`) |
| `ANTHROPIC_MODEL` (env) | Model id (default `claude-sonnet-4-5`) |
| `spring.ai.anthropic.chat.options.max-tokens` | Max response tokens |
| `spring.ai.anthropic.chat.options.temperature` | Sampling temperature |

### Behind an LLM gateway

```bash
ANTHROPIC_API_KEY="$CLAUDE_API_KEY" \
ANTHROPIC_BASE_URL="$LLM_BASE_URL" \
ANTHROPIC_MODEL="global.anthropic.claude-sonnet-4-6" \
mvn spring-boot:run
```

Stack: Java 21 · Spring Boot 3.5 · Spring AI 1.1 · Thymeleaf · Maven
