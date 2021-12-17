package kr.co.specko.masp3d.member.controller;

import javassist.Loader;
import kr.co.specko.masp3d.UserService;
import kr.co.specko.masp3d.common.entity.EmailSend;
import kr.co.specko.masp3d.common.entity.ServerAction;
import kr.co.specko.masp3d.common.repository.EmailSendRepository;
import kr.co.specko.masp3d.common.repository.ServerActionRepository;
import kr.co.specko.masp3d.common.service.NHNCloudRestService;
import kr.co.specko.masp3d.common.utils.FileUploadUtil;
import kr.co.specko.masp3d.common.utils.MailUtil;
import kr.co.specko.masp3d.common.utils.Utility;
import kr.co.specko.masp3d.member.entity.*;
import kr.co.specko.masp3d.member.repository.*;
import kr.co.specko.masp3d.member.service.MemberService;
import kr.co.specko.masp3d.member.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

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
    private final MailUtil mailUtil;
    private final EmailSendRepository emailSendRepository;
    private final TemplateEngine htmlTemplateEngine;
    private final NHNCloudRestService nhnCloudRestService;
    private final ServerActionRepository serverActionRepository;
    private final CompanyRepository companyRepository;
    private final UserServiceRepository userServiceRepository;

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
        List<Map<String, String>> upload = uploadUtil.upload(request);
        user.setPassword(encoder.encode(user.getPassword()));
        company.setChargeMobile(company.getChargeMobile1() + "-" + company.getChargeMobile2() + "-" + company.getChargeMobile3());
        company.setTel(company.getTel1() + "-" + company.getTel2() + "-" + company.getTel3());
        user.setCompany(company);
        user.setName(company.getChargeName());
        memberService.save(user, upload);
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
        List<UserService> userServiceList = userServiceRepository.findByUser(user);
        model.addAttribute("serverList", userServiceList);
        Page<ServiceRequest> requestList = serviceRequestRepository.findByCompany(user.getCompany(), pageable);
        model.addAttribute("requestList", requestList);
        return "pages/member/servicelist";
    }

    @PostMapping("/service_permit")
    public @ResponseBody String servicePermit(Authentication authentication,Long id) {
        ServiceRequest request = serviceRequestRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        User user = request.getUser();
        serviceRequestService.permit(user,id);
        return "OK";
    }



    @PostMapping("/server_action")
    public @ResponseBody String serverAction(@RequestParam(name = "type") String type, @RequestParam(name = "id") Long id) {

        Server server = serverRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        ServerAction serverAction = new ServerAction();
        serverAction.setAction(type);
        serverAction.setServerId(server.getServerId());
        serverActionRepository.save(serverAction);
        if(type.equals("start")) {
            return "서버 시작 요청되었습니다.";
        } else {
            return  "서버 정지 요청되었습니다.";
        }
    }

    @PostMapping("/service_delete")
    public @ResponseBody String serviceDelete(Long id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        User user = serviceRequest.getUser();
        serviceRequestService.delete(user,id);
        return "OK";
    }

    @PostMapping("/member_delete")
    public @ResponseBody String memberDelete(@RequestParam(name = "id") Long id) {
        User user = userRepository.findById(id).orElseThrow(NullPointerException::new);
        memberService.deleteUser(user);
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
                    .company(user.getCompany())
                    .status(RequestStatus.READY)
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

    @PostMapping("/findid")
    public String findIdAction(@RequestParam("name") String name,
                               @RequestParam("mobile") String mobile, Model model, RedirectAttributes attributes) {
        mobile = convertTelNo(mobile);
        User findUser = userRepository.findByNameAndMobile(name, mobile);
            attributes.addFlashAttribute("user", findUser);
        return "redirect:/member/findid_result";
    }

    @GetMapping("/findid_result")
    public String findIdResult(Model model) {
        return "pages/member/findid_result";
    }

    public static String convertTelNo(String src) {

        String mobTelNo = src;

        if (mobTelNo != null) {
            // 일단 기존 - 전부 제거
            mobTelNo = mobTelNo.replaceAll(Pattern.quote("-"), "");

            if (mobTelNo.length() == 11) {
                // 010-1234-1234
                mobTelNo = mobTelNo.substring(0, 3) + "-" + mobTelNo.substring(3, 7) + "-" + mobTelNo.substring(7);

            } else if (mobTelNo.length() == 8) {
                // 1588-1234
                mobTelNo = mobTelNo.substring(0, 4) + "-" + mobTelNo.substring(4);
            } else {
                if (mobTelNo.startsWith("02")) { // 서울은 02-123-1234
                    mobTelNo = mobTelNo.substring(0, 2) + "-" + mobTelNo.substring(2, 5) + "-" + mobTelNo.substring(5);
                } else { // 그외는 012-123-1345
                    mobTelNo = mobTelNo.substring(0, 3) + "-" + mobTelNo.substring(3, 6) + "-" + mobTelNo.substring(6);
                }
            }

        }

        return mobTelNo;
    }


    @GetMapping("/forgot_password")
    public String forgotPassword() {
        return "pages/member/findpw";
    }

    @PostMapping("/forgot_password")
    public void forgotPasswordAction(@RequestParam(name = "name") String name, @RequestParam(name = "email")String email,HttpServletRequest request,HttpServletResponse response) throws Exception {
        String token = RandomString.make(30);
        memberService.updateResetPasswordToken(token,name, email);
//        String resetPasswordLink = Utility.getSiteURL(request) + "/member/reset_password?token=" + token;
        String resetPasswordLink = "http://cloud.vmtech.co.kr/member/reset_password?token=" + token;
        Map<String,Object> map =new HashMap<>();
        map.put("url", resetPasswordLink);

        Context context = new Context();
        context.setVariables(map);

        String html = htmlTemplateEngine.process("email/member_password", context);

        EmailSend emailSend = new EmailSend();
        emailSend.setContents(html);
        emailSend.setSubject("[vmtech]비밀번호 변경 안내 메일입니다.");
        emailSend.setSender("(주)브이엠테크");
        emailSend.setEmail(email);

        emailSendRepository.save(emailSend);

//        mailUtil.sendTemplateMail(email,"비밀번호 변경 안내입니다.","(주)브이엠테크","email/member_password",map);
//        return "redirect:/member/findpw_email";
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>parent.location.href='/member/findpw_email';</script>");
        writer.flush();
        writer.close();;

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handle(IllegalArgumentException e,HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('" + e.getMessage() + "');</script>");
        writer.flush();
        writer.close();
    }

    @GetMapping("/findpw_email")
    public String findpwEmail() {
        return "pages/member/findpw_email";
    }

    @GetMapping("/reset_password")
    public String findpwForm(String token, Model model) {
        User user = userRepository.findByResetPasswordToken(token);
        model.addAttribute("token", token);
        return "pages/member/findpw_new";
    }

    @GetMapping("/change_password")
    public String changePass() {
        return "pages/member/changepw";
    }

    @PostMapping("/change_password")
    public void changePassAction(@RequestParam(name = "password") String password, Authentication authentication,HttpServletResponse response) throws IOException {
        String email = authentication.getName();
        User user = memberService.findByEmail(email);
        memberService.updatePassword(user, password);
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();

        writer.println("<script>alert('비밀번호가 변경 되었습니다.');</script>");
        writer.flush();
        writer.close();
    }

    @PostMapping("/reset_password")
    public void findPwAction(@RequestParam(name = "token") String token, @RequestParam(name = "password") String password, HttpServletResponse response) throws IOException {

        User user = userRepository.findByResetPasswordToken(token);
        memberService.updatePassword(user, password);

        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();

        writer.println("<script>alert('비밀번호가 설정되었습니다.');parent.location.href='/member/login';</script>");
        writer.flush();
        writer.close();

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
        companyRepository.save(resultCompany);
        memberService.save(user);

        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('수정이 완료되었습니다.');parent.location.reload();</script>");
        writer.flush();
    }

    @GetMapping("/payment")
    public String payment(@RequestParam(name = "page") Optional<Integer> page,
                          @DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model, Authentication authentication) throws ParseException {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10,Sort.by("id").descending());
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
        Page<Billing> list = billingRepository.findByBaseDateAndCompanyIdAndPriceGreaterThan(sdf.format(searchDate), user.getCompany().getId(),0,pageable);
        for(Billing billing : list.getContent()) {
            billing.setUsageHour((int) Math.ceil(billing.getUsageTime()));
        }
        long total = 0;
        for(Billing billing : list) {
            total += billing.getPrice();
        }
        model.addAttribute("total", total);
        model.addAttribute("list", list);

        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);


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
