package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.common.entity.ServerAction;
import kr.co.specko.masp3d.common.repository.EmailSendRepository;
import kr.co.specko.masp3d.common.repository.ServerActionRepository;
import kr.co.specko.masp3d.common.utils.MailUtil;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerActionScheduler {

    private final ServerActionRepository serverActionRepository;
    private final ServerRepository serverRepository;
    private final NHNCloudRestService restService;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void run() {
        List<ServerAction> serverActions = serverActionRepository.findAll();

        for(ServerAction serverAction : serverActions) {
            Server server = serverRepository.findByServerId(serverAction.getServerId());
            String tenantId = server.getCompany().getTenantId();
            try {
                if (serverAction.getAction().equals("start")) {
                    restService.startInstance(tenantId, server.getServerId());
                } else {
                    restService.stopInstance(tenantId, server.getServerId());
                }
            } catch (Exception e) {

            }
            serverActionRepository.delete(serverAction);
        }
    }
}
