package kr.co.specko.masp3d.common.service;

import kr.co.specko.masp3d.member.entity.Company;
import kr.co.specko.masp3d.member.entity.Server;
import kr.co.specko.masp3d.member.repository.CompanyRepository;
import kr.co.specko.masp3d.member.repository.ServerRepository;
import kr.co.specko.masp3d.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
@Slf4j
public class NHNCloudRestService {

    @Value("${nhn.username}")
    private String username;

    private final ServerRepository serverRepository;

    private final UserRepository userRepository;

    private final CompanyRepository companyRepository;

    public boolean isActive(String tenantId, String serverId) {
        String token = token(tenantId, "!$VMtech1!");
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/servers/" + serverId).encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        JSONObject jsonObject = new JSONObject(exchange.getBody());
        return jsonObject.getJSONObject("server").getString("status").equals("ACTIVE");
    }

    private String token(String tenantId, String password) throws JSONException {

        log.info("?????? ????????????");
        URI uri = UriComponentsBuilder.fromUriString("https://api-identity.infrastructure.cloud.toast.com")
                .path("/v2.0/tokens").encode().build().toUri();

        JSONObject passwordCredentials = new JSONObject();
        passwordCredentials.put("username",username);
        passwordCredentials.put("password", password);

        JSONObject auth = new JSONObject();
        auth.put("tenantId", tenantId);
        auth.put("passwordCredentials", passwordCredentials);

        JSONObject requestObject = new JSONObject();
        requestObject.put("auth", auth);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(requestObject.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);


        JSONObject jsonObject = new JSONObject(result.getBody());

        String token = jsonObject
                .getJSONObject("access")
                .getJSONObject("token")
                .getString("id");
        String expire = jsonObject.getJSONObject("access").getJSONObject("token").getString("expires");

        log.info("?????? {}", token);
        return token;
    }

    public String getProjectId(String tenantId, String password) throws JSONException {

        log.info("?????? ????????????");
        URI uri = UriComponentsBuilder.fromUriString("https://api-identity.infrastructure.cloud.toast.com")
                .path("/v2.0/tokens").encode().build().toUri();

        JSONObject passwordCredentials = new JSONObject();
        passwordCredentials.put("username",username);
        passwordCredentials.put("password", password);

        JSONObject auth = new JSONObject();
        auth.put("tenantId", tenantId);
        auth.put("passwordCredentials", passwordCredentials);

        JSONObject requestObject = new JSONObject();
        requestObject.put("auth", auth);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(requestObject.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);


        JSONObject jsonObject = new JSONObject(result.getBody());

        String projectId = jsonObject
                .getJSONObject("access")
                .getJSONObject("token")
                .getJSONObject("tenant")
                .getString("name").split("_")[1];

        return projectId;
    }

    //?????? ???????????? ????????? ??????????????? ????????????.
    public List<Server> getServerList(String tenantId, Company company) throws Exception {

        String token = token(tenantId, "!$VMtech1!");
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/servers/detail").encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        JSONObject jsonObject = new JSONObject(exchange.getBody());

        List<Server> list = new ArrayList<>();
        JSONArray servers = jsonObject.getJSONArray("servers");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(int i=0;i<servers.length();i++) {
            Server server = new Server();
            JSONObject obj = servers.getJSONObject(i);
            String created = convertUTC(obj.getString("created"));
            Date startDate = sdf.parse(created);

            String ip = null;
            try {
                JSONArray jsonArray = obj.getJSONObject("addresses").getJSONArray("Default Network");
                for (int j = 0; j < jsonArray.length(); j++) {
                    if (jsonArray.getJSONObject(j).getString("OS-EXT-IPS:type").equals("floating")) {
                        ip = jsonArray.getJSONObject(j).getString("addr");
                    }
                }
            } catch (JSONException e) {
                ip = null;
            }

            String flavorId = obj.getJSONObject("flavor").getString("id");
            String serverName = obj.getString("name");
            String updatedTime = convertUTC(obj.getString("updated"));
            String status = obj.getString("status");
            String id = obj.getString("id");
            JSONObject flavor = getFlavor(tenantId, flavorId, token);
            int vcpu = flavor.getInt("vcpus");
            int ram = flavor.getInt("ram");
            String imageName = obj.getJSONObject("metadata").getString("image_name");
            String imageId = obj.getJSONObject("image").getString("id");

            String serviceType = "";

            switch (imageName) {
                case "CAE-Basic-img":
                    serviceType = "Basic";
                    break;
                case "CAE-Spe-img":
                    serviceType = "Special";
                    break;
                case "CAE-Pro-img":
                    serviceType = "Professional";
                    break;
                case "CAE-ez-img":
                    serviceType = "?????? ???????????? ?????? S/W";
                    break;
            }
            if(imageName.equals("CAE-Basic-img") || imageName.equals("CAE-Spe-img") || imageName.equals("CAE-Pro-img") || imageName.equals("CAE-ez-img")) {
                server.setName(serviceType);
                server.setImageId(imageId);
                server.setCompany(company);
                server.setType(vcpu + "vCPU * " + (ram / 1024) + "GB");
                server.setStatus(status);
                server.setIp(ip);
                if(status.equals("SHUTOFF")) {
                    server.setStartDate(sdf.parse(created));
                    server.setEndDate(sdf.parse(updatedTime));
                }
                if(status.equals("ACTIVE")) {
                    server.setStartDate(sdf.parse(updatedTime));
                }
                server.setServerId(id);
                list.add(server);

            }
        }
        return list;
    }

