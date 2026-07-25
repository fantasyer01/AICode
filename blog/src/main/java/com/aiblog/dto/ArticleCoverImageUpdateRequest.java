package com.aiblog.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for replacing/setting an article's cover image.
 *
 * <p>This DTO is transport-agnostic: it carries the raw image bytes plus the
 * minimal metadata needed by the storage layer (content type, original filename).
 * The Controller is responsible for unwrapping framework types
 * (e.g. {@code MultipartFile}) into this DTO before calling the service, so that
 * the Service layer remains free of any web/HTTP dependency, mirroring the
 * style of {@link ArticleCreateRequest} and {@link ArticleUpdateRequest}.
 */
@Getter
@Setter
public class ArticleCoverImageUpdateRequest {

    /** Raw image bytes. Must be non-null and non-empty. */
    private byte[] data;

    /** MIME content type, e.g. {@code image/png}. Must be one of the supported image types. */
    private String contentType;

    /** Original uploaded filename, used to preserve the file extension when persisted. May be {@code null}. */
    private String originalFilename;
}
