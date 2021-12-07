package kr.co.specko.masp3d.common.controller;

import kr.co.specko.masp3d.common.service.NHNCloudRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final NHNCloudRestService nhnCloudRestService;

    @GetMapping("/terms")
    public String terms() {
        return "pages/common/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "pages/common/privacy";
    }
}
