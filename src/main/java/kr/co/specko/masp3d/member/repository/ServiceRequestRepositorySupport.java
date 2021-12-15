package kr.co.specko.masp3d.member.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.member.entity.QServer;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import static kr.co.specko.masp3d.member.entity.QCompany.company;
import static kr.co.specko.masp3d.member.entity.QServer.server;
import static kr.co.specko.masp3d.member.entity.QServiceRequest.serviceRequest;
import static kr.co.specko.masp3d.member.entity.QUser.user;

@Repository
public class ServiceRequestRepositorySupport extends QuerydslRepositorySupport implements ServiceRequestRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public ServiceRequestRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Notice.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }


    @Override
    public Page<ServiceRequest> findAllServiceRequest(String type, String search, Pageable pageable) {

        JPAQuery<ServiceRequest> query = jpaQueryFactory.select(Projections.fields(ServiceRequest.class,
                serviceRequest.id,serviceRequest.serviceType, user))
                .from(serviceRequest)
                .join(serviceRequest.user(), user);

        BooleanBuilder bb = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(search)) {
            switch (type) {
                case "company":
                    bb.and(serviceRequest.user().company().companyName.contains(search));
                    break;
                case "name":
                    bb.and(serviceRequest.user().user.name.contains(search));
                    break;
                case "email":
                    bb.and(serviceRequest.user().email.contains(search));
                    break;
            }
        }
        if(pageable != null) {
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }

        OrderSpecifier<Long> orderId = serviceRequest.id.desc();
        QueryResults<ServiceRequest> queryResults = query.where(bb).orderBy(orderId).fetchResults();

        return new PageImpl<ServiceRequest>(queryResults.getResults(),pageable, queryResults.getTotal());

    }
}
