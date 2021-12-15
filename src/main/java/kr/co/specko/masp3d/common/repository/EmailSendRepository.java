package kr.co.specko.masp3d.common.repository;

import kr.co.specko.masp3d.common.entity.EmailSend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendRepository extends JpaRepository<EmailSend, Long> {
}
