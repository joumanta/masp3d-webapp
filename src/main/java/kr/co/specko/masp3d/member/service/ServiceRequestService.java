package kr.co.specko.masp3d.member.service;

import kr.co.specko.masp3d.member.entity.RequestStatus;
import kr.co.specko.masp3d.member.entity.ServiceRequest;
import kr.co.specko.masp3d.member.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;

    public ServiceRequest save(ServiceRequest serviceRequest) {
        return requestRepository.save(serviceRequest);
    }

    public void permit(Long id) {
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
        serviceRequest.setStatus(RequestStatus.PERMITTED);
        requestRepository.save(serviceRequest);
    }

    public void delete(Long id) {
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
        serviceRequest.setStatus(RequestStatus.READY);
        requestRepository.save(serviceRequest);
    }
}
