package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.*;
import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.EventRepository;
import com.martin.demo.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final AppUserRepository userRepository;
    private final EventRepository eventRepository;

    public EventController(EventService eventService, AppUserRepository userRepository, EventRepository eventRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
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

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId, Principal principal) {
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("username not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "event not found"));

        if (!event.getCreatedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body ("Du eier ikke dette eventet");
        }

        eventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }
}
