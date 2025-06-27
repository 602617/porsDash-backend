package com.martin.demo.Controller;

import com.martin.demo.dto.*;
import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import com.martin.demo.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** Opprett nytt arrangement */
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @RequestBody EventDto dto,
            Principal principal) {
        Event created = eventService.createEvent(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Liste alle arrangement */
    @GetMapping
    public List<EventListDto> listEvents() {
        return eventService.listAll()
                .stream().map(EventListDto::new)
                .toList();
    }

    /** Detaljer om ett arrangement, inkl. påmeldinger*/
    @GetMapping("/{eventId}")
    public EventDetailDto eventDetail(@PathVariable Long eventId) {
        return eventService.getDetail(eventId);
    }

    /** Påmelding / oppdater status */
    @PostMapping("/{eventId}/attendance")
    public ResponseEntity<AttendanceDto> rsvp(
            @PathVariable Long eventId,
            @RequestBody AttendanceRequest req,
            Principal principal) {
        EventAttendance saved = eventService.rsvp(eventId, principal.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AttendanceDto(saved));
    }
}
