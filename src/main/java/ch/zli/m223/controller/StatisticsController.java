package ch.zli.m223.controller;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import ch.zli.m223.dto.TimeSummaryDto;
import ch.zli.m223.service.EntryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/statistics")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsController {
    @Inject
    EntryService entryService;

    @GET
    @Path("/time-summaries")
    @Operation(summary = "Returns the total recorded duration for each day.")
    public Map<String, String> getTimeSummaries() {
        Map<String, String> summaries = new LinkedHashMap<>();
        for (TimeSummaryDto summary : entryService.GetTimeSummaries()) {
            Duration duration = summary.duration();
            summaries.put(summary.date().toString(), String.format(Locale.ROOT, "%02d:%02d:%02d",
                    duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }
        return summaries;
    }
}
