package uz.db.service;


import lombok.Getter;
import lombok.SneakyThrows;
import uz.core.logger.LogManager;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.SertificatAttestatsiyaEntity;
import uz.db.entity.SertificatTestCheckerMilliyDto;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CertificateService {
    private final LogManager _logger = new LogManager(CertificateService.class);
    @Getter
    private static final CertificateService instance = new CertificateService();

    @SneakyThrows
    public String getAttestatsiyaCertificate(SertificatAttestatsiyaEntity entity) {
        String encodedFio = URLEncoder.encode(entity.getFio(), StandardCharsets.UTF_8);

        String url = PropertiesUtils.getApiBaseUrl() + "/attestatsiya?fio=%s&sort=%s&overallScore=%s&for70Score=%s"
                .formatted(encodedFio, entity.getSort(), entity.getOverallScore(), entity.getFor70Score());

        return sendPostRequest(url);
    }

    @SneakyThrows
    public String getMilliyCertificate(SertificatTestCheckerMilliyDto entity) {
        String encodedFio = URLEncoder.encode(entity.getFio(), StandardCharsets.UTF_8);
        String url = PropertiesUtils.getApiBaseUrl() + "/milliy?part_1=%s&part_2=%s&part_3=%s&part_4=%s&overallScore=%s&fio=%s"
                .formatted(entity.getPart_1(), entity.getPart_2(), entity.getPart_3(), entity.getPart_4(), entity.getOverallScore(), encodedFio);

        return sendPostRequest(url);
    }

    private String sendPostRequest(String urlString) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            _logger.error("Failed to fetch certificate: HTTP code " + response.statusCode());
            return "";
        }
    }

    private String parseCertificate(String response) {
        int startIndex = response.indexOf("\"sertificat\":\"") + 14;
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }
}
