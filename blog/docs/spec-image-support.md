# 需求规格文档：文章图文支持改造

**版本**: 1.0  
**日期**: 2026-07-25  
**状态**: 实施中

---

## 1. 背景与目标

当前文章发布接口通过 `coverImage` 字段支持 Base64 内嵌传图，正文 Markdown 不支持图片插入。本次改造目标：

1. 统一图片上传入口，所有图片（封面、正文）均先通过专用接口上传并获取 URL
2. 去除 `coverImage` 字段的 Base64 支持，`POST /api/articles` 只接受图片 URL
3. 新增正文图片占位符替换接口
4. 新增独立图片上传接口，并纳入 API Key 认证体系
5. Admin 编辑器升级支持 Markdown 图片插入（EasyMDE）
6. 同步更新 API 文档

---

## 2. 现状分析

### 2.1 现有图片相关接口

| 接口 | 传输方式 | 问题 |
|---|---|---|
| `POST /api/articles` | `coverImage` 字段接受 Base64 或 URL（JSON body） | Base64 导致 body 体积膨胀，DB 存储 `content` 字段内嵌 Base64 无法处理 |
| `PUT /api/articles/{id}/cover` | multipart 文件上传，直接绑定文章封面 | 职责混合：上传 + 绑定一步完成，缺乏解耦 |

### 2.2 现有认证覆盖范围

`ApiKeyInterceptor` 当前覆盖路径：
```
/api/articles/**
/api/snippets/**
/api/aesthetics/**
/api/prompts/**
```

新增的 `/api/images/**` 路径**未在覆盖范围内**，需补充注册。

### 2.3 `ApiKeyInterceptor` 认证逻辑

拦截器对 `GET`/`HEAD`/`OPTIONS` 直接放行，仅对写操作（`POST`/`PUT`/`PATCH`/`DELETE`）验证 `X-API-Key` 请求头。新接口 `POST /api/images/upload` 属于写操作，纳入拦截器后自动生效。

---

## 3. 需求详述

### 需求 1：新增独立图片上传接口

#### 接口定义

```
POST /api/images/upload
Content-Type: multipart/form-data
X-API-Key: <required>
```

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | file | 是 | 图片文件，支持 `image/png`、`image/jpeg`、`image/gif`、`image/webp`、`image/svg+xml` |

**响应 `200 OK`：**

```json
{ "url": "/images/550e8400-e29b-41d4-a716-446655440000.png" }
```

**错误响应：**

| 状态码 | 原因 |
|---|---|
| `400 Bad Request` | 文件为空或 MIME 类型不在白名单 |
| `401 Unauthorized` | `X-API-Key` 缺失或错误 |
| `413 Payload Too Large` | 文件超过 5 MB |

**实现说明：**
- 复用 `ImageStorageService.saveImageFile()` 存储逻辑，零重复代码
- 新建 `ImageApiController`，不污染 `ArticleApiController`
- 新建响应 DTO `ImageUploadResponse { String url }`

#### 认证注册

在 `WebMvcConfig.addInterceptors()` 中补充注册：
```java
registry.addInterceptor(apiKeyInterceptor)
    .addPathPatterns(
        "/api/articles/**",
        "/api/snippets/**",
        "/api/aesthetics/**",
        "/api/prompts/**",
        "/api/images/**"   // 新增
    );
```

---

### 需求 2：重构 `POST /api/articles`，移除 Base64 支持

#### 变更点

**`ArticleCreateRequest.coverImage` 字段行为变更：**

| | 改造前 | 改造后 |
|---|---|---|
| 接受值 | Base64 data URI / 原始 Base64 / URL | 仅接受 URL（以 `/` 或 `http`/`https` 开头） |
| 处理逻辑 | `ImageStorageService.isBase64Image()` 判断，Base64 则解码存盘 | 直接赋值给 `coverImageUrl`，不做存储操作 |

**同步移除：**
- `ArticleServiceImpl.processImage()` 私有方法（Base64 解码分支全部删除）
- `ArticleUpdateRequest` 中 `coverImage` 字段同样只接受 URL

**向后兼容策略：**  
`POST /api/articles` 收到 Base64 值时，返回 `400 Bad Request`，错误信息：  
`"coverImage must be a URL (starting with /, http://, or https://). Upload the image first via POST /api/images/upload"`

---

### 需求 3：新增文章图片补录接口

#### 接口定义

```
PATCH /api/articles/{id}/images
Content-Type: application/json
X-API-Key: <required>
```

**请求体：**

