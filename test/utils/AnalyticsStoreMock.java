package utils;

import interfaces.AnalyticsStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AnalyticsStoreMock extends AnalyticsStore {

    default void recordEvent(Map<String, Object> data) {
    }

    default CompletableFuture<List<Map<String, Object>>> recentEvents(int limit) {
        return null;
    }

    default CompletableFuture<Map<Integer, Long>> pageVisits() {
        return null;
    }

    default CompletableFuture<Long> pageVisits(String eventType) {
        return null;
    }
    default CompletableFuture<Long> uniquePageVisits(String eventType) {
        return null;
    }
}