package com.aiblog.controller;

import com.aiblog.dto.PromptCreateRequest;
import com.aiblog.dto.PromptResponse;
import com.aiblog.dto.PromptUpdateRequest;
import com.aiblog.model.Prompt;
import com.aiblog.model.PromptSource;
import com.aiblog.service.PromptService;
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
@RequestMapping("/admin/prompts")
public class AdminPromptController {

    private static final Logger log = LoggerFactory.getLogger(AdminPromptController.class);

    private final PromptService promptService;

    public AdminPromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<PromptResponse> prompts = promptService.list(pageable);
        model.addAttribute("prompts", prompts);
        return "admin/prompts";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("sources", PromptSource.values());
        return "admin/prompt-create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String notes,
                         @RequestParam String source,
                         @RequestParam(required = false) String promptModel,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false, defaultValue = "true") boolean published,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            PromptCreateRequest request = new PromptCreateRequest();
            request.setTitle(title);
            request.setContent(content);
            request.setDescription(description);
            request.setNotes(notes);
            request.setSource(PromptSource.valueOf(source));
            request.setModel(promptModel);
            request.setTags(parseTags(tags));
            request.setPublished(published);

            promptService.create(request);
            log.info("Admin created prompt title='{}'", title);
            redirectAttributes.addFlashAttribute("success", "Prompt created successfully");
            return "redirect:/admin/prompts";
        } catch (Exception e) {
            log.error("Admin: failed to create prompt: {}", e.getMessage());
            model.addAttribute("error", "Failed to create prompt: " + e.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("description", description);
            model.addAttribute("notes", notes);
            model.addAttribute("source", source);
            model.addAttribute("promptModel", promptModel);
            model.addAttribute("tags", tags);
            model.addAttribute("published", published);
            model.addAttribute("sources", PromptSource.values());
            return "admin/prompt-create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Prompt prompt = promptService.getEntityById(id);
        model.addAttribute("prompt", prompt);
        model.addAttribute("sources", PromptSource.values());
        return "admin/prompt-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String notes,
                         @RequestParam String source,
                         @RequestParam(required = false) String promptModel,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false, defaultValue = "true") boolean published,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            PromptUpdateRequest request = new PromptUpdateRequest();
            request.setTitle(title);
            request.setContent(content);
            request.setDescription(description);
            request.setNotes(notes);
            request.setSource(PromptSource.valueOf(source));
            request.setModel(promptModel);
            request.setTags(parseTags(tags));
            request.setPublished(published);

            promptService.update(id, request);
            log.info("Admin updated prompt id={}", id);
            redirectAttributes.addFlashAttribute("success", "Prompt updated successfully");
            return "redirect:/admin/prompts";
        } catch (Exception e) {
            log.error("Admin: failed to update prompt id={}: {}", id, e.toString(), e);
            Prompt prompt = promptService.getEntityById(id);
            model.addAttribute("prompt", prompt);
            model.addAttribute("sources", PromptSource.values());
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            model.addAttribute("error", "Failed to update prompt: " + msg);
            return "admin/prompt-edit";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        Prompt prompt = promptService.getEntityById(id);
        model.addAttribute("prompt", prompt);
        return "admin/prompt-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        promptService.delete(id);
        log.info("Admin deleted prompt id={}", id);
        redirectAttributes.addFlashAttribute("success", "Prompt deleted successfully");
        return "redirect:/admin/prompts";
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
