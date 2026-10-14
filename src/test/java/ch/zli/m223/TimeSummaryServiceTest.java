package ch.zli.m223;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ch.zli.m223.dto.TimeSummaryDto;
import ch.zli.m223.model.Entry;
import ch.zli.m223.service.TimeSummaryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeSummaryServiceTest {
    private final TimeSummaryService service = new TimeSummaryService();

    @Test
    void sumsEntriesAndSortsDays() {
        assertEquals(List.of(
                new TimeSummaryDto(LocalDate.parse("2024-08-20"), Duration.ofHours(8).plusMinutes(30)),
                new TimeSummaryDto(LocalDate.parse("2024-08-21"), Duration.ofHours(7).plusMinutes(45))),
                service.CalculateSummaryPerDay(List.of(
                        entry("2024-08-21T08:00:00", "2024-08-21T15:45:00"),
                        entry("2024-08-20T13:00:00", "2024-08-20T17:30:00"),
                        entry("2024-08-20T08:00:00", "2024-08-20T12:00:00"))));
    }

    @Test
    void splitsAtMidnightAndPreservesSeconds() {
        assertEquals(List.of(
                new TimeSummaryDto(LocalDate.parse("2024-08-20"), Duration.ofMinutes(30)),
                new TimeSummaryDto(LocalDate.parse("2024-08-21"), Duration.ofDays(1)),
                new TimeSummaryDto(LocalDate.parse("2024-08-22"), Duration.ofSeconds(15))),
                service.CalculateSummaryPerDay(List.of(
                        entry("2024-08-20T23:30:00", "2024-08-22T00:00:15"))));
    }

    @Test
    void handlesEmptyAndZeroDurationEntries() {
        assertEquals(List.of(), service.CalculateSummaryPerDay(List.of()));
        assertEquals(List.of(new TimeSummaryDto(LocalDate.parse("2024-08-20"), Duration.ZERO)),
                service.CalculateSummaryPerDay(List.of(
                        entry("2024-08-20T08:00:00", "2024-08-20T08:00:00"))));
    }

    @Test
    void rejectsNegativeDurations() {
        assertThrows(IllegalArgumentException.class, () -> service.CalculateSummaryPerDay(List.of(
                entry("2024-08-20T09:00:00", "2024-08-20T08:00:00"))));
    }

    private Entry entry(String start, String end) {
        Entry entry = new Entry();
        entry.setCheckIn(LocalDateTime.parse(start));
        entry.setCheckOut(LocalDateTime.parse(end));
        return entry;
    }
}
