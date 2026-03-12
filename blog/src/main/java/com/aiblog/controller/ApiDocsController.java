package com.aiblog.controller;

import com.aiblog.service.MarkdownService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api/docs")
public class ApiDocsController {

    private final MarkdownService markdownService;

    public ApiDocsController(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    @GetMapping
    public String docsPage(Model model) throws IOException {
        String markdown = loadApiDocsMd();
        String html = markdownService.renderToHtml(markdown);
        model.addAttribute("contentHtml", html);
        model.addAttribute("title", "API Documentation");
        return "api-docs";
    }

    @GetMapping("/raw")
    @ResponseBody
    public ResponseEntity<String> docsRaw() throws IOException {
        String markdown = loadApiDocsMd();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(markdown);
    }

    private String loadApiDocsMd() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/api-docs.md");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
