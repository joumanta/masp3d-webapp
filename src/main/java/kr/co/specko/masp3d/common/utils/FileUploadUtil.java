package kr.co.specko.masp3d.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FileUploadUtil {

    @Value("${upload.dir}")
    private String uploadDir;

    public List<Map<String,String>> upload(HttpServletRequest request) throws IOException {

        List<Map<String,String>> map = new ArrayList<>();


        File dir = new File(uploadDir);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> files = multipartHttpServletRequest.getFiles("file");
        for(MultipartFile f : files) {
            Map<String,String> result = new HashMap<>();
            if (!"".equals(f.getOriginalFilename())) {
                String saveFileName = UUID.randomUUID().toString();
                String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".") + 1);
                saveFileName += "."+ext;
                result.put("orgFileName", f.getOriginalFilename());
                result.put("saveFileName", saveFileName);
                File saveFile = new File(uploadDir, saveFileName);
                f.transferTo(saveFile);
            }
            map.add(result);
        }
        return map;
    }
}
