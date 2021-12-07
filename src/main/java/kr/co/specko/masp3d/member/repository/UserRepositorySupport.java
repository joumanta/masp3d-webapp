package kr.co.specko.masp3d.member.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.member.entity.QCompany;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static kr.co.specko.masp3d.customer.entity.QNotice.notice;
import static kr.co.specko.masp3d.member.entity.QCompany.company;
import static kr.co.specko.masp3d.member.entity.QUser.user;

@Repository
public class UserRepositorySupport extends QuerydslRepositorySupport implements UserRepositoryCustom {

    public UserRepositorySupport() {
        super(User.class);
    }

    @Override
    public Page<User> findByAuthority(String authority, String type, String search, Pageable pageable) {
        JPQLQuery<User> query = from(user)
                .innerJoin(user.company(), company);

        BooleanBuilder bb = new BooleanBuilder();
        bb.and(user.authority.eq(authority));

        if(!ObjectUtils.isEmpty(search)) {
            switch (type) {
                case "company":
                    bb.and(company.companyName.contains(search));
                    break;
                case "name":
                    bb.and(user.name.contains(search));
                    break;
                case "email":
                    bb.and(user.email.contains(search));
                    break;
            }
        }


        if(pageable != null) {
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }

        OrderSpecifier<Long> orderId = user.id.desc();
        QueryResults<User> queryResults = query.where(bb).orderBy(orderId).fetchResults();

        return new PageImpl<User>(queryResults.getResults(),pageable, queryResults.getTotal());
    }

    @Override
    public Page<User> findAllService(String type, String search, Pageable pageable) {
        JPQLQuery<User> query = from(user)
                .innerJoin(user.company(), company);
        BooleanBuilder bb = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(search)) {
            switch (type) {
                case "company":
                    bb.and(company.companyName.contains(search));
                    break;
                case "name":
                    bb.and(user.name.contains(search));
                    break;
                case "email":
                    bb.and(user.email.contains(search));
                    break;
            }
        }
        if(pageable != null) {
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }

        OrderSpecifier<Long> orderId = user.id.desc();
        QueryResults<User> queryResults = query.where(bb).orderBy(orderId).fetchResults();

        return new PageImpl<User>(queryResults.getResults(),pageable, queryResults.getTotal());

    }

}
