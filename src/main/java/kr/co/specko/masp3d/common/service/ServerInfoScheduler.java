package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.UserService;
import kr.co.specko.masp3d.member.entity.*;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import kr.co.specko.masp3d.member.repository.UserServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerInfoScheduler {

    private final ServerRepository serverRepository;
    private final CompanyRepository companyRepository;
    private final NHNCloudRestService restService;
    private final BillingRepository billingRepository;
    private final UserServiceRepository userServiceRepository;

    @Transactional
    @Scheduled(fixedDelay = 1000*60*1)
    public void run() {
        long start = System.currentTimeMillis();
        List<Company> companyList = companyRepository.findAll();
        companyList.forEach((company -> {
            try {
                company.setProjectId(restService.getProjectId(company.getTenantId(), "!$VMtech1!"));
                companyRepository.save(company);
                List<Server> serverList = restService.getServerList(company.getTenantId(), company);
                for(Server server : serverList) {

                    log.info("{}",server);
                    Server s = serverRepository.findByServerId(server.getServerId());   // 없는 서버이면 서버타입별로
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
                    s.setImageId(server.getImageId());
                    serverRepository.save(s);

                    List<UserService> userServiceList = userServiceRepository.findAll();
                    for(UserService userService : userServiceList) {
                        if (userService.getServer().getName().equals(server.getName())) {
                            if(userServiceRepository.findByServer(s) == null) {
                                UserService newUserService = new UserService();
                                newUserService.setUser(userService.getUser());
                                newUserService.setServer(s);
                                userServiceRepository.save(newUserService);
                            }
                        }
                    }
                }

            }catch (Exception e){

            }
        }));

        long end = System.currentTimeMillis();
    }
}
