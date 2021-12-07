package kr.co.specko.masp3d.config;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.User;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class InitDB {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;

    @Bean
    @Transactional
    public void initializeDefaultUser() {

//        Company byId = companyRepository.findById(12L).orElseThrow(IllegalAccessError::new);
        String encode = passwordEncoder.encode("1234");
        System.out.println("");

       /* User user = User.builder()
                    .email("joumanta@gmail.com")
                    .password(passwordEncoder.encode("1234"))
                    .authority("ROLE_ADMIN")
                    .enabled(true)
                    .company(byId)
                    .build();
            memberService.save(user);*/

    }


}
