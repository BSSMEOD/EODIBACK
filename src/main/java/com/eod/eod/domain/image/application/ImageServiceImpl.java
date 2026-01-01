package com.eod.eod.domain.image.application;

import com.eod.eod.domain.image.exception.ImageErrorCode;
import com.eod.eod.domain.image.exception.ImageException;
import com.eod.eod.domain.image.infrastructure.ImageRepository;
import com.eod.eod.domain.image.model.Image;
import com.eod.eod.domain.user.application.UserFacade;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final ImageRepository imageRepository;
    private final UserFacade userFacade;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public String uploadImage(MultipartFile file, Long userId) {
        User user = userFacade.getUserById(userId);

        String extension = validateFile(file);
        StoredImage storedImage = saveFile(file, extension);

        Image image = Image.create(
            storedImage.publicPath(),
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType(),
            user
        );
        try {
            imageRepository.save(image);
        } catch (RuntimeException e) {
            deleteFileIfExists(storedImage.filePath());
            throw e;
        }

        return buildImageUrl(storedImage.publicPath());
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageException(ImageErrorCode.EMPTY_FILE);
        }

        validateMimeType(file.getContentType());
        validateFileSize(file.getSize());
        String extension = resolveExtension(file.getOriginalFilename());
        validateImageContent(file);
        return extension;
    }

    private void validateMimeType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void validateFileSize(long size) {
        if (size > MAX_FILE_SIZE) {
            throw new ImageException(ImageErrorCode.FILE_TOO_LARGE);
        }
    }

    private String resolveExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }

        String extension = filename.substring(filename.lastIndexOf("."))
            .toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        return extension;
    }

    private void validateImageContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE, e);
        }
    }

    private StoredImage saveFile(MultipartFile file, String extension) {
        try {
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth());

            String relativeDirectory = "images/" + datePath;
            Path directoryPath = resolveDirectory(relativeDirectory);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path filePath = directoryPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredImage(buildPublicPath(relativeDirectory, uniqueFilename), filePath);

        } catch (IOException e) {
            log.error("Failed to save uploaded image", e);
            throw new ImageException(ImageErrorCode.FILE_SAVE_FAILED, e);
        }
    }

    private Path resolveDirectory(String relativeDirectory) {
        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(relativeDirectory).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new ImageException(ImageErrorCode.FILE_SAVE_FAILED);
        }
        return targetPath;
    }

    private String buildPublicPath(String relativeDirectory, String filename) {
        return "/" + relativeDirectory + "/" + filename;
    }

    private String buildImageUrl(String publicPath) {
        return UriComponentsBuilder.fromUriString(baseUrl)
            .path(publicPath)
            .build()
            .toUriString();
    }

    private void deleteFileIfExists(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete uploaded image after DB failure: {}", filePath, e);
        }
    }

    private static final class StoredImage {
        private final String publicPath;
        private final Path filePath;

        private StoredImage(String publicPath, Path filePath) {
            this.publicPath = publicPath;
            this.filePath = filePath;
        }

        private String publicPath() {
            return publicPath;
        }

        private Path filePath() {
            return filePath;
        }
    }
}
