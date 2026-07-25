package com.commandhub.service;

import com.commandhub.model.Command;
import com.commandhub.model.CommandStore;
import com.commandhub.model.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private final JsonStorageService storageService;

    public CommandService(JsonStorageService storageService) {
        this.storageService = storageService;
    }

    public PageResult<Command> search(String keyword, String platform, String tagsParam, int page, int size) {
        List<Command> all = storageService.getStore().getCommands();

        List<Command> filtered = all.stream()
                .filter(cmd -> matchPlatform(cmd, platform))
                .filter(cmd -> matchKeyword(cmd, keyword))
                .filter(cmd -> matchTags(cmd, tagsParam))
                .sorted(Comparator.comparing(Command::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        long total = filtered.size();
        int fromIndex = Math.min((page - 1) * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Command> content = filtered.subList(fromIndex, toIndex);

        return new PageResult<>(content, page, size, total);
    }

    public Optional<Command> findById(String id) {
        return storageService.getStore().getCommands().stream()
                .filter(cmd -> cmd.getId().equals(id))
                .findFirst();
    }

    public Command create(Command command) {
        command.setId(UUID.randomUUID().toString());
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());

        CommandStore store = storageService.getStore();
        store.getCommands().add(command);
        syncTags(store, command.getTags());
        storageService.save();

        return command;
    }

    public Optional<Command> update(String id, Command updated) {
        CommandStore store = storageService.getStore();
        for (int i = 0; i < store.getCommands().size(); i++) {
            Command existing = store.getCommands().get(i);
            if (existing.getId().equals(id)) {
                updated.setId(id);
                updated.setCreateTime(existing.getCreateTime());
                updated.setUpdateTime(LocalDateTime.now());
                store.getCommands().set(i, updated);
                syncTags(store, updated.getTags());
                storageService.save();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public boolean delete(String id) {
        CommandStore store = storageService.getStore();
        boolean removed = store.getCommands().removeIf(cmd -> cmd.getId().equals(id));
        if (removed) {
            storageService.save();
        }
        return removed;
    }

    public List<String> getAllTags() {
        return storageService.getStore().getTags();
    }

    public boolean deleteTag(String tagName) {
        CommandStore store = storageService.getStore();
        boolean removed = store.getTags().remove(tagName);
        if (removed) {
            storageService.save();
        }
        return removed;
    }

    private void syncTags(CommandStore store, List<String> newTags) {
        if (newTags == null) return;
        Set<String> existing = new LinkedHashSet<>(store.getTags());
        existing.addAll(newTags);
        store.setTags(new ArrayList<>(existing));
    }

    private boolean matchPlatform(Command cmd, String platform) {
        if (platform == null || platform.isBlank()) return true;
        return platform.equalsIgnoreCase(cmd.getPlatform());
    }

    private boolean matchKeyword(Command cmd, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (cmd.getTitle() != null && cmd.getTitle().toLowerCase().contains(kw))
                || (cmd.getCommand() != null && cmd.getCommand().toLowerCase().contains(kw))
                || (cmd.getDescription() != null && cmd.getDescription().toLowerCase().contains(kw));
    }

    private boolean matchTags(Command cmd, String tagsParam) {
        if (tagsParam == null || tagsParam.isBlank()) return true;
        if (cmd.getTags() == null || cmd.getTags().isEmpty()) return false;
        String[] required = tagsParam.split(",");
        for (String tag : required) {
            if (cmd.getTags().stream().noneMatch(t -> t.equalsIgnoreCase(tag.trim()))) {
                return false;
            }
        }
        return true;
    }
}
