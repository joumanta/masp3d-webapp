package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServerRepositoryCustom {

    Page<User> findByUser(String authority, String type, String search, Pageable pageable);
}
