package com.aiblog.controller;

import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.AestheticsArticleResponse;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.dto.PromptResponse;
import com.aiblog.dto.SnippetResponse;
import com.aiblog.model.Article;
import com.aiblog.model.AestheticsArticle;
import com.aiblog.model.DimensionType;
import com.aiblog.model.Prompt;
import com.aiblog.model.Snippet;
import com.aiblog.repository.ArticleRepository;
import com.aiblog.service.ArticleService;
import com.aiblog.service.AestheticsArticleService;
import com.aiblog.service.AestheticsDimensionService;
import com.aiblog.service.MarkdownService;
import com.aiblog.service.PromptService;
import com.aiblog.service.SnippetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class PageController {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final MarkdownService markdownService;
    private final SnippetService snippetService;
    private final AestheticsArticleService aestheticsArticleService;
    private final AestheticsDimensionService aestheticsDimensionService;
    private final PromptService promptService;

    public PageController(ArticleService articleService, ArticleRepository articleRepository,
                          MarkdownService markdownService, SnippetService snippetService,
                          AestheticsArticleService aestheticsArticleService,
                          AestheticsDimensionService aestheticsDimensionService,
                          PromptService promptService) {
        this.articleService = articleService;
        this.articleRepository = articleRepository;
        this.markdownService = markdownService;
        this.snippetService = snippetService;
        this.aestheticsArticleService = aestheticsArticleService;
        this.aestheticsDimensionService = aestheticsDimensionService;
        this.promptService = promptService;
    }

    @ModelAttribute("allTags")
    public List<String> allTags() {
        return articleRepository.findAllTags();
    }

    @ModelAttribute("allCategories")
    public List<String> allCategories() {
        return articleRepository.findAllCategories();
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ArticleResponse> articles = articleService.listPublished(pageable);
        model.addAttribute("articles", articles);
        model.addAttribute("paginationBaseUrl", "/");
        return "index";
    }

    @GetMapping("/articles/{id}")
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
        Page<ArticleResponse> articles = articleService.listPublishedByTag(tagName, pageable);
        model.addAttribute("articles", articles);
        model.addAttribute("tagName", tagName);
        model.addAttribute("paginationBaseUrl", "/tag/" + tagName);
        return "tag";
    }

    @GetMapping("/category/{categoryName}")
    public String categoryFilter(@PathVariable String categoryName,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ArticleResponse> articles = articleService.listPublishedByCategory(categoryName, pageable);
        model.addAttribute("articles", articles);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("paginationBaseUrl", "/category/" + categoryName);
        return "category";
    }

    @GetMapping("/snippets")
    public String snippets(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        LinkedHashMap<String, List<SnippetResponse>> snippetsByDate = snippetService.listGroupedByDate(pageable);
        Page<SnippetResponse> snippetsPage = snippetService.list(pageable);
        model.addAttribute("snippetsByDate", snippetsByDate);
        model.addAttribute("snippetsPage", snippetsPage);
        return "snippets";
    }

    @GetMapping("/snippets/{id}")
    public String snippetDetail(@PathVariable Long id, Model model) {
        Snippet snippet = snippetService.getEntityById(id);
        SnippetResponse snippetResponse = snippetService.getById(id);
        model.addAttribute("snippet", snippet);
        model.addAttribute("snippetResponse", snippetResponse);
        return "snippet";
    }

    // ==================== Aesthetics ====================

    @GetMapping("/aesthetics")
    public String aesthetics(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) Long sensory,
                             @RequestParam(required = false) Long domain,
                             @RequestParam(required = false) String tag,
                             Model model) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<AestheticsArticleResponse> articles;

        if (tag != null && !tag.isBlank()) {
            articles = aestheticsArticleService.listPublishedByTag(tag.trim(), pageable);
        } else if (sensory != null && domain != null) {
            articles = aestheticsArticleService.listPublishedByDimensions(sensory, domain, pageable);
        } else if (sensory != null) {
            articles = aestheticsArticleService.listPublishedBySensory(sensory, pageable);
        } else if (domain != null) {
            articles = aestheticsArticleService.listPublishedByDomain(domain, pageable);
        } else {
            articles = aestheticsArticleService.listPublished(pageable);
        }

        List<AestheticsDimensionResponse> sensoryDimensions = aestheticsDimensionService.listByType(DimensionType.SENSORY);
        List<AestheticsDimensionResponse> domainDimensions = aestheticsDimensionService.listByType(DimensionType.DOMAIN);

        model.addAttribute("articles", articles);
        model.addAttribute("sensoryDimensions", sensoryDimensions);
        model.addAttribute("domainDimensions", domainDimensions);
        model.addAttribute("activeSensory", sensory);
        model.addAttribute("activeDomain", domain);
        return "aesthetics";
    }

    @GetMapping("/aesthetics/{id}")
    public String aestheticsDetail(@PathVariable Long id, Model model) {
        AestheticsArticle article = aestheticsArticleService.getEntityById(id);
        String contentHtml = markdownService.renderToHtml(article.getContent());
        model.addAttribute("article", article);
        model.addAttribute("contentHtml", contentHtml);
        return "aesthetics-article";
    }

    // ==================== Prompts ====================

    @GetMapping("/prompts")
    public String prompts(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String source,
                          @RequestParam(name = "model", required = false) String promptModel,
                          @RequestParam(required = false) String tag,
                          Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<PromptResponse> prompts;

        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (source != null && !source.isBlank())
                || (promptModel != null && !promptModel.isBlank())
                || (tag != null && !tag.isBlank());

        if (hasFilter) {
            prompts = promptService.search(keyword, source, promptModel, tag, pageable);
        } else {
            prompts = promptService.listPublished(pageable);
        }

        model.addAttribute("prompts", prompts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("source", source);
        model.addAttribute("model", promptModel);
        model.addAttribute("tag", tag);
        model.addAttribute("allTags", promptService.findAllTags());
        model.addAttribute("allModels", promptService.findAllModels());
        return "prompts";
    }

    @GetMapping("/prompts/{id}")
    public String promptDetail(@PathVariable Long id, Model model) {
        Prompt prompt = promptService.getEntityById(id);
        String contentHtml = markdownService.renderToHtml(prompt.getContent());
        String notesHtml = prompt.getNotes() != null ? markdownService.renderToHtml(prompt.getNotes()) : null;
        model.addAttribute("prompt", prompt);
        model.addAttribute("contentHtml", contentHtml);
        model.addAttribute("notesHtml", notesHtml);
        return "prompt";
    }
}
