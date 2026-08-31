package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.TimeEntryDto;
import com.martin.demo.dto.TimeEntrySummaryDto;
import com.martin.demo.dto.UpdateTimeEntryDto;
import com.martin.demo.dto.WeeklySummaryDto;
import com.martin.demo.model.ScoringAction;
import com.martin.demo.model.TimeEntry;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.TimeEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimeEntryService {

    private static final long NORMAL_WORK_MINUTES = 480; // 8 hours

    private final TimeEntryRepository entries;
    private final AppUserRepository users;
    private final ScoreService scoreService;

    public TimeEntryService(TimeEntryRepository entries, AppUserRepository users,
                            ScoreService scoreService) {
        this.entries = entries;
        this.users = users;
        this.scoreService = scoreService;
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Bruker ikke funnet"));
    }

    @Transactional
    public TimeEntryDto startWork(String username, String note) {
        AppUser user = me(username);

        entries.findByUserUsernameAndEndTimeIsNull(username)
                .ifPresent(e -> {
                    throw new IllegalStateException("Du har allerede en aktiv registrering");
                });

        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setStartTime(Instant.now());
        if (note != null && !note.isBlank()) {
            entry.setNote(note);
        }

        TimeEntryDto result = TimeEntryDto.from(entries.save(entry));
        scoreService.award(username, ScoringAction.START_WORK);
        return result;
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

        TimeEntryDto result = TimeEntryDto.from(entries.save(entry));
        scoreService.award(username, ScoringAction.STOP_WORK);
        return result;
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

    public WeeklySummaryDto getWeeklySummary(String username, LocalDate weekDate) {
        // Find Monday of the given week
        LocalDate monday = weekDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Instant from = monday.atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant();
        Instant to = sunday.plusDays(1).atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant();

        List<TimeEntry> entries = this.entries.findByUserUsernameAndStartTimeBetween(username, from, to);

        // Group entries by day
        Map<LocalDate, List<TimeEntry>> byDay = entries.stream()
                .collect(Collectors.groupingBy(e ->
                        e.getStartTime().atZone(ZoneId.of("Europe/Oslo")).toLocalDate()));

        String[] dayNames = {"Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"};

        List<WeeklySummaryDto.DaySummaryDto> days = new ArrayList<>();
        long weekTotal = 0;
        long weekOvertime = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            List<TimeEntry> dayEntries = byDay.getOrDefault(date, List.of());

            long dayTotal = dayEntries.stream()
                    .filter(e -> e.getTotalMinutes() != null)
                    .mapToLong(TimeEntry::getTotalMinutes)
                    .sum();
            long dayOvertime = dayEntries.stream()
                    .filter(e -> e.getOvertimeMinutes() != null)
                    .mapToLong(TimeEntry::getOvertimeMinutes)
                    .sum();

            days.add(new WeeklySummaryDto.DaySummaryDto(date, dayNames[i], dayTotal, dayOvertime, dayEntries.size()));
            weekTotal += dayTotal;
            weekOvertime += dayOvertime;
        }

        return new WeeklySummaryDto(monday, sunday, weekTotal, weekOvertime, days);
    }

    @Transactional
    public TimeEntryDto updateEntry(Long id, UpdateTimeEntryDto dto, String username) {
        TimeEntry entry = entries.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registrering ikke funnet"));

        if (!entry.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Ikke tilgang");
        }

        if (dto.startTime() != null) {
            entry.setStartTime(dto.startTime());
        }
        if (dto.endTime() != null) {
            entry.setEndTime(dto.endTime());
        }
        if (dto.note() != null) {
            entry.setNote(dto.note());
        }

        if (entry.getStartTime() != null && entry.getEndTime() != null) {
            long minutes = Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes();
            entry.setTotalMinutes(minutes);
            entry.setOvertimeMinutes(Math.max(0, minutes - NORMAL_WORK_MINUTES));
        }

        return TimeEntryDto.from(entries.save(entry));
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
