package kr.co.specko.masp3d.customer.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.customer.entity.QNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static kr.co.specko.masp3d.customer.entity.QNotice.*;

@Repository
public class NoticeRepositorySupport extends QuerydslRepositorySupport implements NoticeRepositoryCustom {

    public NoticeRepositorySupport() {
        super(Notice.class);
    }

    public Page<Notice> findAllNotice(String type, String search, Pageable pageable) {
        JPQLQuery<Notice> query = from(notice);

        BooleanBuilder bb = new BooleanBuilder();
        if(!ObjectUtils.isEmpty(search)) {

            if (type.equals("all")) {
                bb.and(notice.contents.contains(search)).or(notice.title.contains(search));
            } else {
                bb.and(notice.type.eq(type).and(notice.contents.contains(search).or(notice.title.contains(search))));
            }
        } else {
            if(!type.equals("all")) {
                bb.and(notice.type.eq(type));
            }
        }

        if(pageable != null) {
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }
        OrderSpecifier<Long> orderId = notice.id.desc();

        QueryResults<Notice> queryResults = query.where(bb).orderBy(orderId).fetchResults();

        return new PageImpl<Notice>(queryResults.getResults(),pageable, queryResults.getTotal());

    }
}
