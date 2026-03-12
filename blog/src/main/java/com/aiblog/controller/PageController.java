package com.aiblog.controller;

import com.aiblog.dto.ArticleResponse;
import com.aiblog.model.Article;
import com.aiblog.repository.ArticleRepository;
import com.aiblog.service.ArticleService;
import com.aiblog.service.MarkdownService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PageController {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final MarkdownService markdownService;

    public PageController(ArticleService articleService, ArticleRepository articleRepository,
                          MarkdownService markdownService) {
        this.articleService = articleService;
        this.articleRepository = articleRepository;
        this.markdownService = markdownService;
    }

    @ModelAttribute("allTags")
    public List<String> allTags() {
        return articleRepository.findAllTags();
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ArticleResponse> articles = articleService.list(pageable);
        model.addAttribute("articles", articles);
        model.addAttribute("paginationBaseUrl", "/");
        return "index";
    }

    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id, Model model) {
        Article article = articleService.getEntityById(id);
        String contentHtml = markdownService.renderToHtml(article.getContent());
        model.addAttribute("article", article);
        model.addAttribute("contentHtml", contentHtml);
        return "article";
    }

    @GetMapping("/tag/{tagName}")
    public String tagFilter(@PathVariable String tagName,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ArticleResponse> articles = articleService.listByTag(tagName, pageable);
        model.addAttribute("articles", articles);
        model.addAttribute("tagName", tagName);
        model.addAttribute("paginationBaseUrl", "/tag/" + tagName);
        return "tag";
    }
}
