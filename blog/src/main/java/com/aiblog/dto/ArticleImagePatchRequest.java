package com.aiblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Request payload for patching image references on an existing article.
 *
 * <p>Both fields are optional, but at least one must be non-null/non-empty:
 * <ul>
 *   <li>{@code coverImageUrl} – replaces the article's cover image URL unconditionally.</li>
 *   <li>{@code contentReplacements} – a map of placeholder → image URL pairs; each key
 *       found in the article's Markdown content is replaced with its corresponding value.
 *       Keys absent from the content are silently ignored.</li>
 * </ul>
 */
@Getter
@Setter
public class ArticleImagePatchRequest {

    /** New cover image URL. Must start with '/', 'http://', or 'https://'. Optional. */
    private String coverImageUrl;

    /**
     * Placeholder-to-URL replacement map for inline body images.
     * Example: {@code {"{{diagram}}": "![diagram](/images/abc.png)"}}
     * Supports an arbitrary number of entries.
     */
    private Map<String, String> contentReplacements;
}
