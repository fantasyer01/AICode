package com.aiblog.controller;

import com.aiblog.dto.AestheticsArticleCreateRequest;
import com.aiblog.dto.AestheticsArticleResponse;
import com.aiblog.dto.AestheticsArticleUpdateRequest;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.model.AestheticsArticle;
import com.aiblog.model.DimensionType;
import com.aiblog.service.AestheticsArticleService;
import com.aiblog.service.AestheticsDimensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/aesthetics")
public class AdminAestheticsController {

    private static final Logger log = LoggerFactory.getLogger(AdminAestheticsController.class);

    private final AestheticsArticleService articleService;
    private final AestheticsDimensionService dimensionService;

    public AdminAestheticsController(AestheticsArticleService articleService,
                                     AestheticsDimensionService dimensionService) {
        this.articleService = articleService;
        this.dimensionService = dimensionService;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<AestheticsArticleResponse> articles = articleService.list(pageable);
        model.addAttribute("articles", articles);
        return "admin/aesthetics";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        populateDimensionDropdowns(model);
        return "admin/aesthetics-create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String author,
                         @RequestParam String content,
                         @RequestParam(required = false) String summary,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String coverImage,
                         @RequestParam Long sensoryDimensionId,
                         @RequestParam Long domainDimensionId,
                         @RequestParam(required = false, defaultValue = "true") boolean published,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            AestheticsArticleCreateRequest request = new AestheticsArticleCreateRequest();
            request.setTitle(title);
            request.setAuthor(author);
            request.setContent(content);
            request.setSummary(summary);
            request.setTags(parseTags(tags));
            request.setCoverImage(coverImage);
            request.setSensoryDimensionId(sensoryDimensionId);
            request.setDomainDimensionId(domainDimensionId);
            request.setPublished(published);

            articleService.create(request);
            log.info("Admin created aesthetics article title='{}'", title);
            redirectAttributes.addFlashAttribute("success", "Aesthetics article created successfully");
            return "redirect:/admin/aesthetics";
        } catch (Exception e) {
            log.error("Admin: failed to create aesthetics article: {}", e.getMessage());
            model.addAttribute("error", "Failed to create article: " + e.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("author", author);
            model.addAttribute("content", content);
            model.addAttribute("summary", summary);
            model.addAttribute("tags", tags);
            model.addAttribute("coverImage", coverImage);
            model.addAttribute("sensoryDimensionId", sensoryDimensionId);
            model.addAttribute("domainDimensionId", domainDimensionId);
            model.addAttribute("published", published);
            populateDimensionDropdowns(model);
            return "admin/aesthetics-create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        AestheticsArticle article = articleService.getEntityById(id);
        model.addAttribute("article", article);
        populateDimensionDropdowns(model);
        return "admin/aesthetics-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String author,
                         @RequestParam String content,
                         @RequestParam(required = false) String summary,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String coverImage,
                         @RequestParam Long sensoryDimensionId,
                         @RequestParam Long domainDimensionId,
                         @RequestParam(required = false, defaultValue = "true") boolean published,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            AestheticsArticleUpdateRequest request = new AestheticsArticleUpdateRequest();
            request.setTitle(title);
            request.setAuthor(author);
            request.setContent(content);
            request.setSummary(summary);
            request.setTags(parseTags(tags));
            request.setCoverImage(coverImage);
            request.setSensoryDimensionId(sensoryDimensionId);
            request.setDomainDimensionId(domainDimensionId);
            request.setPublished(published);

            articleService.update(id, request);
            log.info("Admin updated aesthetics article id={}", id);
            redirectAttributes.addFlashAttribute("success", "Aesthetics article updated successfully");
            return "redirect:/admin/aesthetics";
        } catch (Exception e) {
            log.error("Admin: failed to update aesthetics article id={}: {}", id, e.getMessage());
            AestheticsArticle article = articleService.getEntityById(id);
            model.addAttribute("article", article);
            model.addAttribute("error", "Failed to update article: " + e.getMessage());
            populateDimensionDropdowns(model);
            return "admin/aesthetics-edit";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        AestheticsArticle article = articleService.getEntityById(id);
        model.addAttribute("article", article);
        return "admin/aesthetics-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        log.info("Admin deleted aesthetics article id={}", id);
        redirectAttributes.addFlashAttribute("success", "Aesthetics article deleted successfully");
        return "redirect:/admin/aesthetics";
    }

    private void populateDimensionDropdowns(Model model) {
        List<AestheticsDimensionResponse> sensoryDimensions = dimensionService.listByType(DimensionType.SENSORY);
        List<AestheticsDimensionResponse> domainDimensions = dimensionService.listByType(DimensionType.DOMAIN);
        model.addAttribute("sensoryDimensions", sensoryDimensions);
        model.addAttribute("domainDimensions", domainDimensions);
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
