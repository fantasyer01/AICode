package com.aiblog.controller;

import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.model.Article;
import com.aiblog.model.AuditLog;
import com.aiblog.repository.ArticleRepository;
import com.aiblog.service.ArticleService;
import com.aiblog.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final AuditLogService auditLogService;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminController(ArticleService articleService, ArticleRepository articleRepository,
                           AuditLogService auditLogService) {
        this.articleService = articleService;
        this.articleRepository = articleRepository;
        this.auditLogService = auditLogService;
    }

    // ==================== Login / Logout ====================

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute("ADMIN_LOGGED_IN", true);
            session.setAttribute("ADMIN_USERNAME", username);
            log.info("Admin login successful: {}", username);
            return "redirect:/admin";
        }

        log.warn("Admin login failed for username: {}", username);
        model.addAttribute("error", "Invalid username or password");
        return "admin/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ==================== Dashboard ====================

    @GetMapping({"", "/"})
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<ArticleResponse> articles = articleService.list(pageable);
        model.addAttribute("articles", articles);
        return "admin/dashboard";
    }

    // ==================== Create Article ====================

    @GetMapping("/articles/create")
    public String createForm() {
        return "admin/article-create";
    }

    @PostMapping("/articles/create")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String author,
                         @RequestParam(required = false) String summary,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String coverImage,
                         @RequestParam(required = false, defaultValue = "true") Boolean published,
                         @RequestParam String content,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            ArticleCreateRequest request = new ArticleCreateRequest();
            request.setTitle(title);
            request.setAuthor(author);
            request.setSummary(summary);
            request.setContent(content);
            request.setCoverImage(coverImage);
            request.setTags(parseTags(tags));
            request.setCategory(category);
            request.setPublished(published);

            ArticleResponse response = articleService.create(request);
            log.info("Admin created article id={}", response.getId());
            redirectAttributes.addFlashAttribute("success", "Article created successfully");
            return "redirect:/admin";
        } catch (Exception e) {
            log.error("Admin: failed to create article: {}", e.getMessage());
            model.addAttribute("error", "Failed to create article: " + e.getMessage());
            return "admin/article-create";
        }
    }

    // ==================== Edit Article ====================

    @GetMapping("/articles/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Article article = articleService.getEntityById(id);
        model.addAttribute("article", article);
        model.addAttribute("tagsString", String.join(", ", article.getTags()));
        return "admin/article-edit";
    }

    @PostMapping("/articles/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String author,
                         @RequestParam(required = false) String summary,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String coverImage,
                         @RequestParam(required = false, defaultValue = "true") Boolean published,
                         @RequestParam String content,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            log.info("Admin update article id={}, category='{}'", id, category);
            ArticleUpdateRequest request = new ArticleUpdateRequest();
            request.setTitle(title);
            request.setAuthor(author);
            request.setSummary(summary);
            request.setContent(content);
            request.setCoverImage(coverImage);
            request.setTags(parseTags(tags));
            request.setCategory(category);
            request.setPublished(published);

            articleService.update(id, request);
            log.info("Admin updated article id={}", id);
            redirectAttributes.addFlashAttribute("success", "Article updated successfully");
            return "redirect:/admin";
        } catch (Exception e) {
            log.error("Admin: failed to update article id={}: {}", id, e.getMessage());
            Article article = articleService.getEntityById(id);
            model.addAttribute("article", article);
            model.addAttribute("tagsString", tags);
            model.addAttribute("error", "Failed to update article: " + e.getMessage());
            return "admin/article-edit";
        }
    }

    // ==================== Delete Article ====================

    @GetMapping("/articles/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        Article article = articleService.getEntityById(id);
        model.addAttribute("article", article);
        return "admin/article-delete";
    }

    @PostMapping("/articles/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        log.info("Admin deleted article id={}", id);
        redirectAttributes.addFlashAttribute("success", "Article deleted successfully");
        return "redirect:/admin";
    }

    // ==================== Audit Logs ====================

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<AuditLog> logs = auditLogService.list(pageable);
        model.addAttribute("logs", logs);
        return "admin/audit-logs";
    }

    // ==================== Helpers ====================

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
