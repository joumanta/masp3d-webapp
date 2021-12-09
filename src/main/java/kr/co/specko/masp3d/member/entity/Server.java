package kr.co.specko.masp3d.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String ip;

    private String serverId;

    private String serviceType;

    private String type;

    private String status;

    private String lastModifyDate;

    private Date startDate;
    private Date endDate;
    private String rdpUrl;


    @ManyToOne(fetch = FetchType.EAGER)
    private Company company;

}
