package kr.co.specko.masp3d.member.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import static kr.co.specko.masp3d.member.entity.QCompany.company;
import static kr.co.specko.masp3d.member.entity.QUser.user;

@Repository
public class ServerRepositorySupport extends QuerydslRepositorySupport implements ServerRepositoryCustom {

    public ServerRepositorySupport() {
        super(Server.class);
    }


    @Override
    public Page<User> findByUser(String authority, String type, String search, Pageable pageable) {
        return null;
    }
}
