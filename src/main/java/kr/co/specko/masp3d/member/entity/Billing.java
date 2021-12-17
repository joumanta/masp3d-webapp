package kr.co.specko.masp3d.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceType;

    private String baseDate;

    private Date startDate;

    private Date endDate;

    private String ip;

    private String type;

    private String status;

    private String serverName;

    private int price;

    private float usageTime;

    @Transient
    private int usageHour;

    private String serverId;


    @ManyToOne
    private Company company;

}
