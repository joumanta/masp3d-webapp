package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Page<User> findByAuthority(String authority, Pageable pageable);
    User findByAuthorityAndCompany(String authority, Company company);
    boolean existsByEmail(String email);
    Page<User> findByCompany(Company company, Pageable pageable);
    List<User> findByCompanyAndAuthority(Company company, String authority);
}
