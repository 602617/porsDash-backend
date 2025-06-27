package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.AttendanceRequest;
import com.martin.demo.dto.EventDetailDto;
import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import com.martin.demo.dto.EventDto;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.EventAttendanceRepository;
import com.martin.demo.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final EventAttendanceRepository attendanceRepo;
    private final AppUserRepository userRepo;

    public EventService(EventRepository eventRepo,
                        EventAttendanceRepository attendanceRepo,
                        AppUserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.attendanceRepo = attendanceRepo;
        this.userRepo = userRepo;
    }

        public Event createEvent(EventDto dto, String username) {
            AppUser creator = userRepo.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("user not found"));
            Event ev = new Event();
            ev.setTitle(dto.getTitle());
            ev.setDescription(dto.getDescription());
            ev.setLocation(dto.getLocation());
            ev.setStartTime(dto.getStartTime());
            ev.setEndTime(dto.getEndTime());
            ev.setCreatedBy(creator);
            return eventRepo.save(ev);

        }

    public List<Event> listAll() {
        return eventRepo.findAll();
    }
    public EventAttendance rsvp(Long eventId, String username, AttendanceRequest req) {
        Event ev = eventRepo.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Finn eksisterende påmelding om den finnes
        EventAttendance att = attendanceRepo
                .findByEventIdAndUserId(eventId, user.getId())
                .orElseGet(() -> {
                    EventAttendance n = new EventAttendance();
                    n.setEvent(ev);
                    n.setUser(user);
                    return n;
                });

        att.setStatus(req.getStatus());
        att.setComment(req.getComment());
        att.setUpdatedAt(LocalDateTime.now());
        return attendanceRepo.save(att);
    }

    public EventDetailDto getDetail(Long eventId) {
        // 1) Finn event, kast 404 om ikke funnet
        Event ev = eventRepo.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event ikke funnet: " + eventId));

        // 2) Hent alle påmeldinger for dette event
        List<EventAttendance> attendees =
                attendanceRepo.findByEventId(eventId);

        // 3) Bygg og returner DTO
        return new EventDetailDto(ev, attendees);
    }
}
