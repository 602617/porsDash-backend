package com.martin.demo.Controller;

import com.martin.demo.dto.*;
import com.martin.demo.service.ApplicationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping("/types")
    public List<String> getTypes() {
        return service.getApplicationTypes();
    }

    @PostMapping
    public ApplicationDetailDto create(@RequestBody CreateApplicationDto dto, Authentication auth) {
        return service.create(dto, auth.getName());
    }

    @GetMapping
    public List<ApplicationListDto> list(Authentication auth) {
        return service.listApplications(auth.getName());
    }

    @GetMapping("/history")
    public List<ApplicationListDto> history(Authentication auth) {
        return service.listHistory(auth.getName());
    }

    @GetMapping("/my-role")
    public List<String> myRole(Authentication auth) {
        return service.getMyRoles(auth.getName());
    }

    @GetMapping("/{id}")
    public ApplicationDetailDto get(@PathVariable Long id, Authentication auth) {
        return service.getApplication(id, auth.getName());
    }

    @PostMapping("/{id}/respond")
    public ApplicationDetailDto respond(@PathVariable Long id,
                                        @RequestBody RespondApplicationDto dto,
                                        Authentication auth) {
        return service.respond(id, dto, auth.getName());
    }

    @DeleteMapping("/{id}")
    public void archive(@PathVariable Long id, Authentication auth) {
        service.archive(id, auth.getName());
    }
}
