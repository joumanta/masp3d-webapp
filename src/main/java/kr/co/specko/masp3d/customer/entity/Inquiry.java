package kr.co.specko.masp3d.customer.entity;


import kr.co.specko.masp3d.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String companyName;

    private String managerName;

    private String managerEmail;

    private String managerPhone;

    @Transient
    private String managerPhone1;
    @Transient
    private String managerPhone2;
    @Transient
    private String managerPhone3;



    private String visitPath;

    private String question;

    private String answer;

    private boolean completed;
}
