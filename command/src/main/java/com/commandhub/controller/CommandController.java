package com.commandhub.controller;

import com.commandhub.model.ApiResponse;
import com.commandhub.model.Command;
import com.commandhub.model.PageResult;
import com.commandhub.service.CommandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @GetMapping("/commands")
    public ApiResponse<PageResult<Command>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<Command> result = commandService.search(keyword, platform, tags, page, size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/commands/{id}")
    public ApiResponse<?> getById(@PathVariable String id) {
        return commandService.findById(id)
                .map(cmd -> ApiResponse.ok(cmd))
                .orElse(ApiResponse.error(404, "命令不存在"));
    }

    @PostMapping("/commands")
    public ApiResponse<Command> create(@RequestBody Command command) {
        Command created = commandService.create(command);
        return ApiResponse.ok(created);
    }

    @PutMapping("/commands/{id}")
    public ApiResponse<?> update(@PathVariable String id, @RequestBody Command command) {
        return commandService.update(id, command)
                .map(cmd -> ApiResponse.ok(cmd))
                .orElse(ApiResponse.error(404, "命令不存在"));
    }

    @DeleteMapping("/commands/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        if (commandService.delete(id)) {
            return ApiResponse.ok();
        }
        return ApiResponse.error(404, "命令不存在");
    }

    @GetMapping("/tags")
    public ApiResponse<List<String>> tags() {
        return ApiResponse.ok(commandService.getAllTags());
    }

    @DeleteMapping("/tags/{tagName}")
    public ApiResponse<?> deleteTag(@PathVariable String tagName) {
        if (commandService.deleteTag(tagName)) {
            return ApiResponse.ok();
        }
        return ApiResponse.error(404, "标签不存在");
    }
}
