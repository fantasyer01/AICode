package com.aiblog.dto;

import com.aiblog.model.Snippet;
import com.aiblog.model.SnippetStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SnippetResponse {

    private Long id;
    private String rawContent;
    private String processedTitle;
    private String processedContent;
    private String displayTitle;
    private String displayContent;
    private String displayContentHtml;
    private List<String> tags;
    private String author;
    private SnippetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SnippetResponse fromEntity(Snippet snippet, String displayContentHtml) {
        SnippetResponse response = new SnippetResponse();
        response.setId(snippet.getId());
        response.setRawContent(snippet.getRawContent());
        response.setProcessedTitle(snippet.getProcessedTitle());
        response.setProcessedContent(snippet.getProcessedContent());
        response.setTags(snippet.getTags());
        response.setAuthor(snippet.getAuthor());
        response.setStatus(snippet.getStatus());
        response.setCreatedAt(snippet.getCreatedAt());
        response.setUpdatedAt(snippet.getUpdatedAt());

        // Compute display fields with fallback
        if (snippet.getStatus() == SnippetStatus.PROCESSED) {
            response.setDisplayTitle(snippet.getProcessedTitle());
            response.setDisplayContent(snippet.getProcessedContent());
        } else {
            response.setDisplayTitle(null);
            response.setDisplayContent(snippet.getRawContent());
        }
        response.setDisplayContentHtml(displayContentHtml);

        return response;
    }
}
