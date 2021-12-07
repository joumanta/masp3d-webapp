package kr.co.specko.masp3d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Masp3dWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(Masp3dWebappApplication.class, args);
	}

}
