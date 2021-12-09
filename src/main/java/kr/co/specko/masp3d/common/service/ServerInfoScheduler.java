package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Billing;
import kr.co.specko.masp3d.member.entity.BillingRepository;
import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerInfoScheduler {

    private final ServerRepository serverRepository;
    private final CompanyRepository companyRepository;
    private final NHNCloudRestService restService;
    private final BillingRepository billingRepository;

    @Transactional
    @Scheduled(fixedDelay = 1000*60*5)
    public void run() {
        long start = System.currentTimeMillis();
        List<Company> companyList = companyRepository.findAll();
        companyList.forEach((company -> {
            try {
                List<Server> serverList = restService.getServerList(company.getTenantId(), company);
                for(Server server : serverList) {

                    log.info("{}",server);
                    Server s = serverRepository.findByServerId(server.getServerId());
                    if(s == null) {
                        s = new Server();
                    }
                    s.setServerId(server.getServerId());
                    s.setStatus(server.getStatus());
                    s.setIp(server.getIp());
                    s.setCompany(company);
                    s.setStartDate(server.getStartDate());
                    s.setEndDate(server.getEndDate());
                    s.setName(server.getName());
                    s.setType(server.getType());
                    serverRepository.save(s);

                    Billing billing = billingRepository.findByServerId(server.getServerId());
                    if(billing != null) {
                        billing.setStatus(s.getStatus());
                        billing.setEndDate(s.getEndDate());
                        billingRepository.save(billing);
                    }

                }

            }catch (Exception e){

            }
        }));
        long end = System.currentTimeMillis();
        System.out.println( "실행 시간 : " + ( end - start )/1000.0 +"초");
    }
}
