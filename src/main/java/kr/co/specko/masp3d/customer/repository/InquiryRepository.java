package kr.co.specko.masp3d.customer.repository;

import kr.co.specko.masp3d.customer.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
