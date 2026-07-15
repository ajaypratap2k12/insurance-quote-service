package com.example.insurance.rules;

import io.gorules.zen_engine.JsonBuffer;
import io.gorules.zen_engine.ZenDecision;
import io.gorules.zen_engine.ZenEngine;
import io.gorules.zen_engine.ZenException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class RuleManager {

    private final ZenEngine engine;
    private final Map<String, ZenDecision> decisionCache = new ConcurrentHashMap<>();
    private final Map<String, byte[]> ruleCache = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running = false;

    @Value("${gorules.rules.path:classpath:rules}")
    private String rulesPath;

    public RuleManager() {
        this.engine = new ZenEngine(null, null);
        log.info("GoRules ZenEngine initialized");
    }

    @PostConstruct
    public void init() {
        loadAllRules();
        startFileWatcher();
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            log.error("Error closing watch service", e);
        }
        engine.close();
        log.info("RuleManager shut down");
    }

    public ZenDecision getDecision(String decisionName) {
        return decisionCache.get(decisionName);
    }

    public ZenDecision loadDecision(String decisionName) {
        Path rulePath = resolveRulePath(decisionName);
        if (rulePath == null) {
            throw new IllegalArgumentException("Rule file not found: " + decisionName);
        }

        try {
            byte[] ruleBytes = Files.readAllBytes(rulePath);
            ruleCache.put(decisionName, ruleBytes);
            ZenDecision decision = engine.createDecision(new JsonBuffer(ruleBytes));
            decisionCache.put(decisionName, decision);
            log.info("Loaded decision: {} from {}", decisionName, rulePath);
            return decision;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rule: " + decisionName, e);
        } catch (ZenException e) {
            throw new RuntimeException("Failed to parse rule: " + decisionName, e);
        }
    }

    public byte[] getRuleBytes(String decisionName) {
        return ruleCache.get(decisionName);
    }

    private void loadAllRules() {
        Path path = resolveRulesDirectory();
        if (path == null || !Files.exists(path)) {
            log.warn("Rules directory not found: {}", rulesPath);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                loadDecision(fileName);
            }
            log.info("Loaded {} rules from {}", decisionCache.size(), path);
        } catch (IOException e) {
            log.error("Error loading rules from {}", path, e);
        }
    }

    private void startFileWatcher() {
        Path path = resolveRulesDirectory();
        if (path == null || !Files.exists(path)) {
            log.warn("Cannot start file watcher: rules directory not found");
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            running = true;
            watchThread = new Thread(this::watchLoop, "rules-watcher");
            watchThread.setDaemon(true);
            watchThread.start();

            log.info("File watcher started for directory: {}", path);
        } catch (IOException e) {
            log.error("Failed to start file watcher", e);
        }
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
                    String decisionName = fileName.toString();

                    if (!decisionName.endsWith(".json")) continue;

                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY ||
                            kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        log.info("Rule file {} detected, reloading: {}", kind, decisionName);
                        reloadDecision(decisionName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        log.info("Rule file deleted: {}", decisionName);
                        decisionCache.remove(decisionName);
                        ruleCache.remove(decisionName);
                    }
                }

                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in file watcher", e);
            }
        }
    }

    private void reloadDecision(String decisionName) {
        try {
            Thread.sleep(100); // Wait for file write to complete
            loadDecision(decisionName);
            log.info("Successfully reloaded decision: {}", decisionName);
        } catch (Exception e) {
            log.error("Failed to reload decision: {}", decisionName, e);
        }
    }

    private Path resolveRulePath(String decisionName) {
        Path path = resolveRulesDirectory();
        if (path == null) return null;
        return path.resolve(decisionName);
    }

    private Path resolveRulesDirectory() {
        if (rulesPath.startsWith("classpath:")) {
            String resourcePath = rulesPath.substring("classpath:".length());
            try {
                return Path.of(getClass().getClassLoader().getResource(resourcePath).toURI());
            } catch (Exception e) {
                log.error("Failed to resolve classpath resource: {}", resourcePath, e);
                return null;
            }
        }
        return Path.of(rulesPath);
    }
}
