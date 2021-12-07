package kr.co.specko.masp3d.customer.repository;

import kr.co.specko.masp3d.customer.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

    Page<Notice> findAllNotice(String type, String search, Pageable pageable);
}
