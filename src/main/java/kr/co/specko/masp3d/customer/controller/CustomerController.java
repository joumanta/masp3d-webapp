package kr.co.specko.masp3d.customer.controller;

import kr.co.specko.masp3d.customer.entity.Faq;
import kr.co.specko.masp3d.customer.entity.Inquiry;
import kr.co.specko.masp3d.customer.entity.Notice;
import kr.co.specko.masp3d.customer.repository.FaqRepository;
import kr.co.specko.masp3d.customer.repository.InquiryRepository;
import kr.co.specko.masp3d.customer.repository.NoticeRepository;
import kr.co.specko.masp3d.customer.repository.NoticeRepositorySupport;
import kr.co.specko.masp3d.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.endsWith;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final FaqRepository faqRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeRepositorySupport noticeRepositorySupport;
    private final InquiryRepository inquiryRepository;

    @GetMapping("/faq")
    public String faq(@RequestParam(name = "page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10,Sort.by("id").descending());
        Page<Faq> list = faqRepository.findAll(pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        return "pages/customer/faq";
    }

    @GetMapping("/inquiry")
    public String inquiryForm() {
        return "pages/customer/inquiry";
    }

    @PostMapping("/inquiry_write")
    public void inquiryAction(Inquiry inquiry, HttpServletResponse response) throws IOException {
        inquiry.setManagerPhone(inquiry.getManagerPhone());
        inquiryRepository.save(inquiry);
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('등록이 완료되었습니다.');parent.location.reload();</script>");
        writer.flush();
    }

    @GetMapping("/notice")
    public String noticeList(@RequestParam(name = "page") Optional<Integer> page,
                             @RequestParam(name="searchType", defaultValue = "all") String type,
                             @RequestParam(name="search", required = false) String search,
                             Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get()-1 : 0, 10, Sort.by("id").descending());
        Page<Notice> list = noticeRepositorySupport.findAllNotice(type, search, pageable);
        model.addAttribute("list", list);
        model.addAttribute("page", pageable.getPageNumber()+1);
        model.addAttribute("maxPage", 5);
        model.addAttribute("type", type);
        model.addAttribute("search", search);
        return "pages/customer/notice";
    }

    @GetMapping("/notice_read")
    public String noticeRead(@RequestParam(name = "page") Integer page,
                             @RequestParam(name="searchType", defaultValue = "all") String type,
                             @RequestParam(name="search", required = false) String search,
            @RequestParam(name = "id") Long id, Model model) {

            Notice notice = noticeRepository.findById(id).orElseThrow(NullPointerException::new);
            model.addAttribute("notice", notice);
            model.addAttribute("page", page);
            model.addAttribute("type", type);
            model.addAttribute("search", search);
        return "pages/customer/notice_read";
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/download")
    public void downloadFile(@RequestParam("id") Long id,
                             HttpServletResponse response, @Value("${upload.dir}") String dir) throws Exception {

        Notice notice = noticeRepository.findById(id).orElseThrow(IllegalAccessError::new);
        byte[] fileByte = FileUtils.readFileToByteArray(new File(dir,notice.getFileName()));

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(notice.getOrgFileName(), "UTF-8")+"\";");
        response.setHeader("Content-Transfer-Encoding", "binary");

        response.getOutputStream().write(fileByte);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
}
