package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.AttendanceRequest;
import com.martin.demo.dto.EventDetailDto;
import com.martin.demo.model.AttendanceStatus;
import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import com.martin.demo.dto.EventDto;
import com.martin.demo.pushnotifications.notifications.NotificationService;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.EventAttendanceRepository;
import com.martin.demo.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final EventAttendanceRepository attendanceRepo;
    private final AppUserRepository userRepo;
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepo,
                        EventAttendanceRepository attendanceRepo,
                        AppUserRepository userRepo,
                        NotificationService notificationService) {
        this.eventRepo = eventRepo;
        this.attendanceRepo = attendanceRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    public Event createEvent(EventDto dto, String username) {
        AppUser creator = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        Event ev = new Event();
        ev.setTitle(dto.getTitle());
        ev.setDescription(dto.getDescription());
        ev.setLocation(dto.getLocation());
        ev.setStartTime(dto.getStartTime());
        ev.setEndTime(dto.getEndTime());
        ev.setCreatedBy(creator);
        Event saved = eventRepo.save(ev);

        if (dto.getInvitedUserIds() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
            for (Long userId : dto.getInvitedUserIds()) {
                AppUser invitee = userRepo.findById(userId).orElse(null);
                if (invitee == null || invitee.getId().equals(creator.getId())) continue;

                EventAttendance invite = new EventAttendance();
                invite.setEvent(saved);
                invite.setUser(invitee);
                invite.setStatus(AttendanceStatus.INVITED);
                invite.setUpdatedAt(LocalDateTime.now());
                attendanceRepo.save(invite);

                notificationService.notifyUser(
                        invitee.getId(),
                        creator.getUsername() + " inviterte deg til " + saved.getTitle()
                                + " (" + saved.getStartTime().format(fmt) + ")",
                        "/events/" + saved.getId()
                );
            }
        }

        return saved;
    }

    public Event updateEvent(Long eventId, EventDto dto, String username) {
        Event ev = eventRepo.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!ev.getCreatedBy().getUsername().equals(username)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Du eier ikke dette eventet");
        }

        ev.setTitle(dto.getTitle());
        ev.setDescription(dto.getDescription());
        ev.setLocation(dto.getLocation());
        ev.setStartTime(dto.getStartTime());
        ev.setEndTime(dto.getEndTime());
        return eventRepo.save(ev);
    }

        public void deleteEvent(Long eventId, String username) throws AccessDeniedException {
        Event ev = eventRepo.findById(eventId).orElseThrow(() -> new EntityNotFoundException("event not found"));

        if (!ev.getCreatedBy().getUsername().equals(username)) {
            throw new AccessDeniedException("Du eier ikke dette eventet");
        }

        eventRepo.delete(ev);


        }

    public List<Event> listAll(String username) {
        return eventRepo.findVisibleToUser(username);
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
