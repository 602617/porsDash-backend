package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.*;
import com.martin.demo.model.*;
import com.martin.demo.pushnotifications.notifications.NotificationService;
import com.martin.demo.repository.ApplicationOfferRepository;
import com.martin.demo.repository.ApplicationPermissionRepository;
import com.martin.demo.repository.ApplicationRepository;
import com.martin.demo.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ApplicationOfferRepository offers;
    private final ApplicationPermissionRepository permissions;
    private final AppUserRepository users;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applications,
                              ApplicationOfferRepository offers,
                              ApplicationPermissionRepository permissions,
                              AppUserRepository users,
                              NotificationService notificationService) {
        this.applications = applications;
        this.offers = offers;
        this.permissions = permissions;
        this.users = users;
        this.notificationService = notificationService;
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void assertCanSend(AppUser user) {
        if (!permissions.existsByUserIdAndRole(user.getId(), ApplicationPermissionRole.SENDER)) {
            throw new AccessDeniedException("Du har ikke tilgang til å sende søknader");
        }
    }

    private boolean isReceiver(AppUser user) {
        return permissions.existsByUserIdAndRole(user.getId(), ApplicationPermissionRole.RECEIVER);
    }

    private void assertCanRespond(Application app, AppUser user) {
        if (app.getStatus() == ApplicationStatus.ACCEPTED || app.getStatus() == ApplicationStatus.DECLINED) {
            throw new IllegalStateException("Søknaden er allerede avsluttet");
        }

        List<ApplicationOffer> offerList = app.getOffers();
        ApplicationOffer latestOffer = offerList.get(offerList.size() - 1);

        // The person who made the latest offer cannot respond to their own offer
        if (latestOffer.getOfferedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Du kan ikke svare på ditt eget tilbud");
        }

        // For initial PENDING applications: only receivers can respond
        if (app.getStatus() == ApplicationStatus.PENDING && app.getRespondedBy() == null) {
            if (!isReceiver(user)) {
                throw new AccessDeniedException("Du har ikke tilgang til å svare på søknader");
            }
        } else {
            // For COUNTERED: only sender or the specific receiver who is in the negotiation
            boolean isSender = app.getSender().getId().equals(user.getId());
            boolean isRespondent = app.getRespondedBy() != null
                    && app.getRespondedBy().getId().equals(user.getId());
            if (!isSender && !isRespondent) {
                throw new AccessDeniedException("Du er ikke del av denne forhandlingen");
            }
        }
    }

    private boolean hasAccess(Application app, AppUser user) {
        if (app.getSender().getId().equals(user.getId())) return true;
        if (app.getRespondedBy() != null && app.getRespondedBy().getId().equals(user.getId())) return true;
        // Receivers can see pending applications that haven't been claimed
        if (app.getRespondedBy() == null && isReceiver(user)) return true;
        return false;
    }

    @Transactional
    public ApplicationDetailDto create(CreateApplicationDto dto, String username) {
        AppUser sender = me(username);
        assertCanSend(sender);

        ApplicationType type = ApplicationType.valueOf(dto.type().toUpperCase());

        Application app = new Application();
        app.setType(type);
        app.setSender(sender);
        app.setStatus(ApplicationStatus.PENDING);

        Application saved = applications.save(app);

        // Create the initial offer
        ApplicationOffer offer = new ApplicationOffer();
        offer.setApplication(saved);
        offer.setAmount(dto.amount());
        offer.setDescription(dto.description());
        offer.setOfferedBy(sender);
        offer.setCounterOffer(false);
        offers.save(offer);

        saved.getOffers().add(offer);

        // Notify all receivers
        List<ApplicationPermission> receivers = permissions.findByRole(ApplicationPermissionRole.RECEIVER);
        String typeName = typeDisplayName(type);
        for (ApplicationPermission perm : receivers) {
            notificationService.notifyUser(
                    perm.getUser().getId(),
                    sender.getUsername() + " sendte en søknad: " + typeName + " (" + dto.amount() + " kr)",
                    "/applications/" + saved.getId()
            );
        }

        return ApplicationDetailDto.from(saved);
    }

    @Transactional
    public ApplicationDetailDto respond(Long applicationId, RespondApplicationDto dto, String username) {
        Application app = applications.findByIdAndActiveTrue(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Søknad ikke funnet"));

        AppUser responder = me(username);
        assertCanRespond(app, responder);

        String action = dto.action().toUpperCase();

        return switch (action) {
            case "ACCEPT" -> handleAccept(app, responder);
            case "DECLINE" -> handleDecline(app, responder);
            case "COUNTER" -> handleCounter(app, responder, dto);
            default -> throw new IllegalArgumentException("Ugyldig handling: " + dto.action());
        };
    }

    private ApplicationDetailDto handleAccept(Application app, AppUser responder) {
        if (app.getRespondedBy() == null) {
            app.setRespondedBy(responder);
        }
        app.setStatus(ApplicationStatus.ACCEPTED);
        app.setUpdatedAt(Instant.now());
        applications.save(app);

        ApplicationOffer currentOffer = app.getOffers().get(app.getOffers().size() - 1);
        String typeName = typeDisplayName(app.getType());

        // Notify the other party
        AppUser toNotify = getOtherParty(app, responder);
        notificationService.notifyUser(
                toNotify.getId(),
                responder.getUsername() + " godtok søknaden: " + typeName + " (" + currentOffer.getAmount() + " kr)",
                "/applications/" + app.getId()
        );

        return ApplicationDetailDto.from(app);
    }

    private ApplicationDetailDto handleDecline(Application app, AppUser responder) {
        if (app.getRespondedBy() == null) {
            app.setRespondedBy(responder);
        }
        app.setStatus(ApplicationStatus.DECLINED);
        app.setUpdatedAt(Instant.now());
        applications.save(app);

        String typeName = typeDisplayName(app.getType());

        AppUser toNotify = getOtherParty(app, responder);
        notificationService.notifyUser(
                toNotify.getId(),
                responder.getUsername() + " avslo søknaden: " + typeName,
                "/applications/" + app.getId()
        );

        return ApplicationDetailDto.from(app);
    }

    private ApplicationDetailDto handleCounter(Application app, AppUser responder, RespondApplicationDto dto) {
        if (dto.amount() == null) {
            throw new IllegalArgumentException("Beløp er påkrevd for mottilbud");
        }

        if (app.getRespondedBy() == null) {
            app.setRespondedBy(responder);
        }

        ApplicationOffer counter = new ApplicationOffer();
        counter.setApplication(app);
        counter.setAmount(dto.amount());
        counter.setDescription(dto.description() != null ? dto.description() : app.getOffers().get(app.getOffers().size() - 1).getDescription());
        counter.setOfferedBy(responder);
        counter.setCounterOffer(true);
        offers.save(counter);

        app.getOffers().add(counter);
        app.setStatus(ApplicationStatus.COUNTERED);
        app.setUpdatedAt(Instant.now());
        applications.save(app);

        String typeName = typeDisplayName(app.getType());

        AppUser toNotify = getOtherParty(app, responder);
        notificationService.notifyUser(
                toNotify.getId(),
                responder.getUsername() + " sendte et mottilbud: " + typeName + " (" + dto.amount() + " kr)",
                "/applications/" + app.getId()
        );

        return ApplicationDetailDto.from(app);
    }

    public ApplicationDetailDto getApplication(Long id, String username) {
        Application app = applications.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Søknad ikke funnet"));

        AppUser user = me(username);
        if (!hasAccess(app, user)) {
            throw new AccessDeniedException("Ikke tilgang");
        }

        return ApplicationDetailDto.from(app);
    }

    public List<ApplicationListDto> listApplications(String username) {
        AppUser user = me(username);

        if (isReceiver(user)) {
            // Receivers see: pending (unclaimed) + their own negotiations
            List<Application> mine = applications.findActiveForUser(username);
            List<Application> pending = applications.findPendingForReceivers();

            // Merge, avoid duplicates
            List<Long> myIds = mine.stream().map(Application::getId).toList();
            List<Application> combined = new java.util.ArrayList<>(mine);
            for (Application p : pending) {
                if (!myIds.contains(p.getId())) {
                    combined.add(p);
                }
            }
            combined.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
            return combined.stream().map(ApplicationListDto::from).toList();
        } else {
            // Senders see only their own
            return applications.findActiveSentByUser(username).stream()
                    .map(ApplicationListDto::from)
                    .toList();
        }
    }

    public List<ApplicationListDto> listHistory(String username) {
        me(username); // verify user exists
        return applications.findClosedForUser(username).stream()
                .map(ApplicationListDto::from)
                .toList();
    }

    public List<String> getMyRoles(String username) {
        AppUser user = me(username);
        return permissions.findByUserId(user.getId()).stream()
                .map(p -> p.getRole().name())
                .toList();
    }

    public void archive(Long id, String username) {
        Application app = applications.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Søknad ikke funnet"));

        AppUser user = me(username);
        if (!app.getSender().getId().equals(user.getId()) && !isReceiver(user)) {
            throw new AccessDeniedException("Ikke tilgang til å arkivere denne søknaden");
        }

        app.setActive(false);
        applications.save(app);
    }

    public List<String> getApplicationTypes() {
        return java.util.Arrays.stream(ApplicationType.values())
                .map(Enum::name)
                .toList();
    }

    public List<ApplicationListDto> listAllApplications(String username) {
        AppUser user = me(username);
        if (!isReceiver(user)) {
            throw new AccessDeniedException("Kun mottakere kan se alle søknader");
        }

        return applications.findAll().stream()
                .filter(Application::isActive)
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .map(ApplicationListDto::from)
                .toList();
    }

    private AppUser getOtherParty(Application app, AppUser current) {
        if (app.getSender().getId().equals(current.getId())) {
            return app.getRespondedBy();
        }
        return app.getSender();
    }

    private String typeDisplayName(ApplicationType type) {
        return switch (type) {
            case SPORTSFONDET -> "Sportsfondet";
            case ANNET -> "Annet";
        };
    }
}
