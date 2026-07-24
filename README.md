# DigitalOcean URL Audit Tool

A small Spring Boot app that fetches a URL and returns a quick on-page SEO/health audit: HTTP status, response time, page title, meta description, H1 count, images missing `alt` text, and word count.

Built for Digital Heroes Training Task.

---

## Setup

**Requirements:** Java 21, Maven (or use the bundled wrapper — no local Maven install needed).

### Run locally

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

### Run with Docker

```bash
docker build -t digitalocean-audit .
docker run -p 8080:8080 digitalocean-audit
```

### Run the tests

```bash
./mvnw test
```

---

## API Contract

### `GET /`
Renders the UI (Thymeleaf `index` template) — the form for submitting a URL to audit.

### `GET /404`
Renders a custom "not found" page (Thymeleaf `404` template).

### `GET /test`
Health check.

**Response:** `200 OK`, plain text `Hello World`.

### `POST /audit`

Audits the given URL and returns page-level metrics.

**Request:**

| Param | Type | Location | Required | Example |
|-------|------|----------|----------|---------|
| `url` | string | query/form param | yes | `https://example.com` |

```
POST /audit?url=https://example.com
```

**Success response — `200 OK`:**

```json
{
  "status": 200,
  "responseTime": 184,
  "metaDescription": "Example description text",
  "pageTitle": "Example Domain",
  "h1Count": 1,
  "missingAltImages": 2,
  "wordCount": 43
}
```

| Field | Meaning |
|---|---|
| `status` | HTTP status code returned by the **audited** page (not this API) |
| `responseTime` | Time (ms) it took to fetch the audited page |
| `metaDescription` | Content of `<meta name="description">`, or `""` if absent |
| `pageTitle` | `<title>` text |
| `h1Count` | Number of `<h1>` elements |
| `missingAltImages` | Number of `<img>` tags with no `alt` attribute or an empty one |
| `wordCount` | Word count of the visible body text |

**Error responses:**

| Status | Condition | Body |
|---|---|---|
| `400 Bad Request` | URL is malformed / not a valid URI, or the host can't be resolved | Plain-text message, e.g. `"Invalid URL. Example: https://www.example.com"`, `"Website not found."` |
| `408 Request Timeout` | The target site didn't respond within the timeout window | `"Website took too long to respond."` |
| `500 Internal Server Error` | Unexpected I/O error, or any other unhandled failure while fetching/parsing | `"unable to parse the webpage"` / `"something is wrong with the given url"` |

**Important quirk:** if the *audited* page itself returns an error (say, `403` or `404`), this API still responds with `200 OK`, and that status code is embedded in the response body's `status` field (with the other fields left at their defaults). See Design Decision #1 below for why.

---

## Design Decisions

### 1. Target-site errors are returned as `200 OK` with the status embedded in the body, not propagated as HTTP errors

If `audit()` fetches a page that responds with, say, `403 Forbidden`, the endpoint still returns `200 OK` with `{"status": 403, ...}`, rather than making the *whole API call* fail with a `403`.

**Reasoning:** There's a meaningful difference between "our service broke" and "the page you asked about is broken/blocked." A `403` or `404` from the target site is a valid, useful *audit finding*, not a failure of this tool. Treating it as an HTTP error on our own endpoint would make it indistinguishable from a real failure of the audit service itself, and would force the client to special-case status codes that actually belong to someone else's website.

*Trade-off:* this means callers can't rely on HTTP status codes alone to know whether the audit "succeeded" — they need to inspect the JSON body's `status` field too. That inconsistency is worth flagging and could be cleaned up (e.g., a dedicated `success` boolean).

### 2. Exceptions are caught by specific type and mapped to distinct HTTP codes, instead of one catch-all

`MalformedURLException`, `UnknownHostException`, `SocketTimeoutException`, generic `IOException`, and a final catch-all `Exception` are each caught separately and mapped to different status codes (`400`, `400`, `408`, `500`, `500`) with different, specific messages.

**Reasoning:** A generic `try { ... } catch (Exception e) { return 500 }` would work, but it gives every caller the same unhelpful "something went wrong" for very different problems — a typo'd URL, a site that's genuinely down, and a site that's just slow are different situations that call for different client-side handling (fix your input vs. retry later vs. give up). Splitting them out costs a bit of extra code but makes the API much more actionable to build against.

### 3. Tests use a local embedded HTTP server instead of live third-party sites

The original test scaffold called out to `example.com` and `httpstat.us` directly. The rewritten tests spin up a throwaway `com.sun.net.httpserver.HttpServer` (built into the JDK — no extra dependency) that serves controlled HTML, and assert against it.

**Reasoning:** Tests that depend on the internet and on someone else's uptime are flaky by construction — they can fail because *your* code is fine but the third-party site is slow, down, rate-limiting, or has changed its markup. A local server that returns exact, known HTML lets the tests actually verify the parsing logic (H1 counting, alt-attribute detection, word counting, meta description extraction) deterministically, in milliseconds, with no network dependency. The one exception is the "host not found" test, which still needs a real DNS lookup to trigger the actual `UnknownHostException` path.

---

## Known limitations / what I'd change given another day

- `audit()` does real network I/O directly inside the service method with no seam for mocking the HTTP layer (jsoup's `Jsoup.connect(...)` is called inline). It works, but it means every test either has to hit the network or spin up a real server, as above. Extracting an interface around the fetch step (e.g. a `PageFetcher`) would let pure-parsing tests run against an in-memory `Document` with zero I/O at all.
- The `200`-with-embedded-error-status behavior (Design Decision #1) is easy to misuse from a client if not documented — it's called out above, but a `success`/`ok` boolean in the response body would make it self-evident from the JSON alone.

---

## Live Build

**Built for Digital Heroes Training Task**

Live URL: [click here](https://digitalocean-w6ae.onrender.com/)
