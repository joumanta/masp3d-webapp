package kr.co.specko.masp3d.member.service;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.User;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServiceRequestRepository;
import kr.co.specko.masp3d.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CompanyRepository companyRepository;

    public void save(User user, List<Map<String, String>> files) {

        Company company = user.getCompany();
        companyRepository.save(company);
        company.setFile1(files.get(0).get("saveFileName"));
        company.setFile2(files.get(1).get("saveFileName"));
        company.setFile3(files.get(2).get("saveFileName"));
        company.setFile4(files.get(3).get("saveFileName"));
        company.setFile5(files.get(4).get("saveFileName"));

        user.setAuthority("ROLE_ADMIN");
        userRepository.save(user);
    }

    public void deleteUser(User user) {
        serviceRequestRepository.deleteByUser(user);
        userRepository.deleteById(user.getId());
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByAuthorityAndBlocked("ROLE_USER",false, pageable);
    }

    public Page<User> findByCompany(Company company, Pageable pageable) {
        return userRepository.findByCompany(company,pageable);
    }

    public Page<User> findByAuthority(String authority, Pageable pageable) {
        return  userRepository.findByAuthorityAndBlocked(authority,false, pageable);
    }


    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(IllegalArgumentException::new);
    }

    public boolean existUser(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updateResetPasswordToken(String token, String name, String email)  {
        User user = userRepository.findByNameAndEmail(name, email).orElseThrow(() -> new IllegalArgumentException("해당 계정정보가 존재하지 않습니다."));
        if (user != null) {
            user.setResetPasswordToken(token);
            userRepository.save(user);
        }
    }

    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

}
