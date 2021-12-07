package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerInfoScheduler {

    private final ServerRepository serverRepository;
    private final CompanyRepository companyRepository;
    private final NHNCloudRestService restService;

    @Transactional
    @Scheduled(fixedDelay = 1000*60*5)
    public void run() {
        List<Company> companyList = companyRepository.findAll();
        companyList.forEach((company -> {
            try {
                List<Server> serverList = restService.getServerList(company.getTenantId(), company);
                for(Server server : serverList) {
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
                }

            }catch (Exception e){

            }
        }));
    }
}
