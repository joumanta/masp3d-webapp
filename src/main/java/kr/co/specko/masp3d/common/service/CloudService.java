package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloudService {


    private final NHNCloudRestService restService;

    @Async
    public void saveServerList(String tenantId, Company company) throws Exception {
        restService.getServerList(tenantId, company);
    }
}
