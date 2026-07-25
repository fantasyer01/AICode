package com.aiblog.controller;

import com.aiblog.dto.AestheticsDimensionRequest;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.model.AestheticsDimension;
import com.aiblog.model.DimensionType;
import com.aiblog.service.AestheticsDimensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/aesthetics/dimensions")
public class AdminAestheticsDimensionController {

    private static final Logger log = LoggerFactory.getLogger(AdminAestheticsDimensionController.class);

    private final AestheticsDimensionService dimensionService;

    public AdminAestheticsDimensionController(AestheticsDimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<AestheticsDimensionResponse> dimensions = dimensionService.listAll();
        model.addAttribute("dimensions", dimensions);
        return "admin/aesthetics-dimensions";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dimensionTypes", DimensionType.values());
        return "admin/aesthetics-dimension-create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam String type,
                         @RequestParam(required = false, defaultValue = "0") int displayOrder,
                         @RequestParam(required = false) String description,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            AestheticsDimensionRequest request = new AestheticsDimensionRequest();
            request.setName(name);
            request.setType(DimensionType.valueOf(type));
            request.setDisplayOrder(displayOrder);
            request.setDescription(description);

            dimensionService.create(request);
            log.info("Admin created aesthetics dimension name='{}'", name);
            redirectAttributes.addFlashAttribute("success", "Dimension created successfully");
            return "redirect:/admin/aesthetics/dimensions";
        } catch (Exception e) {
            log.error("Admin: failed to create dimension: {}", e.getMessage());
            model.addAttribute("error", "Failed to create dimension: " + e.getMessage());
            model.addAttribute("dimensionTypes", DimensionType.values());
            model.addAttribute("name", name);
            model.addAttribute("type", type);
            model.addAttribute("displayOrder", displayOrder);
            model.addAttribute("description", description);
            return "admin/aesthetics-dimension-create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        AestheticsDimension dimension = dimensionService.getEntityById(id);
        model.addAttribute("dimension", dimension);
        model.addAttribute("dimensionTypes", DimensionType.values());
        return "admin/aesthetics-dimension-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam String type,
                         @RequestParam(required = false, defaultValue = "0") int displayOrder,
                         @RequestParam(required = false) String description,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            AestheticsDimensionRequest request = new AestheticsDimensionRequest();
            request.setName(name);
            request.setType(DimensionType.valueOf(type));
            request.setDisplayOrder(displayOrder);
            request.setDescription(description);

            dimensionService.update(id, request);
            log.info("Admin updated aesthetics dimension id={}", id);
            redirectAttributes.addFlashAttribute("success", "Dimension updated successfully");
            return "redirect:/admin/aesthetics/dimensions";
        } catch (Exception e) {
            log.error("Admin: failed to update dimension id={}: {}", id, e.getMessage());
            AestheticsDimension dimension = dimensionService.getEntityById(id);
            model.addAttribute("dimension", dimension);
            model.addAttribute("dimensionTypes", DimensionType.values());
            model.addAttribute("error", "Failed to update dimension: " + e.getMessage());
            return "admin/aesthetics-dimension-edit";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        AestheticsDimension dimension = dimensionService.getEntityById(id);
        model.addAttribute("dimension", dimension);
        return "admin/aesthetics-dimension-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dimensionService.delete(id);
            log.info("Admin deleted aesthetics dimension id={}", id);
            redirectAttributes.addFlashAttribute("success", "Dimension deleted successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/aesthetics/dimensions";
    }
}
