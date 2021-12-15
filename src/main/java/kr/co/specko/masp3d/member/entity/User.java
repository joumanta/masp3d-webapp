package kr.co.specko.masp3d.member.entity;

import kr.co.specko.masp3d.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String name;

    private boolean enabled;

    private boolean blocked;

    private String status;

    private String authority;

    private String tenantId;

    private String mobile;

    @Transient
    private String mobile1;
    @Transient
    private String mobile2;
    @Transient
    private String mobile3;

    @ManyToOne(fetch = FetchType.EAGER)
    private Company company;

    private boolean basicEnabled;

    private boolean profEnabled;

    private boolean specialEnabled;

    private boolean dezEnabled;

    private String resetPasswordToken;

    @Transient
    private List<Server> serverList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton((GrantedAuthority) () -> authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
