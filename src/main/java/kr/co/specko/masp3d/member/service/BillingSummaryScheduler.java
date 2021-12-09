package kr.co.specko.masp3d.member.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BillingSummaryScheduler {

//    @Scheduled(cron = "0 0 * * * *")
    @Scheduled(fixedDelay = 10*60*1000)
    public void run() {
        System.out.println("");
    }
}
