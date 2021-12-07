package kr.co.specko.masp3d.customer.service;

import kr.co.specko.masp3d.customer.entity.Faq;
import kr.co.specko.masp3d.customer.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository repository;

    public Faq save(Faq faq) {
        return repository.save(faq);
    }
}
