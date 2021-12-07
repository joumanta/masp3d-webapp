package kr.co.specko.masp3d.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
public class CompanyBilling {

    private long price;
    private Company company;
}
