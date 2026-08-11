package com.yasarbilgi.ats.attachment.service;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.contract.ApplicationContract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class CandidateCvStorageService {
    private final Path root;

    public CandidateCvStorageService(@Value("${ats.storage.cv-path:./data/uploads/cv}") String rootPath) {
        this.root = Path.of(rootPath).toAbsolutePath().normalize();
    }

    public StoredCv store(MultipartFile file) {
        validate(file);
        String key = UUID.randomUUID() + ".pdf";
        Path target = resolve(key);
        try {
            Files.createDirectories(root);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredCv(safeFileName(file.getOriginalFilename()), "application/pdf", file.getSize(), key);
        } catch (IOException exception) {
            throw new BusinessRuleException("CV dosyası kaydedilemedi.");
        }
    }

    public Resource load(String key) {
        try {
            Resource resource = new UrlResource(resolve(key).toUri());
            if (!resource.exists() || !resource.isReadable()) throw new BusinessRuleException("CV dosyası depolamada bulunamadı.");
            return resource;
        } catch (IOException exception) {
            throw new BusinessRuleException("CV dosyası okunamadı.");
        }
    }

    public void delete(String key) {
        try { Files.deleteIfExists(resolve(key)); }
        catch (IOException exception) { throw new BusinessRuleException("CV dosyası silinemedi."); }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessRuleException("Yüklenecek PDF dosyasını seçin.");
        if (file.getSize() > ApplicationContract.CV_MAX_FILE_SIZE_BYTES) throw new BusinessRuleException("CV dosyası izin verilen boyutu aşıyor.");
        String name = safeFileName(file.getOriginalFilename());
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) throw new BusinessRuleException("CV yalnızca PDF formatında yüklenebilir.");
        try (InputStream input = file.getInputStream()) {
            byte[] s = input.readNBytes(5);
            if (s.length != 5 || s[0] != '%' || s[1] != 'P' || s[2] != 'D' || s[3] != 'F' || s[4] != '-')
                throw new BusinessRuleException("Seçilen dosya geçerli bir PDF değil.");
        } catch (IOException exception) { throw new BusinessRuleException("CV dosyası doğrulanamadı."); }
    }

    private Path resolve(String key) {
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) throw new BusinessRuleException("Geçersiz dosya yolu.");
        return resolved;
    }

    private String safeFileName(String value) {
        String name = value == null ? "cv.pdf" : Path.of(value).getFileName().toString().trim();
        return name.isBlank() ? "cv.pdf" : name;
    }

    public record StoredCv(String fileName, String contentType, long fileSize, String storageKey) {}
}
