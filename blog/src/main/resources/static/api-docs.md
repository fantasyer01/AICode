# AI Blog API Documentation

This document describes the REST API for the AI Blog system. Use these endpoints to programmatically create and manage blog articles.

**Base URL**: `http://localhost:8080`

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

### 3. Update Article

**PUT** `/api/articles/{id}`

Updates an existing article. Only provided fields are updated (partial update supported).

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | `long` | Article ID |

**Request Body:**

All fields are optional. Only non-null fields will be updated.

| Field | Type | Description |
|---|---|---|
| `title` | `string` | New title |
| `author` | `string` | New author name |
| `content` | `string` | New Markdown content |
| `summary` | `string` | New summary |
| `tags` | `string[]` | New tags (replaces all existing tags) |
| `coverImage` | `string` | New cover image (Base64 or URL, same format as create) |

**Example Request:**

```bash
curl -X PUT http://localhost:8080/api/articles/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "tags": ["updated-tag", "ai"]
  }'
```

**Response:** `200 OK` - Returns the updated article in the same JSON format.

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
