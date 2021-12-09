package kr.co.specko.masp3d.member.service;

import kr.co.specko.masp3d.member.entity.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillingRepository billingRepository;

    public void deleteByBaseDate(String baseDate) {
        billingRepository.deleteByBaseDate(baseDate);
    }
}
