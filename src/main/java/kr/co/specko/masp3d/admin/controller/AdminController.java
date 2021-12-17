package kr.co.specko.masp3d.admin.controller;

import kr.co.specko.masp3d.UserService;
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
    private final UserServiceRepository userServiceRepository;

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
        Page<UserService> list = userServiceRepository.findAll(pageable);
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
        user.setBlocked(true);
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


    /*@PreAuthorize("hasRole('SUPER')")
    @GetMapping("/faq_write")
    public String faqWrite() {
        return "pages/admin/faq_write";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/faq_write")
    public String faqWriteAction(Faq faq, HttpServletRequest request) throws IOException {
        List<Map<String, String>> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.size() > 0) {
            faq.setFileName(upload.get(0).get("saveFileName"));
            faq.setOrgFileName(upload.get(0).get("orgFileName"));
        }
        faqRepository.save(faq);
        return "redirect:/customer/faq";
    }*/

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/notice_write")
    public String noticeWrite() {
        return "pages/admin/notice_write";
    }

    @PreAuthorize("hasRole('SUPER')")
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

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/notice_write")
    public String noticeWriteAction(Notice notice, HttpServletRequest request) throws IOException {
        List<Map<String, String>> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.size() > 0) {
            notice.setFileName(upload.get(0).get("saveFileName"));
            notice.setOrgFileName(upload.get(0).get("orgFileName"));
        }
        noticeRepository.save(notice);
        return "redirect:/customer/notice";
    }


    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/notice_modify")
    public String noticeModifyAction(Notice notice,@RequestParam(name = "page") Integer page,
    @RequestParam(name="searchType", defaultValue = "all") String type,
    @RequestParam(name="search", required = false) String search, HttpServletRequest request,RedirectAttributes redirectAttributes) throws IOException {
        List<Map<String, String>> upload = uploadUtil.upload(request);
        Notice findNotice = noticeRepository.findById(notice.getId()).orElseThrow(NullPointerException::new);
        if(upload.get(0).size() > 0) {
            findNotice.setFileName(upload.get(0).get("saveFileName"));
            findNotice.setOrgFileName(upload.get(0).get("orgFileName"));
        }
        findNotice.setTitle(notice.getTitle());
        findNotice.setContents(notice.getContents());
        noticeRepository.save(findNotice);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("searchType", type);
        redirectAttributes.addAttribute("search", search);
        return "redirect:/customer/notice";
    }

    @PreAuthorize("hasRole('SUPER')")
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

    @PreAuthorize("hasRole('SUPER')")
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

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/inquiry_write")
    public String inquiryWrite(@RequestParam(name = "id") Long id,
                               @RequestParam(name = "page") Integer page,
                               Model model) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("page", page);
        return "pages/admin/inquiry_write";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/inquiry_delete")
    public String inquiryDelete(@RequestParam(name = "id") Long id,
                               @RequestParam(name = "page") Integer page,
                               Model model,RedirectAttributes redirectAttributes) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        inquiryRepository.delete(inquiry);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/admin/inquiry_list";
    }

    @PreAuthorize("hasRole('SUPER')")
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

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/billing")
    public String billing(@RequestParam(name = "page") Optional<Integer> page,
                          @DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model, Authentication authentication) throws ParseException {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10,Sort.by("id").descending());
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
        Page<CompanyBilling> groupByCompany = serverRepository.findGroupByCompany(sdf.format(searchDate), pageable);

        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);

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

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/billing_detail")
    public String billingDetail(@RequestParam(name = "page") Optional<Integer> page,
                          @DateTimeFormat(pattern = "yyyyMM") Date searchDate, Model model, Authentication authentication) throws ParseException {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10,Sort.by("id").descending());
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
        Page<Billing> groupByCompany = billingRepository.findByBaseDateAndPriceGreaterThan(sdf.format(searchDate),0, pageable);
        for(Billing billing : groupByCompany.getContent()) {
            billing.setUsageHour((int) Math.ceil(billing.getUsageTime()));
        }

        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);

        model.addAttribute("list", groupByCompany);

        List<Date> searchDates = new ArrayList<>();
        Date date = new Date();
        for(int i=0;i<10;i++) {
            searchDates.add(date);
            date = DateUtils.addMonths(date, -1);
        }
        model.addAttribute("searchDates", searchDates);

        return "pages/admin/billing_detail";
    }

    @PreAuthorize("hasRole('SUPER')")
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

    /*@PreAuthorize("hasRole('SUPER')")
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
    }*/

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/faq_write")
    public String faqWrite() {
        return "pages/admin/faq_write";
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/faq_modify")
    public String faqModify(@RequestParam(name = "page") Integer page,
                               @RequestParam(name="searchType", defaultValue = "all") String type,
                               @RequestParam(name="search", required = false) String search,
                               @RequestParam(name = "id") Long id, Model model) {

        Faq faq = faqRepository.findById(id).orElseThrow(NullPointerException::new);
        model.addAttribute("faq", faq);
        model.addAttribute("page", page);
        model.addAttribute("searchType", type);
        model.addAttribute("search", search);
        return "pages/admin/faq_modify";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/faq_write")
    public String faqWriteAction(Faq faq, HttpServletRequest request) throws IOException {
        List<Map<String, String>> upload = uploadUtil.upload(request);
        String file = null;
        if(upload.size() > 0) {
            faq.setFileName(upload.get(0).get("saveFileName"));
            faq.setOrgFileName(upload.get(0).get("orgFileName"));
        }
        faqRepository.save(faq);
        return "redirect:/customer/faq";
    }


    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/faq_modify")
    public String faqModifyAction(Faq faq,@RequestParam(name = "page") Integer page,
                                     @RequestParam(name="searchType", defaultValue = "all") String type,
                                     @RequestParam(name="search", required = false) String search, HttpServletRequest request,RedirectAttributes redirectAttributes) throws IOException {
        List<Map<String, String>> upload = uploadUtil.upload(request);
        Faq findFaq = faqRepository.findById(faq.getId()).orElseThrow(NullPointerException::new);
        findFaq.setTitle(faq.getTitle());
        findFaq.setContents(faq.getContents());
        if(upload.get(0).size() > 0) {
            findFaq.setFileName(upload.get(0).get("saveFileName"));
            findFaq.setOrgFileName(upload.get(0).get("orgFileName"));
        }
        faqRepository.save(findFaq);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("searchType", type);
        redirectAttributes.addAttribute("search", search);
        return "redirect:/customer/faq";
    }

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/faq_delete")
    public String faqDelete(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name="searchType", defaultValue = "all") String type,
            @RequestParam(name="search", required = false) String search,
            @RequestParam(name = "id") Long id, RedirectAttributes redirectAttributes) {
        faqRepository.deleteById(id);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("searchType", type);
        redirectAttributes.addAttribute("search", search);
        return "redirect:/customer/faq";
    }

}
