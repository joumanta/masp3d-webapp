package kr.co.specko.masp3d.customer.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import kr.co.specko.masp3d.customer.entity.Faq;
import kr.co.specko.masp3d.customer.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import static kr.co.specko.masp3d.customer.entity.QFaq.faq;
import static kr.co.specko.masp3d.customer.entity.QNotice.notice;

@Repository
public class FaqRepositorySupport extends QuerydslRepositorySupport implements FaqRepositoryCustom {

    public FaqRepositorySupport() {
        super(Faq.class);
    }

    public Page<Faq> findAllFaq(String type, String search, Pageable pageable) {
        JPQLQuery<Faq> query = from(faq);

        BooleanBuilder bb = new BooleanBuilder();
        if(!ObjectUtils.isEmpty(search)) {

            if (type.equals("all")) {
                bb.and(faq.contents.contains(search)).or(faq.title.contains(search));
            } else if(type.equals("title")) {
                bb.and(faq.title.contains(search));
            } else if(type.equals("contents")) {
                bb.and(faq.contents.contains(search));
            }
        }
        if(pageable != null) {
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }
        OrderSpecifier<Long> orderId = faq.id.desc();

        QueryResults<Faq> queryResults = query.where(bb).orderBy(orderId).fetchResults();

        return new PageImpl<Faq>(queryResults.getResults(),pageable, queryResults.getTotal());

    }
}
