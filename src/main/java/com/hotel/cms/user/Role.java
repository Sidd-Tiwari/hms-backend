package com.hotel.cms.user;
import com.hotel.cms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="roles")
public class Role extends BaseEntity {
 @Column(nullable=false, unique=true, length=80) private String name;
 private String description;
}
