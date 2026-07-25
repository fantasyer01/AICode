package com.aiblog.controller;

import com.aiblog.dto.AestheticsDimensionRequest;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.model.DimensionType;
import com.aiblog.service.AestheticsDimensionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/aesthetics/dimensions")
public class AestheticsDimensionApiController {

    private static final Logger log = LoggerFactory.getLogger(AestheticsDimensionApiController.class);

    private final AestheticsDimensionService dimensionService;

    public AestheticsDimensionApiController(AestheticsDimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @PostMapping
    public ResponseEntity<AestheticsDimensionResponse> create(@Valid @RequestBody AestheticsDimensionRequest request) {
        log.info("API: POST /api/aesthetics/dimensions - name='{}', type={}", request.getName(), request.getType());
        AestheticsDimensionResponse response = dimensionService.create(request);
        URI location = URI.create("/api/aesthetics/dimensions/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AestheticsDimensionResponse> getById(@PathVariable Long id) {
        log.info("API: GET /api/aesthetics/dimensions/{}", id);
        AestheticsDimensionResponse response = dimensionService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AestheticsDimensionResponse>> list(
            @RequestParam(required = false) String type) {
        log.info("API: GET /api/aesthetics/dimensions - type={}", type);
        List<AestheticsDimensionResponse> list;
        if (type != null && !type.isBlank()) {
            try {
                DimensionType dimensionType = DimensionType.valueOf(type.trim().toUpperCase());
                list = dimensionService.listByType(dimensionType);
            } catch (IllegalArgumentException e) {
                list = dimensionService.listAll();
            }
        } else {
            list = dimensionService.listAll();
        }
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AestheticsDimensionResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody AestheticsDimensionRequest request) {
        log.info("API: PUT /api/aesthetics/dimensions/{}", id);
        AestheticsDimensionResponse response = dimensionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API: DELETE /api/aesthetics/dimensions/{}", id);
        dimensionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
