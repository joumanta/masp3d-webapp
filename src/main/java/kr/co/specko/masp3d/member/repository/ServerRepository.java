package kr.co.specko.masp3d.member.repository;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.CompanyBilling;
import kr.co.specko.masp3d.member.entity.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {

    public void deleteByCompany(Company company);

    List<Server> findByCompany(Company company);

    List<Server> findByCompanyAndName(Company company, String name);

    List<Server> findByName(String type);

    @Query("select new kr.co.specko.masp3d.member.entity.CompanyBilling(sum(b.price),b.company) from Billing b join b.company where b.baseDate = :baseDate group by b.company")
    List<CompanyBilling> findGroupByCompany(String baseDate);


    Server findByServerId(String serverId);

}
