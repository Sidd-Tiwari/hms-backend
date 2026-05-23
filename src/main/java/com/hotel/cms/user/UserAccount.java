package com.hotel.cms.user;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hotel.cms.common.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet; import java.util.Set;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) @Table(name="users")
public class UserAccount extends BaseEntity {
 @Column(name="full_name", nullable=false, length=160) private String fullName;
 @Column(nullable=false, unique=true, length=180) private String email;
 private String phone;
 @JsonIgnore @Column(name="password_hash", nullable=false) private String passwordHash;
 @Column(name="is_active", nullable=false) private boolean active = true;
 @Column(name="last_login") private Instant lastLogin;
 @ManyToMany(fetch=FetchType.EAGER)
 @JoinTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
 private Set<Role> roles = new HashSet<>();
}
