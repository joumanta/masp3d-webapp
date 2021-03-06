package kr.co.specko.masp3d.member.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    void deleteByBaseDate(String baseDate);
    Page<Billing> findByBaseDateAndCompanyIdAndPriceGreaterThan(String  baseDate, Long companyId,int usageTime, Pageable pageable);
    Billing findByBaseDateAndServerId(String baseDate, String serverId);
    Billing findByServerId(String serverId);

    Page<Billing> findByBaseDateAndPriceGreaterThan(String baseDate, int usageTime, Pageable pageable);
}
