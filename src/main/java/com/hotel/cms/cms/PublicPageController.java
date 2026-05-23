package com.hotel.cms.cms;

import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.PageStatus;
import com.hotel.cms.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/pages")
public class PublicPageController {

    private final CmsPageRepository pageRepo;

    public PublicPageController(CmsPageRepository pageRepo) {
        this.pageRepo = pageRepo;
    }

    @GetMapping("/{slug}")
    public ApiResponse<CmsPage> getPublishedPage(@PathVariable String slug) {
        CmsPage page = pageRepo.findBySlugAndStatus(slug, PageStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published page not found"));

        return ApiResponse.ok("Published page", page);
    }
}