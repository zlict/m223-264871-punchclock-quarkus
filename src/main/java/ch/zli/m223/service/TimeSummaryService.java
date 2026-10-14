package ch.zli.m223.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.zli.m223.dto.TimeSummaryDto;
import ch.zli.m223.model.Entry;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TimeSummaryService {
    public List<TimeSummaryDto> CalculateSummaryPerDay(List<Entry> entries) {
        List<TimeSummaryDto> summaries = new ArrayList<>();
        for (Entry entry : entries) {
            LocalDateTime start = entry.getCheckIn();
            LocalDateTime end = entry.getCheckOut();
            if (start == null || end == null || end.isBefore(start)) {
                throw new IllegalArgumentException("Entries require check-in and check-out in chronological order");
            }
            if (start.equals(end)) {
                addDuration(summaries, start.toLocalDate(), Duration.ZERO);
            }
            while (start.isBefore(end)) {
                LocalDateTime nextDay = start.toLocalDate().plusDays(1).atStartOfDay();
                LocalDateTime segmentEnd = end.isBefore(nextDay) ? end : nextDay;
                addDuration(summaries, start.toLocalDate(), Duration.between(start, segmentEnd));
                start = segmentEnd;
            }
        }
        summaries.sort(Comparator.comparing(TimeSummaryDto::date));
        return summaries;
    }

    private void addDuration(List<TimeSummaryDto> summaries, LocalDate date, Duration duration) {
        // Wenn der Tag bereits vorhanden ist, wird seine Dauer erhöht.
        for (int i = 0; i < summaries.size(); i++) {
            TimeSummaryDto summary = summaries.get(i);
            if (summary.date().equals(date)) {
                summaries.set(i, new TimeSummaryDto(date, summary.duration().plus(duration)));
                return;
            }
        }
        // Für einen neuen Tag wird eine neue Zusammenfassung hinzugefügt.
        summaries.add(new TimeSummaryDto(date, duration));
    }
}
