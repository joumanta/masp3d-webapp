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
    @Scheduled(fixedDelay = 10*60*1000)
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
                    if(result.getStatus().equals("ACTIVE")) {
                        billing.setUsageTime(1);
                    }
                    billing.setType(server.getType());
                    billing.setStatus(result.getStatus());
                    billing.setServerId(server.getServerId());
                    billing.setStartDate(result.getStartDate());
                    if(!result.getStatus().equals("ACTIVE")) {
                        billing.setEndDate(result.getEndDate());
                    } else {
                        billing.setEndDate(null);
                    }
                    if(result.getStatus().equals("ACTIVE")) {
                        if (result.getName().equals("Basic")) {
                            billing.setPrice(5000 * billing.getUsageTime());
                        } else if (result.getName().equals("Special")) {
                            billing.setPrice(15000 * billing.getUsageTime());
                        } else if (result.getName().equals("Professional")) {
                            billing.setPrice(8000 * billing.getUsageTime());
                        } else {
                            billing.setPrice(30000 * billing.getUsageTime());
                        }
                    }
                } else {
                    billing.setBaseDate(baseDate);
                    if(result.getStatus().equals("ACTIVE")) {
                        billing.setUsageTime(billing.getUsageTime() + 1);
                    }
                    if(result.getStatus().equals("SHUTOFF")) {
                        Date date = DateUtils.addMinutes(now, -10);
                        // 업데이트 시간이 현재시간과 10분전시간사이에 있으면 +1
                        if(date.getTime() <= result.getEndDate().getTime() && result.getEndDate().getTime() <= now.getTime()) {
                            billing.setUsageTime(billing.getUsageTime() + 1);
                        }
                    }
                    billing.setStatus(result.getStatus());
                    billing.setType(server.getType());
                    if(!result.getStatus().equals("ACTIVE")) {
                        billing.setEndDate(result.getEndDate());
                    } else {
                        billing.setEndDate(null);
                    }
                    if(server.getStatus().equals("ACTIVE")) {
                        if (server.getName().equals("Basic")) {
                            billing.setPrice(5000 * (billing.getUsageTime()));
                        } else if (server.getName().equals("Special")) {
                            billing.setPrice(15000 * (billing.getUsageTime()));
                        } else if (server.getName().equals("Professional")) {
                            billing.setPrice(8000 * (billing.getUsageTime()));
                        } else {
                            billing.setPrice(30000 * (billing.getUsageTime()));
                        }
                    }
                }
                billingRepository.save(billing);
            } catch (Exception e) {

            }
        }));

    }
}
