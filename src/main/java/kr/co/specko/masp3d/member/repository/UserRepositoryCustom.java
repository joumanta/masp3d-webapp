package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> findByAuthority(String authority, String type, String search, Pageable pageable);

    Page<User> findAllService(String type, String search, Pageable pageable);

}
