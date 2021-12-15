package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.UserService;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserServiceRepository extends JpaRepository<UserService, Long> {

    List<UserService> findByUser(User user);
    void deleteByUserAndServer(User user, Server server);
}
