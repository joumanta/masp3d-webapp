package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Billing;
import kr.co.specko.masp3d.member.entity.BillingRepository;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final ServerRepository serverRepository;
    private final BillingRepository billingRepository;
    private final NHNCloudRestService restService;

    @Transactional
    @Scheduled(fixedDelay = 60*60*1000)
    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date now = new Date();
        String baseDate = sdf.format(now);
        serverRepository.findAll().forEach((server -> {
            try {
                Server result = restService.getServerInfo(server.getCompany().getTenantId(), server.getServerId());
                log.info("{}", result);
                Billing billing = billingRepository.findByBaseDateAndServerId(baseDate, server.getServerId());
                if (billing == null) {
                    billing = new Billing();
                    billing.setBaseDate(baseDate);
                    billing.setCompany(server.getCompany());
                    billing.setIp(server.getIp());
                    billing.setServerName(server.getName());
                    billing.setUsageTime(1);
                    billing.setServerId(server.getServerId());
                    billing.setStartDate(server.getStartDate());
                    billing.setEndDate(result.getEndDate());
                    if (server.getName().equals("Basic")) {
                        billing.setPrice(5000 * billing.getUsageTime());
                    } else if (server.getName().equals("Special")) {
                        billing.setPrice(15000 * billing.getUsageTime());
                    } else if (server.getName().equals("Professional")) {
                        billing.setPrice(8000 * billing.getUsageTime());
                    } else {
                        billing.setPrice(30000 * billing.getUsageTime());
                    }
                } else {
                    billing.setUsageTime(billing.getUsageTime() + 1);
                    billing.setEndDate(result.getEndDate());
                    if (server.getName().equals("Basic")) {
                        billing.setPrice(5000 * (billing.getUsageTime() + 1));
                    } else if (server.getName().equals("Special")) {
                        billing.setPrice(15000 * (billing.getUsageTime() + 1));
                    } else if (server.getName().equals("Professional")) {
                        billing.setPrice(8000 * (billing.getUsageTime() + 1));
                    } else {
                        billing.setPrice(30000 * (billing.getUsageTime() + 1));
                    }
                }
                billingRepository.save(billing);
            } catch (Exception e) {

            }
        }));

    }
}
