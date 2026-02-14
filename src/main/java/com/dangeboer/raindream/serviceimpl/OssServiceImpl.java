package com.dangeboer.raindream.serviceimpl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.dangeboer.raindream.exception.BadRequestException;
import com.dangeboer.raindream.model.form.OssPresignForm;
import com.dangeboer.raindream.model.vo.OssPresignVO;
import com.dangeboer.raindream.service.OssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URI;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024L;

    @Value("${raindream.oss.endpoint}")
    private String endpoint;

    @Value("${raindream.oss.bucket}")
    private String bucket;

    @Value("${raindream.oss.access-key-id}")
    private String accessKeyId;

    @Value("${raindream.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${raindream.oss.expires-seconds:300}")
    private Long expiresSeconds;

    @Value("${raindream.oss.read-expires-seconds:600}")
    private Long readExpiresSeconds;

    @Value("${raindream.oss.public-domain:}")
    private String publicDomain;

    @Override
    public OssPresignVO presignPutUrl(Long userId, OssPresignForm form) {
        if (userId == null || form == null) {
            throw new BadRequestException();
        }
        validateConfig();
        validateForm(form);

        String objectKey = buildObjectKey(userId, form.getFileName());
        String normalizedEndpoint = stripScheme(endpoint);
        String endpointWithScheme = endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : "https://" + endpoint;

        Date expiration = new Date(System.currentTimeMillis() + expiresSeconds * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.PUT);
        request.setContentType(form.getContentType());
        request.setExpiration(expiration);

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpointWithScheme, accessKeyId, accessKeySecret);
            URL presigned = ossClient.generatePresignedUrl(request);
            String fileUrl = buildFileUrl(normalizedEndpoint, objectKey);
            return new OssPresignVO(presigned.toString(), objectKey, fileUrl);
        } catch (Exception e) {
            throw new BadRequestException("生成 OSS 上传签名失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String presignReadUrl(Long userId, String storeUrl) {
        if (userId == null || isBlank(storeUrl)) {
            throw new BadRequestException("storeUrl 不能为空");
        }
        validateConfig();

        String objectKey = parseObjectKey(storeUrl);
        String expectedPrefix = "user/" + userId + "/";
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new BadRequestException("无权访问该文件");
        }

        String endpointWithScheme = endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : "https://" + endpoint;
        long ttl = (readExpiresSeconds == null || readExpiresSeconds < 60) ? 600L : readExpiresSeconds;
        Date expiration = new Date(System.currentTimeMillis() + ttl * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.GET);
        request.setExpiration(expiration);

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpointWithScheme, accessKeyId, accessKeySecret);
            URL presigned = ossClient.generatePresignedUrl(request);
            return presigned.toString();
        } catch (Exception e) {
            throw new BadRequestException("生成 OSS 访问签名失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void validateConfig() {
        if (isBlank(endpoint) || isBlank(bucket) || isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            throw new BadRequestException("OSS 配置不完整，请先配置 endpoint/bucket/access key");
        }
        if (expiresSeconds == null || expiresSeconds < 60) {
            expiresSeconds = 300L;
        }
    }

    private void validateForm(OssPresignForm form) {
        if (isBlank(form.getFileName()) || isBlank(form.getContentType()) || form.getSize() == null) {
            throw new BadRequestException("文件参数不完整");
        }
        if (form.getSize() <= 0) {
            throw new BadRequestException("文件大小不合法");
        }
        if (form.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("文件过大，当前限制 30MB");
        }
        String contentType = form.getContentType().toLowerCase(Locale.ROOT);
        if (!(contentType.startsWith("image/")
                || contentType.startsWith("video/")
                || contentType.startsWith("text/")
                || "application/octet-stream".equals(contentType))) {
            throw new BadRequestException("不支持的文件类型");
        }
    }

    private String buildObjectKey(Long userId, String fileName) {
        LocalDate today = LocalDate.now();
        String ext = extractExtension(fileName);
        String random = UUID.randomUUID().toString().replace("-", "");
        return "user/" + userId + "/"
                + today.getYear() + "/"
                + String.format("%02d", today.getMonthValue()) + "/"
                + random + ext;
    }

    private String extractExtension(String fileName) {
        if (isBlank(fileName)) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        String ext = fileName.substring(idx).toLowerCase(Locale.ROOT);
        if (ext.length() > 12 || !ext.matches("\\.[a-z0-9]+")) {
            return "";
        }
        return ext;
    }

    private String buildFileUrl(String normalizedEndpoint, String objectKey) {
        if (!isBlank(publicDomain)) {
            String domain = publicDomain.startsWith("http://") || publicDomain.startsWith("https://")
                    ? publicDomain
                    : "https://" + publicDomain;
            return domain + "/" + objectKey;
        }
        return "https://" + bucket + "." + normalizedEndpoint + "/" + objectKey;
    }

    private String parseObjectKey(String storeUrl) {
        String normalized = storeUrl.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        try {
            URI uri = URI.create(normalized);
            String host = uri.getHost();
            String path = uri.getPath();
            if (isBlank(host) || isBlank(path) || "/".equals(path)) {
                throw new BadRequestException("storeUrl 不合法");
            }
            if (!isAllowedHost(host)) {
                throw new BadRequestException("storeUrl 非法域名");
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("storeUrl 不合法");
        }
    }

    private boolean isAllowedHost(String host) {
        String lowerHost = host.toLowerCase(Locale.ROOT);
        String endpointHost = stripScheme(endpoint).toLowerCase(Locale.ROOT);
        String bucketHost = (bucket + "." + endpointHost).toLowerCase(Locale.ROOT);
        if (bucketHost.equals(lowerHost)) {
            return true;
        }
        if (!isBlank(publicDomain)) {
            String publicHost = stripScheme(publicDomain).toLowerCase(Locale.ROOT);
            return publicHost.equals(lowerHost);
        }
        return false;
    }

    private String stripScheme(String value) {
        if (value == null) return "";
        return value.replaceFirst("^https?://", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
