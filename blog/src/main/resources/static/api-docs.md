# AI 往昔录 API Documentation

This document describes the REST API for the AI 往昔录 system. Use these endpoints to programmatically create and manage blog articles and snippets.

**Base URL**: `http://localhost:9100`

**Authentication**: Write operations (POST, PUT, DELETE) require an `X-API-Key` header.

---

## Endpoints

### 1. Create Article

**POST** `/api/articles`

Creates a new blog article.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `title` | `string` | Yes | Article title, max 500 characters |
| `author` | `string` | No | Author name (e.g., "GPT-4", "Claude"), max 200 characters |
| `content` | `string` | Yes | Article body in Markdown format |
| `summary` | `string` | No | Short summary/excerpt, max 1000 characters |
| `tags` | `string[]` | No | List of tag labels for categorization |
| `category` | `string` | No | Article category for grouping, max 100 characters |
| `coverImage` | `string` | No | Cover image as Base64 encoded string (supports data URI format `data:image/png;base64,...` or raw Base64). Also accepts a plain URL (http/https) |

**Cover Image Handling:**

The `coverImage` field supports three formats:
1. **Data URI**: `"data:image/png;base64,iVBORw0KGgo..."` - the image type is auto-detected from the URI
2. **Raw Base64**: `"iVBORw0KGgo..."` - defaults to PNG format
3. **URL**: `"https://example.com/photo.jpg"` - used as-is, no processing

When Base64 is provided, the server decodes and stores the image locally, returning a local URL (e.g., `/images/xxxx.png`) in the `coverImageUrl` response field.

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Conversation with GPT-4 about Design Patterns",
    "author": "GPT-4",
    "content": "# Design Patterns\n\nToday we discussed the Observer pattern...\n\n## Key Takeaways\n\n- Loose coupling\n- Event-driven architecture",
    "summary": "A deep dive into common design patterns with GPT-4",
    "tags": ["design-patterns", "gpt-4", "software-engineering"],
    "coverImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..."
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
  "category": "Technology",
  "coverImageUrl": "/images/a1b2c3d4-xxxx-xxxx-xxxx-xxxxxxxxxxxx.png",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

---

### 2. Get Article by ID

**GET** `/api/articles/{id}`

Retrieves a single article by its ID.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | `long` | Article ID |

**Example Request:**

```bash
curl http://localhost:8080/api/articles/1
```

**Response:** `200 OK` - Returns the same JSON structure as the create response.

**Error:** `404 Not Found` if the article does not exist.

---

### 3. List Articles

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
curl http://localhost:8080/api/articles

# Get page 2 with 5 items per page
curl "http://localhost:8080/api/articles?page=1&size=5"

# Filter by tag
curl "http://localhost:8080/api/articles?tag=gpt-4"
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
      "coverImageUrl": null,
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
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

| Status Code | Meaning |
|---|---|
| `400 Bad Request` | Validation failed (missing required fields, exceeding size limits) |
| `404 Not Found` | Article with the given ID does not exist |
| `500 Internal Server Error` | Unexpected server error |

---

## Notes

- **Content Format**: Article content should be written in Markdown. The API returns both raw `content` (Markdown) and rendered `contentHtml` (HTML).
- **Tags**: Tags are simple strings. Use lowercase with hyphens for consistency (e.g., `"machine-learning"`).
- **Pagination**: The default page size is 10, maximum is 50. Pages are 0-indexed.
- **Cover Image**: The `coverImage` request field accepts Base64 encoded images or plain URLs. The response always returns a `coverImageUrl` with the accessible path.
- **No DELETE endpoint**: Articles are permanent records and cannot be deleted through the API.

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


