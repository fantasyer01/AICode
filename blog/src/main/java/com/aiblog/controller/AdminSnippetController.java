package com.aiblog.controller;

import com.aiblog.dto.SnippetCreateRequest;
import com.aiblog.dto.SnippetResponse;
import com.aiblog.model.Snippet;
import com.aiblog.service.SnippetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/snippets")
public class AdminSnippetController {

    private static final Logger log = LoggerFactory.getLogger(AdminSnippetController.class);

    private final SnippetService snippetService;

    public AdminSnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    // ==================== List ====================

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<SnippetResponse> snippets = snippetService.list(pageable);
        model.addAttribute("snippets", snippets);
        return "admin/snippets";
    }

    // ==================== Create ====================

    @GetMapping("/create")
    public String createForm() {
        return "admin/snippet-create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String rawContent,
                         @RequestParam(required = false) String author,
                         Model model) {
        try {
            SnippetCreateRequest request = new SnippetCreateRequest();
            request.setRawContent(rawContent);
            request.setAuthor(author);

            SnippetResponse response = snippetService.create(request);
            log.info("Admin created snippet id={}", response.getId());
            return "redirect:/admin/snippets";
        } catch (Exception e) {
            log.error("Admin: failed to create snippet: {}", e.getMessage());
            model.addAttribute("error", "Failed to create snippet: " + e.getMessage());
            model.addAttribute("rawContent", rawContent);
            model.addAttribute("author", author);
            return "admin/snippet-create";
        }
    }

    // ==================== Edit ====================

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Snippet snippet = snippetService.getEntityById(id);
        model.addAttribute("snippet", snippet);
        return "admin/snippet-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String rawContent,
                         @RequestParam(required = false) String processedTitle,
                         @RequestParam(required = false) String processedContent,
                         @RequestParam(required = false) String author,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            snippetService.saveOnly(id, rawContent, processedTitle, processedContent, author);
            log.info("Admin saved snippet id={}", id);
            redirectAttributes.addFlashAttribute("success", "保存成功");
            return "redirect:/admin/snippets";
        } catch (Exception e) {
            log.error("Admin: failed to save snippet id={}: {}", id, e.getMessage());
            Snippet snippet = snippetService.getEntityById(id);
            model.addAttribute("snippet", snippet);
            model.addAttribute("error", "Failed to save snippet: " + e.getMessage());
            return "admin/snippet-edit";
        }
    }

    @PostMapping("/{id}/reprocess")
    public String reprocess(@PathVariable Long id,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            snippetService.reprocess(id);
            log.info("Admin reprocessed snippet id={}", id);
            redirectAttributes.addFlashAttribute("success", "AI 重新处理完成");
            return "redirect:/admin/snippets/" + id + "/edit";
        } catch (Exception e) {
            log.error("Admin: failed to reprocess snippet id={}: {}", id, e.getMessage());
            Snippet snippet = snippetService.getEntityById(id);
            model.addAttribute("snippet", snippet);
            model.addAttribute("error", "Failed to reprocess snippet: " + e.getMessage());
            return "admin/snippet-edit";
        }
    }

    // ==================== Delete ====================

    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        Snippet snippet = snippetService.getEntityById(id);
        model.addAttribute("snippet", snippet);
        return "admin/snippet-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        snippetService.delete(id);
        log.info("Admin deleted snippet id={}", id);
        return "redirect:/admin/snippets";
    }
}
