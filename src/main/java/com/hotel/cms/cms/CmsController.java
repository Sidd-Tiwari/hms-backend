package com.hotel.cms.cms;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.BadRequestException;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.user.UserAccount;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cms")
public class CmsController {
    private final CmsPageRepository pageRepo;
    private final CurrentUserService currentUserService;

    public CmsController(CmsPageRepository pageRepo, CurrentUserService currentUserService) {
        this.pageRepo = pageRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/pages")
    public ApiResponse<List<CmsPage>> pages(Principal principal) {
        assertSuperAdmin(principal);
        return ApiResponse.ok("CMS pages", pageRepo.findAll());
    }

    @PostMapping("/pages")
    public ApiResponse<CmsPage> create(Principal principal, @RequestBody CmsPage p) {
        assertSuperAdmin(principal);
        return ApiResponse.ok("Page created", pageRepo.save(p));
    }

    @PutMapping("/pages/{id}")
    public ApiResponse<CmsPage> update(Principal principal, @PathVariable String id, @RequestBody CmsPage p) {
        assertSuperAdmin(principal);
        p.setId(id);
        return ApiResponse.ok("Page updated", pageRepo.save(p));
    }

    @DeleteMapping("/pages/{id}")
    public ApiResponse<Void> delete(Principal principal, @PathVariable String id) {
        assertSuperAdmin(principal);
        CmsPage p = pageRepo.findById(id).orElseThrow(() -> new NotFoundException("Page not found"));
        pageRepo.delete(p);
        return ApiResponse.ok("Page deleted", null);
    }

    private void assertSuperAdmin(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        if (!currentUserService.isSuperAdmin(user)) {
            throw new BadRequestException("Only super admin can manage global CMS pages");
        }
    }
}
