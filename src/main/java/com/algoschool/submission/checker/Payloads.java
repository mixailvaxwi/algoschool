package com.algoschool.submission.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Разбор ответов, записанных индексами.
 * <p>
 * Ответ студента приходит строкой, и испорченная строка — это неверный ответ,
 * а не ошибка сервера: разбор всюду возвращает {@code null} вместо исключения.
 */
final class Payloads {

    private Payloads() {
    }

    /** «2,0,1» — список индексов по порядку. */
    static List<Integer> parseIndexes(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        List<Integer> indexes = new ArrayList<>();
        for (String part : payload.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                indexes.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return indexes;
    }

    /** «0-2,1-0» — пары «левый-правый». Повтор левого индекса делает ответ невалидным. */
    static Map<Integer, Integer> parsePairs(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        Map<Integer, Integer> pairs = new LinkedHashMap<>();
        for (String part : payload.split(",")) {
            String[] sides = part.trim().split("-");
            if (sides.length != 2) {
                return null;
            }
            try {
                Integer left = Integer.parseInt(sides[0].trim());
                Integer right = Integer.parseInt(sides[1].trim());
                if (pairs.put(left, right) != null) {
                    return null; // один левый элемент дважды — ответ противоречив
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return pairs;
    }

    /** Ровно ли это перестановка 0..size-1 — без пропусков и повторов. */
    static boolean isPermutation(List<Integer> indexes, int size) {
        if (indexes.size() != size) {
            return false;
        }
        Set<Integer> seen = new HashSet<>(indexes);
        if (seen.size() != size) {
            return false;
        }
        return indexes.stream().allMatch(index -> index >= 0 && index < size);
    }
}
