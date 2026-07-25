package com.commandhub.service;

import com.commandhub.config.AppProperties;
import com.commandhub.model.CommandStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class JsonStorageService {

    private static final Logger log = LoggerFactory.getLogger(JsonStorageService.class);

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private CommandStore store;

    public JsonStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        load();
    }

    public void load() {
        lock.writeLock().lock();
        try {
            File file = new File(appProperties.getDataPath());
            if (file.exists()) {
                store = objectMapper.readValue(file, CommandStore.class);
                log.info("Loaded {} commands from {}", store.getCommands().size(), file.getAbsolutePath());
            } else {
                store = new CommandStore();
                store.setCommands(new ArrayList<>());
                store.setTags(new ArrayList<>(List.of("网络", "文件", "系统管理", "Docker", "Git", "调试", "运维")));
                save();
                log.info("Created new data file at {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to load data file", e);
            store = new CommandStore();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void save() {
        lock.writeLock().lock();
        try {
            File file = new File(appProperties.getDataPath());
            Path parent = file.toPath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(file, store);
        } catch (IOException e) {
            log.error("Failed to save data file", e);
            throw new RuntimeException("Failed to save data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CommandStore getStore() {
        lock.readLock().lock();
        try {
            return store;
        } finally {
            lock.readLock().unlock();
        }
    }
}
