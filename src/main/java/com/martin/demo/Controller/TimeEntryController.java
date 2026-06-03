package com.martin.demo.Controller;

import com.martin.demo.dto.TimeEntryDto;
import com.martin.demo.dto.TimeEntrySummaryDto;
import com.martin.demo.dto.UpdateTimeEntryDto;
import com.martin.demo.service.TimeEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public TimeEntryDto start(Authentication auth) {
        return service.startWork(auth.getName());
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
