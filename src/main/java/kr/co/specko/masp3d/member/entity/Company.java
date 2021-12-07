package kr.co.specko.masp3d.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String bizNumber;
    private String bizOwner;

    @Transient
    private String tel1;
    @Transient
    private String tel2;
    @Transient
    private String tel3;

    private String tel;
    private String addr1;
    private String addr2;
    private String chargeName;

    private String chargeMobile;
    @Transient
    private String chargeMobile1;
    @Transient
    private String chargeMobile2;
    @Transient
    private String chargeMobile3;
    private String chargeEmail;
    private String taxEmail;

    private String file1;
    private String file2;
    private String file3;
    private String file4;
    private String file5;

    private String tenantId;
    private String token;
    private LocalDateTime tokenExpireTime;
    private String password;

    @CreatedDate
    private LocalDateTime createAt;

    @Transient
    private User adminUser;

}
