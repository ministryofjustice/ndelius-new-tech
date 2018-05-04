package utils;

import interfaces.AnalyticsStore;

import java.time.LocalDateTime;
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

    default CompletableFuture<Long> pageVisits(String eventType, LocalDateTime from) {
        return null;
    }

    default CompletableFuture<Long> uniquePageVisits(String eventType, LocalDateTime from) {
        return null;
    }

    default CompletableFuture<Map<Integer, Long>> rankGrouping(String eventType, LocalDateTime from) {
        return null;
    }

    default CompletableFuture<Map<String, Long>> eventOutcome(String eventType, LocalDateTime from) {
        return null;
    }

    default CompletableFuture<Map<Long, Long>> durationBetween(String firstEventType, String secondEventType, LocalDateTime from, long groupBySeconds) {
        return null;
    }

    default CompletableFuture<Map<Integer, Long>> countGrouping(String eventType, String countFieldName, LocalDateTime from, long groupByScale) {
        return null;
    }

    default CompletableFuture<Map<String, Long>> countGroupingArray(String eventType, String countFieldName, LocalDateTime from) {
        return null;
    }
    default CompletableFuture<List<Map<String, Object>>> nationalSearchFeedback() {
        return null;
    }
    default CompletableFuture<List<Map<String, Object>>> sfpsrFeedback() {
        return null;
    }
    default CompletableFuture<Map<String, Integer>> filterCounts(LocalDateTime localDateTime) {
        return null;
    }

}