package com.martin.demo.Controller;

import com.martin.demo.dto.TimeEntryDto;
import com.martin.demo.dto.TimeEntrySummaryDto;
import com.martin.demo.dto.UpdateTimeEntryDto;
import com.martin.demo.dto.WeeklySummaryDto;
import com.martin.demo.service.TimeEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public TimeEntryDto start(@RequestBody(required = false) Map<String, String> body, Authentication auth) {
        String note = body != null ? body.get("note") : null;
        return service.startWork(auth.getName(), note);
    }

    @PostMapping("/stop")
    public TimeEntryDto stop(Authentication auth) {
        return service.stopWork(auth.getName());
    }

    @GetMapping("/active")
    public ResponseEntity<TimeEntryDto> active(Authentication auth) {
        TimeEntryDto dto = service.getActive(auth.getName());
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<TimeEntryDto> list(Authentication auth) {
        return service.listEntries(auth.getName());
    }

    @GetMapping("/summary")
    public TimeEntrySummaryDto summary(
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication auth) {
        return service.getSummary(auth.getName(), from, to);
    }

    @GetMapping("/summary/weekly")
    public WeeklySummaryDto weeklySummary(
            @RequestParam(required = false) LocalDate date,
            Authentication auth) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return service.getWeeklySummary(auth.getName(), targetDate);
    }

    @PutMapping("/{id}")
    public TimeEntryDto update(
            @PathVariable Long id,
            @RequestBody UpdateTimeEntryDto dto,
            Authentication auth) {
        return service.updateEntry(id, dto, auth.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.deleteEntry(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