```json
{
  "coverImageUrl": "/images/aaa.png",
  "contentReplacements": {
    "{{chart}}": "/images/bbb.png",
    "{{screenshot}}": "/images/ccc.png",
    "{{diagram}}": "/images/ddd.png"
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `coverImageUrl` | string | 否 | 替换文章封面，无论原来是否有封面均覆盖 |
| `contentReplacements` | `Map<String, String>` | 否 | key 为正文中的占位符字符串，value 为目标图片 URL；正文中所有匹配的占位符均被替换；支持任意数量的键值对 |

两个字段均为可选，但至少传一个，否则返回 `400 Bad Request`。

**响应 `200 OK`：** 返回完整的 `ArticleResponse`（同其他文章接口）。

**正文替换逻辑：**
- 对 `article.content` 执行字符串全量替换（`String.replace()`），每个 key 替换为对应 URL（自动形成 `![key](url)` 由调用方在占位符中预埋）
- 替换顺序与 Map 迭代顺序一致
- key 不存在于正文中时静默忽略（不报错）

**典型调用流程示例：**

```
// 写文章时正文预埋占位符
content = "## 架构图\n\n{{arch_diagram}}\n\n## 时序图\n\n{{seq_diagram}}"

// Step 1: 上传两张图片
POST /api/images/upload  file=arch.png   → { "url": "/images/aaa.png" }
POST /api/images/upload  file=seq.png    → { "url": "/images/bbb.png" }

// Step 2: 创建文章（此时正文含占位符）
POST /api/articles  { title: "...", content: "...{{arch_diagram}}...{{seq_diagram}}..." }

// Step 3: 补录图片 URL
PATCH /api/articles/42/images
{
  "contentReplacements": {
    "{{arch_diagram}}": "![架构图](/images/aaa.png)",
    "{{seq_diagram}}": "![时序图](/images/bbb.png)"
  }
}
```

**实现说明：**
- `ArticleService` 新增 `patchImages(Long id, ArticleImagePatchRequest request)` 方法
- 新建 DTO `ArticleImagePatchRequest { String coverImageUrl; Map<String, String> contentReplacements }`
- 在 `ArticleApiController` 新增 `@PatchMapping("/{id}/images")` 端点

---

### 需求 4：`PUT /api/articles/{id}/cover` 接口处理

该接口定位（上传文件 + 直接绑定封面）被"上传 + 补录"两步方案完全覆盖，直接删除。

**处置方案：完全删除**

- 删除 `ArticleApiController` 中 `PUT /{id}/cover` 端点及 `toCoverImageRequest()` 辅助方法
- 删除 `ArticleCoverImageUpdateRequest` DTO
- 删除 `ArticleService` 接口中 `updateCoverImage()` 方法签名
- 删除 `ArticleServiceImpl` 中 `updateCoverImage()` 实现
- API 文档中删除对应章节

---

### 需求 5：Admin 编辑器升级（EasyMDE）

#### 目标

在 `article-create.html` 和 `article-edit.html` 中，将原始 `<textarea>` 升级为 EasyMDE Markdown 编辑器，支持：
- Markdown 实时预览
- 标准格式工具栏
- **图片上传按钮**：点击后弹出文件选择，上传至 `POST /api/images/upload`，取得 URL 后在光标位置插入 `![描述](url)` 语法

#### 实现方案

**引入方式：** CDN（无需构建步骤）

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/easymde/dist/easymde.min.css">
<script src="https://cdn.jsdelivr.net/npm/easymde/dist/easymde.min.js"></script>
```

**图片上传 JS 逻辑（伪代码）：**

```javascript
const easyMDE = new EasyMDE({
  element: document.getElementById("content"),
  uploadImage: true,
  imageUploadFunction: (file, onSuccess, onError) => {
    const formData = new FormData();
    formData.append("file", file);
    fetch("/api/images/upload", {
      method: "POST",
      headers: { "X-API-Key": API_KEY },   // API Key 由模板变量注入或 meta 标签读取
      body: formData
    })
    .then(r => r.json())
    .then(data => onSuccess(data.url))
    .catch(() => onError("上传失败"));
  }
});
```

**Admin 认证上下文中 API Key 的传递方式：**  
由 Thymeleaf 模板从服务端 session/配置注入至页面 `<meta>` 标签或隐藏 `<input>`，JS 从 DOM 读取。具体注入方式在实现阶段确认。

**涉及文件：**
- `src/main/resources/templates/admin/article-create.html`
- `src/main/resources/templates/admin/article-edit.html`

---

### 需求 6：API 文档同步更新

**文件：** `src/main/resources/static/api-docs.md`

变更清单：

| 章节 | 操作 |
|---|---|
| `POST /api/articles` - `coverImage` 字段说明 | 改为"仅接受 URL，不再支持 Base64；请先调用 `POST /api/images/upload`" |
| `POST /api/articles` - 示例 curl | 移除 Base64 示例，改为 URL 示例 |
| `PUT /api/articles/{id}/cover` | 标注 Deprecated，推荐替代方案 |
| 新增章节：`POST /api/images/upload` | 完整文档（请求、响应、错误码、示例） |
| 新增章节：`PATCH /api/articles/{id}/images` | 完整文档（请求、响应、占位符说明、完整流程示例） |
| 全局 Notes 部分 | 更新图片使用流程说明 |