    private JSONObject getFlavor(String tenantId, String flavorId, String token) throws JSONException {

        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/flavors/detail").encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray flavors = jsonObject.getJSONArray("flavors");
        for(int i=0;i<flavors.length();i++) {
            JSONObject flavor = flavors.getJSONObject(i);

            if(flavor.getString("id").equals(flavorId)) {
                return flavor;
            }
        }
        return null;
    }

    private String getImageName(String imageId, String token) throws JSONException {
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-image.infrastructure.cloud.toast.com")
                .path("/v2/images/" + imageId ).encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());

        String name = jsonObject.getString("name");
        return name;

    }

    private String getIp(String tenantId, String token) {
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-network.infrastructure.cloud.toast.com")
                .path("/v2.0/floatingips").encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());

        JSONArray jsonArray = jsonObject.getJSONArray("floatingips");

        for(int i=0;i<jsonArray.length();i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            if(jsonObject1.getString("tenant_id").equals(tenantId)) {
                return jsonObject1.getString("floating_ip_address");
            }
        }
        return null;
    }



    private String convertUTC(String dtString){

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        transFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dt = null;
        try {
            dt = transFormat.parse(dtString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        dtString = dateFormatter.format(dt);
        return dtString;
    }


    public Server getServerInfo(String tenantId, String serverId) throws ParseException {
        String token = token(tenantId, "!$VMtech1!");
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/servers/" + serverId).encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = null;
        try {
            exchange = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        } catch (HttpClientErrorException e) {
            return null;
        }
        JSONObject serverInfo = new JSONObject(exchange.getBody());

        JSONObject server1 = serverInfo.getJSONObject("server");

        Server server = new Server();
        server.setServerId(server1.getString("id"));
        server.setStatus(server1.getString("status"));
        String createdDate = convertUTC(server1.getString("created"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updatedDate = convertUTC(server1.getString("updated"));

        if(server1.getString("status").equals("ACTIVE")) {
            server.setStartDate(sdf.parse(updatedDate));
            server.setEndDate(null);
        }

        if(server1.getString("status").equals("SHUTOFF")) {
            server.setStartDate(sdf.parse(createdDate));
            server.setEndDate(sdf.parse(updatedDate));
        }
        return server;
    }

    public void startInstance(String tenantId, String serverId) {
        String token = token(tenantId, "!$VMtech1!");

        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/servers/" + serverId + "/action").encode().build().toUri();

        JSONObject requestObject = new JSONObject();
        requestObject.put("os-start", JSONObject.NULL);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(requestObject.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
    }

    public void stopInstance(String tenantId, String serverId) {
        String token = token(tenantId, "!$VMtech1!");
        URI uri = UriComponentsBuilder.fromUriString("https://kr1-api-instance.infrastructure.cloud.toast.com")
                .path("/v2/" + tenantId + "/servers/" + serverId + "/action").encode().build().toUri();

        JSONObject requestObject = new JSONObject();
        requestObject.put("os-stop", JSONObject.NULL);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Auth-Token",token);

        HttpEntity<String> request = new HttpEntity<>(requestObject.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);


    }
}
