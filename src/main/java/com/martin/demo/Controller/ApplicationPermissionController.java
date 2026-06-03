package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.ApplicationPermissionDto;
import com.martin.demo.model.ApplicationPermission;
import com.martin.demo.model.ApplicationPermissionRole;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.ApplicationPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application-permissions")
public class ApplicationPermissionController {

    private final ApplicationPermissionRepository permissions;
    private final AppUserRepository users;

    public ApplicationPermissionController(ApplicationPermissionRepository permissions,
                                           AppUserRepository users) {
        this.permissions = permissions;
        this.users = users;
    }

    @GetMapping
    public List<ApplicationPermissionDto> list() {
        return permissions.findAll().stream()
                .map(p -> new ApplicationPermissionDto(
                        p.getId(),
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getRole().name()
                ))
                .toList();
    }

    @PostMapping
    public ApplicationPermissionDto add(@RequestParam String username,
                                        @RequestParam String role) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Bruker ikke funnet: " + username));

        ApplicationPermissionRole permRole = ApplicationPermissionRole.valueOf(role.toUpperCase());

        if (permissions.existsByUserIdAndRole(user.getId(), permRole)) {
            throw new IllegalArgumentException("Bruker har allerede denne tilgangen");
        }

        ApplicationPermission perm = new ApplicationPermission();
        perm.setUser(user);
        perm.setRole(permRole);
        permissions.save(perm);

        return new ApplicationPermissionDto(perm.getId(), user.getId(), user.getUsername(), perm.getRole().name());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        permissions.deleteById(id);
    }
}
