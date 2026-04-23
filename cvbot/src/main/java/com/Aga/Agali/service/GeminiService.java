package com.Aga.Agali.service;

import com.Aga.Agali.entity.CvData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public String polishText(String text) {
        if (text == null || text.trim().isEmpty()) return text;

        String prompt = """
                Sən Azərbaycan dili ekspertisən. Aşağıdakı qaydaları ciddi şəkildə icra et:
                
                1. Bütün sözləri düzgün Azərbaycan əlifbası ilə yaz (ə, ş, ğ, ı, ö, ü, ç, İ)
                2. Böyük/kiçik hərf xətalarını düzəlt
                3. Durğu işarələrini düzəlt
                4. Cümlə quruluşunu peşəkar CV dilinə uyğunlaşdır
                5. Mənası dəyişməsin, məlumat əlavə edilməsin, silinməsin
                6. Mətndəki **bold** və __italic__ işarələrini olduğu kimi saxla
                7. Yalnız düzəldilmiş mətni qaytar — heç bir izahat yazma
                
                Mətn:
                %s
                """.formatted(text);

        return callGemini(prompt, text);
    }

    public String polishAbout(String about, String profession) {
        if (about == null || about.trim().isEmpty()) return about;

        String prompt = """
                Sən peşəkar CV redaktorusan. Aşağıdakı "Haqqımda" mətnini peşəkar CV dilinə çevir.
                
                Qaydalar:
                1. Mətni 4-6 cümlə həcminə gətir
                2. "%s" peşəsinə aid peşəkar terminlər və bacarıqlar əlavə et
                3. Azərbaycan orfoqrafiyasını düzəlt
                4. Güclü, özünəinamlı CV dili istifadə et
                5. Həddindən artıq şişirtmə — real və inandırıcı olsun
                6. Yalnız hazır mətni qaytar — heç bir izahat yazma
                
                Mətn:
                %s
                """.formatted(profession, about);

        return callGemini(prompt, about);
    }

    public String polishDuties(String duties, String position) {
        if (duties == null || duties.trim().isEmpty()) return duties;

        String prompt = """
                Sən peşəkar CV redaktorusan. Aşağıdakı iş öhdəliklərini peşəkar CV dilinə çevir.
                
                Qaydalar:
                1. Hər öhdəliyi bullet point ilə yaz (• işarəsi ilə)
                2. "%s" vəzifəsinə aid peşəkar terminlər əlavə et
                3. Hər cümləni güclü feil ilə başlat (Həyata keçirdim, Təmin etdim, İdarə etdim)
                4. 4-6 bullet point olsun
                5. Azərbaycan orfoqrafiyasını düzəlt
                6. Yalnız hazır mətni qaytar — heç bir izahat yazma
                
                Mətn:
                %s
                """.formatted(position, duties);

        return callGemini(prompt, duties);
    }

    private String callGemini(String prompt, String fallback) {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map body = response.getBody();
            List candidates = (List) body.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            log.error("Gemini xətası: {}", e.getMessage());
            return fallback;
        }
    }
}