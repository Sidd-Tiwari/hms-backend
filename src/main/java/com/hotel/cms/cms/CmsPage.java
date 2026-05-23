package com.hotel.cms.cms;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.common.enums.PageStatus;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="cms_pages")
public class CmsPage extends BaseEntity {
 @Column(nullable=false, length=180) private String title;
 @Column(nullable=false, unique=true, length=220) private String slug;
 @Column(columnDefinition="MEDIUMTEXT") private String content;
 @Column(name="meta_title") private String metaTitle;
 @Column(name="meta_description", columnDefinition="TEXT") private String metaDescription;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private PageStatus status = PageStatus.DRAFT;
}
