package br.gov.mma.facial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@RestController
public class FaviconController {
    private static final Logger logger = LoggerFactory.getLogger(FaviconController.class);

    @GetMapping("/favicon.ico")
    public ResponseEntity<byte[]> favicon() {
        try {
            ClassPathResource res = new ClassPathResource("static/favicon.ico");
            if (res.exists()) {
                try (InputStream in = res.getInputStream()) {
                    byte[] bytes = in.readAllBytes();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                            .contentType(MediaType.parseMediaType("image/x-icon"))
                            .body(bytes);
                }
            }
        } catch (IOException e) {
            logger.debug("favicon resource read error: {}", e.getMessage());
        }

        // Fallback: 1x1 transparent PNG (base64)
        String b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";
        byte[] png = Base64.getDecoder().decode(b64);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
