package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.member.entity.ServiceRequest;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceRequestRepositoryCustom {

    Page<ServiceRequest> findAllServiceRequest(String type, String search, Pageable pageable);

}