---

## 4. 影响范围

### 后端代码

| 文件 | 变更类型 |
|---|---|
| `WebMvcConfig.java` | 修改 — 补充 `/api/images/**` 拦截路径 |
| `ImageApiController.java` | 新建 — `POST /api/images/upload` |
| `ImageUploadResponse.java` | 新建 — DTO |
| `ArticleImagePatchRequest.java` | 新建 — DTO |
| `ArticleService.java`（接口） | 修改 — 新增 `patchImages()` 方法签名 |
| `ArticleServiceImpl.java` | 修改 — 实现 `patchImages()`；移除 `processImage()` Base64 分支 |
| `ArticleApiController.java` | 修改 — 新增 `PATCH /{id}/images`；删除 `/cover` 端点 |
| `ArticleCoverImageUpdateRequest.java` | 删除 — DTO 不再需要 |
| `ArticleCreateRequest.java` | 修改 — `coverImage` 字段添加 URL 格式校验注解 |
| `ArticleUpdateRequest.java` | 修改 — 同上 |

### 前端模板

| 文件 | 变更类型 |
|---|---|
| `article-create.html` | 修改 — 集成 EasyMDE，添加图片上传逻辑 |
| `article-edit.html` | 修改 — 同上 |

### 文档

| 文件 | 变更类型 |
|---|---|
| `src/main/resources/static/api-docs.md` | 修改 — 全面更新（见需求 6） |

### 不受影响的文件

- `Article.java`（model）
- `ImageStorageService.java`（复用，无需修改）
- `MarkdownService.java`
- `article.html`（公开展示页）
- 所有 Repository 层

---

## 5. 测试验证要求

### 5.1 新接口测试

| 测试用例 | 预期结果 |
|---|---|
| `POST /api/images/upload` 上传合法 PNG | 返回 `200`，`url` 以 `/images/` 开头，文件写入 `./data/images/` |
| `POST /api/images/upload` 无 API Key | 返回 `401` |
| `POST /api/images/upload` 上传非图片文件（如 `.txt`） | 返回 `400` |
| `POST /api/images/upload` 空文件 | 返回 `400` |
| `PATCH /api/articles/{id}/images` 补录封面 URL | 返回 `200`，文章 `coverImageUrl` 被替换 |
| `PATCH /api/articles/{id}/images` 替换正文单个占位符 | 正文中占位符被替换为 `![...](url)` |
| `PATCH /api/articles/{id}/images` 替换正文多个占位符（3个） | 全部占位符均被替换 |
| `PATCH /api/articles/{id}/images` 占位符不存在于正文 | 返回 `200`，正文无变化（静默忽略） |
| `PATCH /api/articles/{id}/images` 两个字段均为 null | 返回 `400` |
| `PATCH /api/articles/{id}/images` 文章不存在 | 返回 `404` |

### 5.2 重构验证测试

| 测试用例 | 预期结果 |
|---|---|
| `POST /api/articles` 传 Base64 `coverImage` | 返回 `400`，错误信息引导使用上传接口 |
| `POST /api/articles` 传 URL 格式 `coverImage` | 正常创建，`coverImageUrl` = 传入 URL |
| `POST /api/articles` 不传 `coverImage` | 正常创建，`coverImageUrl` = null |
| `PUT /api/articles/{id}/cover` 调用 | 返回 `404`（接口已删除） |

### 5.3 Admin 编辑器测试

| 测试用例 | 预期结果 |
|---|---|
| 进入文章创建页，编辑器正常渲染 EasyMDE | 工具栏和预览区均正常显示 |
| 点击图片上传按钮，选择图片文件 | 文件上传成功，`![](url)` 插入到光标位置 |
| 选择非图片文件上传 | 提示上传失败 |
| 提交含图片 URL 的文章 | 文章保存成功，公开页面图片正常渲染 |

---

## 6. 接口总览（改造后）

```
图片管理
  POST   /api/images/upload               上传图片，返回 URL [新增]

文章管理
  POST   /api/articles                    创建文章（coverImage 仅接受 URL）[重构]
  GET    /api/articles                    列表查询（不变）
  GET    /api/articles/{id}               详情查询（不变）
  PUT    /api/articles/{id}               更新文章（不变）
  DELETE /api/articles/{id}               删除文章（不变）
  PATCH  /api/articles/{id}/images        补录封面/正文图片 URL [新增]
```

所有写操作均需 `X-API-Key` 认证。
