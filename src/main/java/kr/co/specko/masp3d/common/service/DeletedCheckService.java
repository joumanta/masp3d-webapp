package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Billing;
import kr.co.specko.masp3d.member.entity.BillingRepository;
import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import kr.co.specko.masp3d.member.repository.UserServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletedCheckService {

    private final ServerRepository serverRepository;
    private final CompanyRepository companyRepository;
    private final NHNCloudRestService restService;
    private final BillingRepository billingRepository;
    private final UserServiceRepository userServiceRepository;

    @Transactional
    @Scheduled(fixedDelay = 1000*60*1)
    public void run() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        serverRepository.findAll().forEach((server) -> {
            try {
                Server serverInfo = restService.getServerInfo(server.getCompany().getTenantId(), server.getServerId());
                if(serverInfo == null) {    //삭제됨
                    server.setDeleted(true);
                    serverRepository.save(server);
                    userServiceRepository.deleteByServer(server);
//                    Billing billing = billingRepository.findByBaseDateAndServerId(sdf.format(now),server.getServerId());
//                    billing.setStatus("SHUTOFF");
//                    billing.setEndDate(new Date());
//                    billingRepository.save(billing);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }
}
