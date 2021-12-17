package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Billing;
import kr.co.specko.masp3d.member.entity.BillingRepository;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
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
    @Scheduled(fixedDelay = 1*60*1000)
    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date now = new Date();
        String baseDate = sdf.format(now);
        serverRepository.findAll().forEach((server -> {
            try {
                Server result = restService.getServerInfo(server.getCompany().getTenantId(), server.getServerId());
                log.info("{}", result);
                Billing billing = billingRepository.findByBaseDateAndServerId(baseDate, server.getServerId());
                if(result != null) {
                    if (billing == null) {
                        billing = new Billing();
                        billing.setBaseDate(baseDate);
                        billing.setCompany(server.getCompany());
                        billing.setIp(server.getIp());
                        billing.setServerName(server.getName());
                        if (result.getStatus().equals("ACTIVE")) {
                            billing.setUsageTime(1/60.0f);
                        }
                        billing.setType(server.getType());
                        billing.setStatus(result.getStatus());
                        billing.setServerId(server.getServerId());

                        billing.setStartDate(result.getStartDate());
                        billing.setEndDate(result.getEndDate());

                        if (result.getStatus().equals("ACTIVE")) {
                            if (server.getName().equals("Basic")) {
                                billing.setPrice((int) (5000 * (Math.ceil(billing.getUsageTime()))));
                            } else if (server.getName().equals("Special")) {
                                billing.setPrice((int) (15000 * (Math.ceil(billing.getUsageTime()))));
                            } else if (server.getName().equals("Professional")) {
                                billing.setPrice((int) (8000 * (Math.ceil(billing.getUsageTime()))));
                            } else {
                                billing.setPrice((int) (30000 * (Math.ceil(billing.getUsageTime()))));
                            }
                        }
                    } else {
                        billing.setBaseDate(baseDate);
                        if (result.getStatus().equals("ACTIVE")) {
                             billing.setUsageTime(billing.getUsageTime() + 1/60.f );
                        }

                        billing.setStatus(result.getStatus());
                        billing.setType(server.getType());
                        billing.setStartDate(result.getStartDate());
                        billing.setEndDate(result.getEndDate());

                        if (result.getStatus().equals("ACTIVE")) {
                            if (server.getName().equals("Basic")) {
                                billing.setPrice((int) (5000 * (Math.ceil(billing.getUsageTime()))));
                            } else if (server.getName().equals("Special")) {
                                billing.setPrice((int) (15000 * (Math.ceil(billing.getUsageTime()))));
                            } else if (server.getName().equals("Professional")) {
                                billing.setPrice((int) (8000 * (Math.ceil(billing.getUsageTime()))));
                            } else {
                                billing.setPrice((int) (30000 * (Math.ceil(billing.getUsageTime()))));
                            }
                        }

                    }
                    billingRepository.save(billing);
                }
            } catch (Exception e) {
            }
        }));

    }
}
