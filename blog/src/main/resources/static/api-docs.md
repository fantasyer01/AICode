# AI 往昔录 API Documentation

This document describes the REST API for the AI 往昔录 system. Use these endpoints to programmatically create and manage blog articles and snippets.

**Base URL**: `http://localhost:9100`

**Authentication**: All write operations (POST, PUT, PATCH) require an `X-API-Key` header.

---

## Image Endpoints

### 1. Upload Image

**POST** `/api/images/upload`

Uploads an image file and returns its publicly accessible URL. This is the **single entry point** for all image uploads — use the returned URL in article `coverImageUrl` or inline in Markdown content.

**Request Headers:**
- `Content-Type: multipart/form-data`
- `X-API-Key: <your-api-key>` (required)

**Request Body (multipart form fields):**

| Field | Type | Required | Description |
|---|---|---|---|
| `file` | file | Yes | Image file. Supported MIME types: `image/png`, `image/jpeg`, `image/gif`, `image/webp`, `image/svg+xml`. Max size: **3 MB** |

**Response:** `200 OK`

```json
{ "url": "/images/550e8400-e29b-41d4-a716-446655440000.png" }
```

**Example Request:**

```bash
curl -X POST http://localhost:9100/api/images/upload \
  -H "X-API-Key: your-api-key" \
  -F "file=@/path/to/image.png"
```

PowerShell:

```powershell
$form = @{ file = Get-Item "C:\images\photo.png" }
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:9100/api/images/upload" `
  -Headers @{ "X-API-Key" = $env:BLOG_API_KEY } `
  -Form $form
```

**Error Responses:**

| Status | Reason |
|---|---|
| `400 Bad Request` | File is empty or not an allowed image type |
| `401 Unauthorized` | Missing or invalid `X-API-Key` |
| `413 Payload Too Large` | File exceeds 3 MB limit |

---

## Article Endpoints

### 2. Create Article

**POST** `/api/articles`

Creates a new blog article.

**Request Headers:**
- `Content-Type: application/json`
- `X-API-Key: <your-api-key>` (required)

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `title` | `string` | Yes | Article title, max 500 characters |
| `author` | `string` | No | Author name (e.g., "GPT-4", "Claude"), max 200 characters |
| `content` | `string` | Yes | Article body in Markdown format. Inline images use standard Markdown syntax: `![alt](url)` |
| `summary` | `string` | No | Short summary/excerpt, max 1000 characters |
| `tags` | `string[]` | No | List of tag labels for categorization |
| `category` | `string` | No | Article category for grouping, max 100 characters |
| `coverImageUrl` | `string` | No | Cover image URL. Must start with `/`, `http://`, or `https://`. Upload the image first via `POST /api/images/upload` |
| `published` | `boolean` | No | Publication status, defaults to `true` |

**Example Request:**

```bash
curl -X POST http://localhost:9100/api/articles \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "title": "My Conversation with GPT-4 about Design Patterns",
    "author": "GPT-4",
    "content": "# Design Patterns\n\nToday we discussed the Observer pattern.\n\n![Architecture Diagram](/images/abc123.png)\n\n## Key Takeaways\n\n- Loose coupling\n- Event-driven architecture",
    "summary": "A deep dive into common design patterns with GPT-4",
    "tags": ["design-patterns", "gpt-4", "software-engineering"],
    "coverImageUrl": "/images/cover-uuid.png"
  }'
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "title": "My Conversation with GPT-4 about Design Patterns",
  "author": "GPT-4",
  "content": "# Design Patterns\n\nToday we discussed...",
  "contentHtml": "<h1>Design Patterns</h1>\n<p>Today we discussed...</p>",
  "summary": "A deep dive into common design patterns with GPT-4",
  "tags": ["design-patterns", "gpt-4", "software-engineering"],
  "category": null,
  "coverImageUrl": "/images/cover-uuid.png",
  "published": true,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

---

### 3. Get Article by ID

**GET** `/api/articles/{id}`

Retrieves a single article by its ID.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | `long` | Article ID |

**Example Request:**

```bash
curl http://localhost:9100/api/articles/1
```

**Response:** `200 OK` — Returns the same JSON structure as the create response.

**Error:** `404 Not Found` if the article does not exist.

---

### 4. List Articles

**GET** `/api/articles`

Returns a paginated list of articles, ordered by creation date (newest first).

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | `int` | `0` | Page number (0-based) |
| `size` | `int` | `10` | Page size (max 50) |
| `tag` | `string` | - | Filter by tag (optional) |
| `category` | `string` | - | Filter by category (optional) |

**Example Requests:**

```bash
# Get first page
curl http://localhost:9100/api/articles

