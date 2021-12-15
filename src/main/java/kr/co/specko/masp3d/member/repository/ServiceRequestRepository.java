package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.ServiceRequest;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Page<ServiceRequest> findByCompany(Company company, Pageable pageable);
    ServiceRequest findByUserAndServiceType(User user, String serviceType);
    void deleteByUser(User user);
}
