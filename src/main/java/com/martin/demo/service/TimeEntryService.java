package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.TimeEntryDto;
import com.martin.demo.dto.TimeEntrySummaryDto;
import com.martin.demo.model.TimeEntry;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.TimeEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TimeEntryService {

    private static final long NORMAL_WORK_MINUTES = 480; // 8 hours

    private final TimeEntryRepository entries;
    private final AppUserRepository users;

    public TimeEntryService(TimeEntryRepository entries, AppUserRepository users) {
        this.entries = entries;
        this.users = users;
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Bruker ikke funnet"));
    }

    @Transactional
    public TimeEntryDto startWork(String username) {
        AppUser user = me(username);

        entries.findByUserUsernameAndEndTimeIsNull(username)
                .ifPresent(e -> {
                    throw new IllegalStateException("Du har allerede en aktiv registrering");
                });

        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setStartTime(Instant.now());

        return TimeEntryDto.from(entries.save(entry));
    }

    @Transactional
    public TimeEntryDto stopWork(String username) {
        TimeEntry entry = entries.findByUserUsernameAndEndTimeIsNull(username)
                .orElseThrow(() -> new IllegalStateException("Ingen aktiv registrering å stoppe"));

        Instant now = Instant.now();
        entry.setEndTime(now);

        long minutes = Duration.between(entry.getStartTime(), now).toMinutes();
        entry.setTotalMinutes(minutes);
        entry.setOvertimeMinutes(Math.max(0, minutes - NORMAL_WORK_MINUTES));

        return TimeEntryDto.from(entries.save(entry));
    }

    public TimeEntryDto getActive(String username) {
        return entries.findByUserUsernameAndEndTimeIsNull(username)
                .map(TimeEntryDto::from)
                .orElse(null);
    }

    public List<TimeEntryDto> listEntries(String username) {
        return entries.findByUserUsernameOrderByStartTimeDesc(username).stream()
                .map(TimeEntryDto::from)
                .toList();
    }

    public TimeEntrySummaryDto getSummary(String username, Instant from, Instant to) {
        List<TimeEntry> period = entries.findByUserUsernameAndStartTimeBetween(username, from, to);

        long totalMinutes = period.stream()
                .filter(e -> e.getTotalMinutes() != null)
                .mapToLong(TimeEntry::getTotalMinutes)
                .sum();

        long overtimeMinutes = period.stream()
                .filter(e -> e.getOvertimeMinutes() != null)
                .mapToLong(TimeEntry::getOvertimeMinutes)
                .sum();

        return new TimeEntrySummaryDto(totalMinutes, overtimeMinutes, period.size());
    }

    @Transactional
    public void deleteEntry(Long id, String username) {
        TimeEntry entry = entries.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registrering ikke funnet"));

        if (!entry.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Ikke tilgang");
        }

        entries.delete(entry);
    }
}
