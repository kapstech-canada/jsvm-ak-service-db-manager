package com.kaps.jsvm.ak.service_db_manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaps.jsvm.ak.service_db_manager.dto.ResponseObject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class JSONDatabaseService {
   private static final Logger log = LoggerFactory.getLogger(JSONDatabaseService.class);
   @Autowired
   private S3Client s3Client;
   @Value("${aws.bucket.name}")
   private String bucketName;
   @Value("${aws.bucket.folder.name}")
   private String folderName;
   private ResponseObject responseObject;
   private final ObjectMapper objectMapper = new ObjectMapper();

   public void uploadFile(MultipartFile file) throws IOException {
      this.s3Client.putObject((PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(file.getOriginalFilename()).build(), RequestBody.fromBytes(file.getBytes()));
   }

   public byte[] downloadFile(String key) {
      ResponseBytes<GetObjectResponse> objectAsBytes = this.s3Client.getObjectAsBytes((GetObjectRequest)GetObjectRequest.builder().bucket(this.bucketName).key(key).build());
      return objectAsBytes.asByteArray();
   }

   public boolean createFileIfNotExists(String fileName) throws JsonProcessingException {
      log.info("Creating JSON file " + fileName);
      if (this.bucketName != null && !this.bucketName.isBlank()) {
         String key = this.folderName + "/" + fileName;

         try {
            this.s3Client.headObject((HeadObjectRequest)HeadObjectRequest.builder().bucket(this.bucketName).key(key).build());
            System.out.println("File already exists in S3: " + key);
            return false;
         } catch (S3Exception var5) {
            byte[] emptyJson = this.objectMapper.writeValueAsBytes(new LinkedHashMap());
            this.s3Client.putObject((PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(key).build(), RequestBody.fromBytes(emptyJson));
            System.out.println("File created in S3: " + key);
            return true;
         }
      } else {
         throw new IllegalStateException("DB BUCKET is not configured");
      }
   }

   public Map<String, Object> readJson(String fileName) throws IOException {
      try {
         ResponseInputStream<GetObjectResponse> s3Object = this.s3Client.getObject((GetObjectRequest)GetObjectRequest.builder().bucket(this.bucketName).key(fileName).build());
         return (Map)this.objectMapper.readValue(s3Object, new TypeReference<Map<String, Object>>() {
         });
      } catch (NoSuchKeyException var3) {
         return new LinkedHashMap();
      }
   }

   public void saveJson(String fileName, Map<String, Object> data) throws IOException {
      byte[] json = this.objectMapper.writeValueAsBytes(data);
      this.s3Client.putObject((PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(fileName).build(), RequestBody.fromBytes(json));
   }

   public void createRecord(String fileName, String id, Object record) throws IOException {
      Map<String, Object> data = this.readJson(fileName);
      if (data.containsKey(id)) {
         throw new IllegalArgumentException("ID already exists: " + id);
      } else {
         data.put(id, record);
         this.saveJson(fileName, data);
      }
   }

   public void updateRecord(String fileName, String id, Object record) throws IOException {
      Map<String, Object> data = this.readJson(fileName);
      if (!data.containsKey(id)) {
         throw new IllegalArgumentException("ID not found: " + id);
      } else {
         data.put(id, record);
         this.saveJson(fileName, data);
      }
   }

   public void deleteRecord(String fileName, String id) throws IOException {
      Map<String, Object> data = this.readJson(fileName);
      if (!data.containsKey(id)) {
         throw new IllegalArgumentException("ID not found: " + id);
      } else {
         data.remove(id);
         this.saveJson(fileName, data);
      }
   }
}
