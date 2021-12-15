package kr.co.specko.masp3d.member.service;

import kr.co.specko.masp3d.UserService;
import kr.co.specko.masp3d.member.entity.RequestStatus;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.ServiceRequest;
import kr.co.specko.masp3d.member.entity.User;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import kr.co.specko.masp3d.member.repository.ServiceRequestRepository;
import kr.co.specko.masp3d.member.repository.UserRepository;
import kr.co.specko.masp3d.member.repository.UserServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final ServerRepository serverRepository;
    private final UserServiceRepository userServiceRepository;
    private final UserRepository userRepository;

    public ServiceRequest save(ServiceRequest serviceRequest) {
        return requestRepository.save(serviceRequest);
    }

    public void permit(User user, Long id) {
        ServiceRequest serviceRequest = requestRepository.findById(id).orElseThrow(IllegalAccessError::new);
        switch (serviceRequest.getServiceType()) {
            case "Basic":
                serviceRequest.getUser().setBasicEnabled(true);
                break;
            case "Professional":
                serviceRequest.getUser().setProfEnabled(true);
                break;
            case "Special":
                serviceRequest.getUser().setSpecialEnabled(true);
                break;
            case "쾌속 금형설계 검증 S/W":
                serviceRequest.getUser().setDezEnabled(true);
                break;
        }

        List<Server> serverList = serverRepository.findByCompanyAndName(user.getCompany(), serviceRequest.getServiceType());
        for(Server server : serverList) {
            UserService userService = new UserService();
            userService.setUser(user);
            userService.setServer(server);
            userServiceRepository.save(userService);

        }
        serviceRequest.setStatus(RequestStatus.PERMITTED);
        requestRepository.save(serviceRequest);
    }

    public void delete(User user, Long id) {
        ServiceRequest serviceRequest = requestRepository.findById(id).orElseThrow(IllegalAccessError::new);
        switch (serviceRequest.getServiceType()) {
            case "Basic":
                serviceRequest.getUser().setBasicEnabled(false);
                break;
            case "Professional":
                serviceRequest.getUser().setProfEnabled(false);
                break;
            case "Special":
                serviceRequest.getUser().setSpecialEnabled(false);
                break;
            case "쾌속 금형설계 검증 S/W":
                serviceRequest.getUser().setDezEnabled(false);
                break;
        }
        List<Server> servers = serverRepository.findByCompanyAndName(user.getCompany(),serviceRequest.getServiceType());
        for(Server server : servers) {
            userServiceRepository.deleteByUserAndServer(user, server);
        }
        serviceRequest.setStatus(RequestStatus.READY);
        requestRepository.save(serviceRequest);
    }
}
