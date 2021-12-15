package kr.co.specko.masp3d.customer.repository;

import kr.co.specko.masp3d.customer.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FaqRepositoryCustom {

    Page<Faq> findAllFaq(String type, String search, Pageable pageable);
}
