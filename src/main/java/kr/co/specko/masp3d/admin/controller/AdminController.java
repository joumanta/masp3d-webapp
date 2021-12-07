package kr.co.specko.masp3d.admin.controller;

import kr.co.specko.masp3d.common.service.CloudService;
import kr.co.specko.masp3d.common.utils.FileUploadUtil;
import kr.co.specko.masp3d.customer.entity.Faq;
import kr.co.specko.masp3d.customer.entity.Inquiry;
import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.customer.repository.FaqRepository;
import kr.co.specko.masp3d.customer.repository.InquiryRepository;
import kr.co.specko.masp3d.customer.repository.NoticeRepository;
import kr.co.specko.masp3d.member.entity.*;
import kr.co.specko.masp3d.member.repository.*;
import kr.co.specko.masp3d.member.service.BillingService;
import kr.co.specko.masp3d.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final FileUploadUtil uploadUtil;
    private final NoticeRepository noticeRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final FaqRepository faqRepository;
    private final InquiryRepository inquiryRepository;
    private final CloudService cloudService;
    private final CompanyRepository companyRepository;
    private final ServiceRequestRepositoryCustom serviceRequestRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final BillingRepository billingRepository;
    private final BillingService billingService;

    @GetMapping(value = {"","/"})
    public String index() {
        return "redirect:/admin/servicelist";
    }

    @GetMapping("/login")
    public String login() {
        return "pages/admin/login";
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/servicelist")
    public String serviceList(@RequestParam(name = "page") Optional<Integer> page,Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        Page<User> list = memberService.findByAuthority("ROLE_ADMIN",pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        return "pages/admin/servicelist";
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/usage_history")
    public String usageHistory(
            @RequestParam(name = "page") Optional<Integer> page,
            @RequestParam(name="searchType", defaultValue = "all") String type,
            @RequestParam(name="search", required = false) String search
            ,Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        Page<ServiceRequest> list = serviceRequestRepository.findAllServiceRequest(type, search, pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        return "pages/admin/usage_history";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("service_permit")
    public @ResponseBody String servicePermit(Long companyId, String tenantId, String password, Long id ) throws Exception {
        User user = memberService.findById(id);
        user.getCompany().setTenantId(tenantId);
        user.setEnabled(true);
        memberService.save(user);

//        cloudService.saveServerList(tenantId, user.getCompany());

        return "OK";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/service_stop")
    public @ResponseBody String servicePermit(Long id ) {
        User user = memberService.findById(id);
        user.setEnabled(false);
        memberService.save(user);
        return "OK";
    }


    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/servicelist_member")
    public String serviceListMember(@RequestParam(name = "page") Optional<Integer> page, @RequestParam(name = "id") Long id, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        User user = memberService.findById(id);
        Page<User> list = memberService.findByCompany(user.getCompany(), pageable);
        model.addAttribute("user", user);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        model.addAttribute("id", id);
        return "pages/admin/servicelist_member";
    }



    @GetMapping("/faq_write")
    public String faqWrite() {
        return "pages/admin/faq_write";
    }

    @PostMapping("/faq_write")
    public String faqWriteAction(Faq faq, HttpServletRequest request) throws IOException {
        Map<String, String> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.size() > 0) {
            faq.setFileName(upload.get("orgFileName"));
            faq.setOrgFileName(upload.get("orgFileName"));
        }
        faqRepository.save(faq);
        return "redirect:/customer/faq";
    }

    @GetMapping("/notice_write")
    public String noticeWrite() {
        return "pages/admin/notice_write";
    }

    @GetMapping("/notice_modify")
    public String noticeModify(@RequestParam(name = "page") Integer page,
                               @RequestParam(name="searchType", defaultValue = "all") String type,
                               @RequestParam(name="search", required = false) String search,
                               @RequestParam(name = "id") Long id, Model model) {

        Notice notice = noticeRepository.findById(id).orElseThrow(NullPointerException::new);
        model.addAttribute("notice", notice);
        model.addAttribute("page", page);
        model.addAttribute("type", type);
        model.addAttribute("search", search);
        return "pages/admin/notice_modify";
    }

    @PostMapping("/notice_write")
    public String noticeWriteAction(Notice notice, HttpServletRequest request) throws IOException {
        Map<String, String> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.get("orgFileName") != null) {
            notice.setFileName(upload.get("saveFileName"));
            notice.setOrgFileName(upload.get("orgFileName"));
        }
        noticeRepository.save(notice);
        return "redirect:/customer/notice";
    }



    @PostMapping("/notice_modify")
    public String noticeModifyAction(Notice notice,@RequestParam(name = "page") Integer page,
    @RequestParam(name="searchType", defaultValue = "all") String type,
    @RequestParam(name="search", required = false) String search, HttpServletRequest request,RedirectAttributes redirectAttributes) throws IOException {
        Map<String, String> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.get("orgFileName") != null) {
            notice.setFileName(upload.get("saveFileName"));
            notice.setOrgFileName(upload.get("orgFileName"));
        }
        noticeRepository.save(notice);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("searchType", type);
        redirectAttributes.addAttribute("search", search);
        return "redirect:/customer/notice";
    }

    @PostMapping("/notice_delete")
    public String noticeDelete(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name="searchType", defaultValue = "all") String type,
            @RequestParam(name="search", required = false) String search,
            @RequestParam(name = "id") Long id, RedirectAttributes redirectAttributes) {
        noticeRepository.deleteById(id);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("searchType", type);
        redirectAttributes.addAttribute("search", search);
        return "redirect:/customer/notice";
    }

    @GetMapping("/inquiry_list")
    public String inquiryList(@RequestParam(name = "page") Optional<Integer> page,
                              @RequestParam(name="searchType", defaultValue = "all") String type,
                              @RequestParam(name="search", required = false) String search,
                             Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        Page<Inquiry> list = inquiryRepository.findAll(pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        return "pages/admin/inquiry_list";
    }

    @GetMapping("/inquiry_write")
    public String inquiryWrite(@RequestParam(name = "id") Long id,
                               Model model) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        model.addAttribute("inquiry", inquiry);
        return "pages/admin/inquiry_write";
    }

    @PostMapping("/inquiry_answer")
    public String inquiryAnswer(@RequestParam(name = "id") Long id, @RequestParam(name = "answer") String answer,
                               Model model) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        inquiry.setAnswer(answer);
        inquiry.setCompleted(true);
        inquiryRepository.save(inquiry);
        model.addAttribute("inquiry", inquiry);
        return "redirect:/admin/inquiry_write?id=" + id;
    }

    @GetMapping("/billing")
    public String billing(@DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model, Authentication authentication) throws ParseException {

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

        List<Company> list = companyRepository.findAll();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        List<CompanyBilling> groupByCompany = serverRepository.findGroupByCompany(sdf.format(searchDate));

        long totalSum = 0;
        for(CompanyBilling c : groupByCompany) {
            totalSum += c.getPrice();
        }
        model.addAttribute("total", totalSum);
        model.addAttribute("list", groupByCompany);

        List<Date> searchDates = new ArrayList<>();
        Date date = new Date();
        for(int i=0;i<10;i++) {
            searchDates.add(date);
            date = DateUtils.addMonths(date, -1);
        }
        model.addAttribute("searchDates", searchDates);

        return "pages/admin/billing";
    }

    @GetMapping("/billing_insert")
    public String billingInsert(@RequestParam(name = "page") Optional<Integer> page,
                                @DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
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

        List<Server> list = serverRepository.findAll();

        for(Server server : list) {
            List<User> role_admin = userRepository.findByCompanyAndAuthority(server.getCompany(), "ROLE_ADMIN");
            server.getCompany().setAdminUser(role_admin.get(0));
        }


        model.addAttribute("list", list);

        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);

        return "pages/admin/billing_insert";
    }

    @PostMapping("/billing_insert")
    public String billingInsertAction(@RequestParam("searchDate") String searchDate,
                                      @RequestParam("id[]") Long[] id,
                                      @RequestParam("time[]") Integer[] time) {

        billingService.deleteByBaseDate(searchDate);
        List<Billing> billings = new ArrayList<>();
        for(int i=0;i<time.length;i++) {
            if(time[i] != null) {
                Server server = serverRepository.findById(id[i]).get();
                Company company = companyRepository.findById(server.getCompany().getId()).get();
                Billing billing = new Billing();
                billing.setBaseDate(searchDate);
                billing.setStartDate(server.getStartDate());
                billing.setServerName(server.getName());
                billing.setIp(server.getIp());
                billing.setType(server.getType());
                billing.setStatus(server.getStatus());
                billing.setEndDate(server.getEndDate());
                billing.setCompany(company);
                if(server.getName().equals("Basic")) {
                    billing.setPrice(5000*time[i]);
                } else if(server.getName().equals("Special")) {
                    billing.setPrice(15000*time[i]);
                } else if(server.getName().equals("Professional")) {
                    billing.setPrice(8000*time[i]);
                } else {
                    billing.setPrice(30000*time[i]);
                }
                billing.setUsageTime(time[i]);
                billingRepository.save(billing);
            }
        }
        return "redirect:billing?searchDate=" + searchDate;
    }

}
