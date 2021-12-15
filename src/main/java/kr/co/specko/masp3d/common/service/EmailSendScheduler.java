package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.common.entity.EmailSend;
import kr.co.specko.masp3d.common.repository.EmailSendRepository;
import kr.co.specko.masp3d.common.utils.MailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSendScheduler {

    private final MailUtil mailUtil;
    private final EmailSendRepository emailSendRepository;

    @Transactional
    @Scheduled(fixedDelay = 2000)
    public void run() {

        emailSendRepository.findAll().forEach((emailSend) -> {
            try {
                mailUtil.sendTemplateMail(emailSend.getEmail(),emailSend.getSubject(),emailSend.getSender(),emailSend.getContents());
                emailSendRepository.delete(emailSend);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
