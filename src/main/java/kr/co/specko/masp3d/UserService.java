package kr.co.specko.masp3d;

import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.User;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    @ManyToOne
    private Server server;
}