# Filter by tag
curl "http://localhost:9100/api/articles?tag=gpt-4"
```

**Response:** `200 OK`

```json
{
  "content": [
    {
      "id": 2,
      "title": "Article Title",
      "author": "Claude",
      "content": "...",
      "contentHtml": "...",
      "summary": "...",
      "tags": ["ai"],
      "coverImageUrl": "/images/abc.png",
      "createdAt": "2025-01-16T09:00:00",
      "updatedAt": "2025-01-16T09:00:00"
    }
  ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

---

### 5. Update Article

**PUT** `/api/articles/{id}`

Updates an existing article. All fields are optional; only provided fields are updated.

**Request Headers:**
- `Content-Type: application/json`
- `X-API-Key: <your-api-key>` (required)

**Request Body:** Same optional fields as Create Article (`title`, `author`, `content`, `summary`, `tags`, `category`, `coverImageUrl`, `published`).

**Response:** `200 OK` — Returns the updated article.

---

### 6. Patch Article Images

**PATCH** `/api/articles/{id}/images`

Patches image references on an existing article without replacing other fields. Supports two independent operations in a single call:

1. **Replace cover image** — provide `coverImageUrl`
2. **Replace content placeholders** — provide `contentReplacements` (a map of placeholder string → replacement value)

Both fields are optional, but at least one must be provided.

**Request Headers:**
- `Content-Type: application/json`
- `X-API-Key: <your-api-key>` (required)

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | `long` | Article ID |

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `coverImageUrl` | `string` | No | New cover image URL. Replaces existing cover unconditionally |
| `contentReplacements` | `object` | No | Map of placeholder → replacement value. Each key found in the article's Markdown body is replaced with its value. Supports any number of entries. Keys absent from the content are silently ignored |

**Example Request:**

```bash
curl -X PATCH http://localhost:9100/api/articles/42/images \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "coverImageUrl": "/images/cover-new.png",
    "contentReplacements": {
      "{{arch_diagram}}": "![架构图](/images/arch.png)",
      "{{seq_diagram}}": "![时序图](/images/seq.png)",
      "{{screenshot}}":  "![截图](/images/shot.png)"
    }
  }'
```

**Response:** `200 OK` — Returns the full updated article.

**Error Responses:**

| Status | Reason |
|---|---|
| `400 Bad Request` | Both `coverImageUrl` and `contentReplacements` are null/empty |
| `401 Unauthorized` | Missing or invalid `X-API-Key` |
| `404 Not Found` | Article with the given `id` does not exist |

**Typical full workflow:**

```bash
# Step 1: Upload images
COVER=$(curl -s -X POST http://localhost:9100/api/images/upload \
  -H "X-API-Key: $KEY" -F "file=@cover.png" | jq -r .url)

IMG1=$(curl -s -X POST http://localhost:9100/api/images/upload \
  -H "X-API-Key: $KEY" -F "file=@diagram.png" | jq -r .url)

# Step 2: Create article with placeholder in content
ARTICLE_ID=$(curl -s -X POST http://localhost:9100/api/articles \
  -H "Content-Type: application/json" -H "X-API-Key: $KEY" \
  -d "{\"title\":\"My Article\",\"content\":\"## Diagram\n\n{{diagram}}\n\nText here.\",\"coverImageUrl\":\"$COVER\"}" \
  | jq -r .id)

# Step 3: Patch inline image into content
curl -X PATCH "http://localhost:9100/api/articles/$ARTICLE_ID/images" \
  -H "Content-Type: application/json" -H "X-API-Key: $KEY" \
  -d "{\"contentReplacements\":{\"{{diagram}}\":\"![diagram]($IMG1)\"}}"
```

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/articles",
  "fieldErrors": [
    {
      "field": "coverImageUrl",
      "message": "coverImageUrl must be a URL starting with '/', 'http://', or 'https://'. Upload the image first via POST /api/images/upload"
    }
  ]
}
```

| Status Code | Meaning |
|---|---|
| `400 Bad Request` | Validation failed (missing required fields, invalid format) |
| `401 Unauthorized` | Missing or invalid `X-API-Key` |
| `404 Not Found` | Resource with the given ID does not exist |
| `413 Payload Too Large` | Uploaded file exceeds size limit |
| `500 Internal Server Error` | Unexpected server error |

---

## Notes

- **Content Format**: Article content is Markdown. The API returns both raw `content` (Markdown) and rendered `contentHtml` (HTML).
- **Inline Images**: Use standard Markdown syntax `![alt text](url)` in the `content` field. Upload images first via `POST /api/images/upload` to get their URLs.
- **Cover Image**: Upload the image via `POST /api/images/upload`, then set the returned URL as `coverImageUrl` in Create or Update requests, or patch it later via `PATCH /api/articles/{id}/images`.
- **Tags**: Tags are simple strings. Use lowercase with hyphens for consistency (e.g., `"machine-learning"`).
- **Pagination**: The default page size is 10, maximum is 50. Pages are 0-indexed.

---

## Snippets API

Snippets are short reading notes that are automatically processed by AI (DeepSeek LLM) to generate a title, structured content, and tags.

### Create Snippet

**POST** `/api/snippets`

Creates a new snippet. The raw content is automatically processed by AI to generate a title, structured content, and 1-2 topic tags. Processing happens synchronously - the response includes the processed result.

**Request Headers:**
- `Content-Type: application/json`
- `X-API-Key: <your-api-key>` (required)

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `rawContent` | `string` | Yes | The raw reading note text to be processed |
| `author` | `string` | No | Author name, max 200 characters |

**Example Request:**

```bash
curl -X POST http://localhost:9100/api/snippets \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "rawContent": "Today I read about how Redis uses single-threaded event loop for I/O multiplexing. The key insight is that most operations are memory-based so CPU is rarely the bottleneck. Network I/O is the real bottleneck and epoll handles that efficiently.",
    "author": "John"
  }'
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "rawContent": "Today I read about how Redis uses single-threaded...",
  "processedTitle": "Redis Single-Threaded Architecture",
  "processedContent": "## Redis I/O Model\n\nRedis employs a single-threaded event loop...",
  "displayTitle": "Redis Single-Threaded Architecture",
  "displayContent": "## Redis I/O Model\n\nRedis employs a single-threaded event loop...",
  "displayContentHtml": "<h2>Redis I/O Model</h2>\n<p>Redis employs a single-threaded event loop...</p>",
  "tags": ["redis", "system-design"],
  "author": "John",
  "status": "PROCESSED",
  "createdAt": "2026-03-14T14:30:00",
  "updatedAt": "2026-03-14T14:30:00"
}
```

**Status Field Values:**

| Status | Meaning |
|---|---|
| `PROCESSED` | AI successfully generated title, content, and tags |
| `PENDING` | AI processing has not started (rare in normal flow) |
| `FAILED` | AI processing failed. `displayContent` falls back to `rawContent` |

When `status` is `FAILED`, the snippet is still saved. The `displayTitle` will be `null` and `displayContent` will contain the original `rawContent`. The `tags` list will be empty.
