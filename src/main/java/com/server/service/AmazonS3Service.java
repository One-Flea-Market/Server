package com.server.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3Service {
    @Value("${cloud.aws.s3.bucket}") // application.properties에 설정한 S3 버킷 이름을 가져옵니다.
    private String bucketName;

    private final AmazonS3 amazonS3;

    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            String fileName = createFileName(file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                fileUrls.add(amazonS3.getUrl(bucketName, fileName).toString());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
            }
        }

        return fileUrls;
    }

    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            // S3에서 파일 삭제
            try {
                String key = extractKeyFromUrl(fileUrl); // 파일 URL에서 S3 키 추출
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
                log.info("File deleted successfully from S3: {}", fileUrl);
            } catch (Exception e) {
                log.error("Failed to delete file from S3: {}", fileUrl, e);
                // 실패한 경우 예외 처리 또는 로깅을 수행할 수 있습니다.
            }
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        // 주어진 URL에서 마지막 슬래시('/') 이후의 문자열을 추출하여 반환합니다.
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex + 1 < fileUrl.length()) {
            return fileUrl.substring(lastSlashIndex + 1);
        } else {
            // 유효한 파일 키를 추출할 수 없는 경우 예외 처리 또는 기본값을 반환할 수 있습니다.
            throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
        }
    }

    private String createFileName(String fileName) { // 먼저 파일 업로드 시, 파일명을 난수화하기 위해 random으로 돌립니다.
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) { // file 형식이 잘못된 경우를 확인하기 위해 만들어진 로직이며, 파일 타입과 상관없이 업로드할 수 있게 하기 위해 .의 존재 유무만 판단하였습니다.
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }
}