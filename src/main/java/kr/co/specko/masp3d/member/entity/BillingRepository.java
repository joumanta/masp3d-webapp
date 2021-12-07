package kr.co.specko.masp3d.member.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    void deleteByBaseDate(String baseDate);
    List<Billing> findByBaseDateAndCompanyId(String  baseDate, Long companyId);
}
