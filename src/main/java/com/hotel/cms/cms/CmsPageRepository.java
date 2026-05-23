package com.hotel.cms.cms;

import com.hotel.cms.common.enums.PageStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CmsPageRepository extends JpaRepository<CmsPage, String> {
    Optional<CmsPage> findBySlug(String slug);

    // Add this
    Optional<CmsPage> findBySlugAndStatus(String slug, PageStatus status);
}