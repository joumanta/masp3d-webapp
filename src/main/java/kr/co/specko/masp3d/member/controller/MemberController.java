package kr.co.specko.masp3d.member.controller;

import javassist.Loader;
import kr.co.specko.masp3d.common.utils.FileUploadUtil;
import kr.co.specko.masp3d.member.entity.*;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import kr.co.specko.masp3d.member.repository.ServiceRequestRepository;
import kr.co.specko.masp3d.member.repository.UserRepository;
import kr.co.specko.masp3d.member.service.MemberService;
import kr.co.specko.masp3d.member.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final FileUploadUtil uploadUtil;
    private final MemberService memberService;
    private final PasswordEncoder encoder;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestService serviceRequestService;
    private final BillingRepository billingRepository;

    @GetMapping("/signup")
    public String signupForm() {
        return "pages/member/signup";
    }

    @GetMapping("/signup_step2")
    public String signupStep2Form() {
        return "pages/member/signup_step2";
    }

    @PostMapping("/signup_step3")
    public String signupStep3Form() {
        return "pages/member/signup_step3";
    }

    @PostMapping("/signup_step4")
    public String signupStep4Form(User user, Company company, MultipartHttpServletRequest request) throws IOException {
        Map<String, String> upload = uploadUtil.upload(request);
        user.setPassword(encoder.encode(user.getPassword()));
        company.setChargeMobile(company.getChargeMobile1() + "-" + company.getChargeMobile2() + "-" + company.getChargeMobile3());
        company.setTel(company.getTel1() + "-" + company.getTel2() + "-" + company.getTel3());
        user.setCompany(company);
        user.setName(company.getChargeName());
        memberService.save(user, Collections.singletonList(upload.get("saveFileName")));
        return "redirect:/member/signup_success";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member_add")
    public String memberAdd() {
        return "pages/member/member_add";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/member_add")
    public String memberAddAction(User user,Authentication authentication) {
        User loginUser = (User) authentication.getPrincipal();
        User adminUser = memberService.findById(loginUser.getId());
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCompany(adminUser.getCompany());
        user.setAuthority("ROLE_USER");
        user.setMobile(user.getMobile1()+"-"+user.getMobile2()+"-"+user.getMobile3());
        user.setEnabled(true);
        memberService.save(user);
        return "redirect:/member/member_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member_list")
    public String memberList(@RequestParam(name = "page") Optional<Integer> page,
                             Model model,Authentication authentication) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        User user = (User) authentication.getPrincipal();
        Page<User> list = memberService.findByCompany(user.getCompany(), pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        return "pages/member/member_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/download")
    public void downloadFile(@RequestParam("id") Long id,
                             @RequestParam("gb") int gb,
                             HttpServletResponse response, @Value("${upload.dir}") String dir) throws Exception {
        User user = memberService.findById(id);
        String fileName = "";
        String newFileName = user.getCompany().getCompanyName();
        String ext = "";
        switch (gb) {
            case 1:
                fileName = user.getCompany().getFile1();
                newFileName += "_신청서";
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                break;
            case 2:
                fileName = user.getCompany().getFile2();
                newFileName += "_서약서";
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                break;
            case 3:
                fileName = user.getCompany().getFile3();
                newFileName += "_법인인감";
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                break;
            case 4:
                fileName = user.getCompany().getFile4();
                newFileName += "_사용인감계";
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                break;
            case 5:
                fileName = user.getCompany().getFile5();
                newFileName += "_등록증";
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                break;
        }
        byte[] fileByte = FileUtils.readFileToByteArray(new File(dir,fileName));

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(newFileName+"."+ext, "UTF-8")+"\";");
        response.setHeader("Content-Transfer-Encoding", "binary");

        response.getOutputStream().write(fileByte);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member_detail")
    public String memberDetail() {
        return "pages/member/member_detail";
    }

    @PreAuthorize("hasAnyRole('ADMIN,USER')")
    @GetMapping("/servicelist")
    public String servicelist(@RequestParam(name = "page") Optional<Integer> page,Authentication authentication, Model model) {

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10,Sort.by("id").descending());
        User user = (User) authentication.getPrincipal();
        List<Server> serverList = new ArrayList<>();
        User findUser = userRepository.findById(user.getId()).orElseThrow(IllegalAccessError::new);
        if(findUser.isBasicEnabled()) {
            serverList.addAll(serverRepository.findByCompanyAndName(user.getCompany(),"Basic"));
        }
        if(findUser.isProfEnabled()) {
            serverList.addAll(serverRepository.findByCompanyAndName(user.getCompany(), "Professional"));
        }
        if(findUser.isDezEnabled()) {
            serverList.addAll(serverRepository.findByCompanyAndName(user.getCompany(), "쾌속 금형설 검증 S/W"));
        }
        if(findUser.isSpecialEnabled()) {
            serverList.addAll(serverRepository.findByCompanyAndName(user.getCompany(), "Special"));
        }
        model.addAttribute("serverList", serverList);
        Page<ServiceRequest> requestList = serviceRequestRepository.findByCompany(user.getCompany(), pageable);
        model.addAttribute("requestList", requestList);
        return "pages/member/servicelist";
    }

    @PostMapping("/service_permit")
    public @ResponseBody String servicePermit(Long id) {
        serviceRequestService.permit(id);
        return "OK";
    }

    @PostMapping("/service_delete")
    public @ResponseBody String serviceDelete(Long id) {
        serviceRequestService.delete(id);
        return "OK";
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/service_add")
    public @ResponseBody String serviceadd(Authentication authentication, String serviceType, String cause, Model model) {
        User user = (User) authentication.getPrincipal();
        ServiceRequest findRequest = serviceRequestRepository.findByUserAndServiceType(user, serviceType);
        if(findRequest != null) {
            return "FAIL";
        } else {
            ServiceRequest serviceRequest = ServiceRequest.builder()
                    .serviceType(serviceType)
                    .cause(cause)
                    .status(RequestStatus.READY)
                    .company(user.getCompany())
                    .user(user)
                    .build();
            serviceRequestService.save(serviceRequest);
        }
        return "OK";
    }

    @GetMapping("/check_email")
    public @ResponseBody boolean checkEmail(@RequestParam(name = "email") String email) {
        return !memberService.existUser(email);
    }

    @GetMapping("/signup_success")
    public String signupSuccess() {
        return "pages/member/signup_step4";
    }

    // TODO 로그인 구현
    @GetMapping("/login")
    public String loginForm() {
        return "pages/member/login";
    }

    @GetMapping("/findid")
    public String findIdForm() {
        return "pages/member/findid";
    }

    @PreAuthorize("!isAnonymous()")
    @GetMapping("/findpw")
    public String findpwForm() {
        return "pages/member/findpw_new";
    }

    @GetMapping("/member_info")
    public String memberInfo(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = memberService.findByEmail(email);
        model.addAttribute("member", user);
        return "pages/member/member_info";
    }

    @PostMapping("/member_info")
    public void memberInfoAction(Authentication authentication, Company company, HttpServletResponse response) throws IOException {
        User user = (User) authentication.getPrincipal();
        Company resultCompany = user.getCompany();
        resultCompany.setBizOwner(company.getBizOwner());
        resultCompany.setTel(company.getTel1() + "-" + company.getTel2() + "-" + company.getTel3());
        resultCompany.setAddr1(company.getAddr1());
        resultCompany.setAddr2(company.getAddr2());
        resultCompany.setChargeName(company.getChargeName());
        resultCompany.setChargeMobile(company.getChargeMobile1() + "-" + company.getChargeMobile2() + "-" + company.getChargeMobile3());
        resultCompany.setChargeEmail(company.getChargeEmail());
        resultCompany.setTaxEmail(company.getTaxEmail());
        memberService.save(user);

        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('수정이 완료되었습니다.');parent.location.reload();</script>");
        writer.flush();
    }

    @GetMapping("/payment")
    public String payment(@DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model, Authentication authentication) throws ParseException {
        User user = (User)authentication.getPrincipal();

        if(searchDate == null) {
            searchDate = new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(searchDate);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));

        model.addAttribute("searchDate", searchDate);
        model.addAttribute("firstDate", calendar.getTime());

        calendar = Calendar.getInstance();
        calendar.setTime(searchDate);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));

        model.addAttribute("lastDate", calendar.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        List<Billing> list = billingRepository.findByBaseDateAndCompanyId(sdf.format(searchDate), user.getCompany().getId());
        long total = 0;
        for(Billing billing : list) {
            total += billing.getPrice();
        }
        model.addAttribute("total", total);
        model.addAttribute("list", list);


        List<Date> searchDates = new ArrayList<>();
        Date date = new Date();
        for(int i=0;i<10;i++) {
            searchDates.add(date);
            date = DateUtils.addMonths(date, -1);
        }
        model.addAttribute("searchDates", searchDates);


        return "pages/member/payment";
    }

}
