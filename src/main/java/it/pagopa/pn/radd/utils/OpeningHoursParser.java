package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.exception.ExceptionTypeEnum;
import it.pagopa.pn.radd.pojo.OpeningHourEntry;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CustomLog
public class OpeningHoursParser {

    private OpeningHoursParser() {
        throw new IllegalStateException("OpeningHoursParser is a utility class");
    }

    private static final List<String> VALID_DAYS_ORDERED = List.of("lun", "mar", "mer", "gio", "ven", "sab", "dom");
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "(?i)^([a-z]{3})(?:-([a-z]{3}))?\\s+((?:\\d{2}:\\d{2}-\\d{2}:\\d{2})(?:\\s*,\\s*\\d{2}:\\d{2}-\\d{2}:\\d{2})*)$"
                                                               );
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})");

    private static final String ERRORE = ExceptionTypeEnum.OPENING_TIME_ERROR.getTitle();

    public static void validateOpenHours(String input) {
        Set<String> usedDays = new HashSet<>();
        List<OpeningHourEntry> entries = parseEntries(input, true);

        if (!entries.isEmpty()) {
            for (OpeningHourEntry entry : entries) {
                String day = entry.day();
                String times = entry.timeRanges();

                if (!usedDays.add(day)) {
                    log.debug("{}: Giorno duplicato: {}", ERRORE, day);
                }

                String[] timeRanges = times.split("\\s*,\\s*");
                for (String range : timeRanges) {
                    Matcher timeMatcher = TIME_RANGE_PATTERN.matcher(range);
                    if (!timeMatcher.matches()) {
                        log.debug("{}: Orario non riconosciuto: {}", ERRORE, range);

                    }

                    int startH = Integer.parseInt(timeMatcher.group(1));
                    int startM = Integer.parseInt(timeMatcher.group(2));
                    int endH = Integer.parseInt(timeMatcher.group(3));
                    int endM = Integer.parseInt(timeMatcher.group(4));

                    if (!isValidTime(startH, startM) || !isValidTime(endH, endM)) {
                        log.debug("{}: Orario non valido: {}", ERRORE, range);
                    }

                    if (startH > endH || (startH == endH && startM >= endM)) {
                        log.debug("{}: Intervallo orario non valido: {}", ERRORE, range);
                    }
                }
            }
        }
    }

    public static Map<String, String> parseOpeningHours(String input) {
        Map<String, String> result = new LinkedHashMap<>();
        List<OpeningHourEntry> entries = parseEntries(input, false);

        for (OpeningHourEntry entry : entries) {
            result.merge(entry.day(), entry.timeRanges(), (oldVal, newVal) -> oldVal + "," + newVal);
        }

        return result;
    }

    public static String serializeOpeningHours(Map<String, String> openingTime) {
        if (openingTime == null || openingTime.isEmpty()) return "";

        // Normalizza chiavi in lowercase
        Map<String, String> normalized = openingTime.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toLowerCase(),
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // in caso di duplicati tiene il primo
                        LinkedHashMap::new
                ));

        // Mappa: orari => lista di giorni
        Map<String, List<String>> timesToDays = new LinkedHashMap<>();

        for (String day : VALID_DAYS_ORDERED) {
            String times = normalized.get(day);
            if (times != null) {
                timesToDays.computeIfAbsent(times, k -> new ArrayList<>()).add(day);
            }
        }

        // Ricostruzione finale
        List<String> resultLines = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : timesToDays.entrySet()) {
            List<String> days = entry.getValue();
            String time = entry.getKey();

            // Raggruppa giorni consecutivi
            List<String> groupedDays = groupConsecutiveDays(days);

            for (String dayGroup : groupedDays) {
                resultLines.add(dayGroup + " " + time);
            }
        }

        return String.join("; ", resultLines);
    }

    private static List<String> groupConsecutiveDays(List<String> days) {
        List<String> result = new ArrayList<>();
        if (days.isEmpty()) return result;

        List<Integer> indexes = days.stream()
                .map(VALID_DAYS_ORDERED::indexOf)
                .sorted()
                .toList();

        int start = indexes.get(0);
        int prev = start;

        for (int i = 1; i < indexes.size(); i++) {
            int current = indexes.get(i);
            if (current == prev + 1) {
                prev = current;
            } else {
                result.add(formatDayRange(start, prev));
                start = current;
                prev = current;
            }
        }
        result.add(formatDayRange(start, prev));

        return result;
    }

    private static String formatDayRange(int start, int end) {
        return start == end ? VALID_DAYS_ORDERED.get(start) : VALID_DAYS_ORDERED.get(start) + "-" + VALID_DAYS_ORDERED.get(end);
    }


    private static List<OpeningHourEntry> parseEntries(String input, boolean validateFormat) {
        if (StringUtils.isBlank(input)) {
            if (validateFormat) {
                log.debug("{}: Orari di apertura non forniti", ERRORE);
            }
            return Collections.emptyList();
        }

        List<OpeningHourEntry> entries = new ArrayList<>();
        String[] lines = input.strip().split("[\\r?\\n;]+");

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher matcher = LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                if (validateFormat) {
                    log.debug("{}: Formato Opening Hours non valido", ERRORE);
                }
                continue; // parse-only mode: ignora righe non valide
            }

            String startDay = matcher.group(1).toLowerCase();
            String endDay = matcher.group(2) != null ? matcher.group(2).toLowerCase() : null;
            String times = matcher.group(3);

            List<String> daysInRange = expandDays(startDay, endDay);
            for (String day : daysInRange) {
                entries.add(new OpeningHourEntry(day, times));
            }
        }

        return entries;
    }

    private static boolean isValidTime(int hour, int minute) {
        return hour >= 0 && hour < 24 && minute >= 0 && minute < 60;
    }

    private static List<String> expandDays(String start, String end) {
        List<String> expandDays = List.of();
        int startIndex = VALID_DAYS_ORDERED.indexOf(start);
        if (startIndex == -1) {
            log.debug("{}: Intervallo giorni non valido: {}", ERRORE, start);
        }

        if (end == null) {
            return List.of(start);
        }

        int endIndex = VALID_DAYS_ORDERED.indexOf(end);
        if (endIndex == -1 || endIndex < startIndex) {
            log.debug("{}: Intervallo giorni invalido o fuori ordine: {}", ERRORE, end);
        }
        else{
            expandDays = VALID_DAYS_ORDERED.subList(startIndex, endIndex + 1);
        }

        return expandDays;
    }

}