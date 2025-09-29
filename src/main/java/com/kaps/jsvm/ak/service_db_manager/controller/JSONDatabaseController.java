package com.kaps.jsvm.ak.service_db_manager.controller;

import com.kaps.jsvm.ak.service_db_manager.service.JSONDatabaseService;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JSONDatabaseController {
   private static final Logger log = LoggerFactory.getLogger(JSONDatabaseController.class);
   @Autowired
   private JSONDatabaseService jSONDatabaseService;

   @PostMapping({"/create-json-db"})
   public ResponseEntity<String> createFile(@RequestBody String fileName) {
      try {
         fileName = fileName.replace("\"", "").trim();
         boolean created = this.jSONDatabaseService.createFileIfNotExists(fileName);
         return created ? ResponseEntity.status(201).body("File created: " + fileName) : ResponseEntity.status(409).body("File already exists: " + fileName);
      } catch (Exception var3) {
         log.error("Error creating file: {}", fileName, var3);
         return ResponseEntity.status(500).body("Error creating file: " + var3.getMessage());
      }
   }

   @PostMapping({"/createRecord/{fileName}/{id}"})
   public ResponseEntity<String> createRecord(@PathVariable String fileName, @PathVariable String id, @RequestBody Map<String, Object> record) {
      try {
         this.jSONDatabaseService.createRecord(fileName, id, record);
         return ResponseEntity.status(201).body("Record created in " + fileName);
      } catch (IllegalArgumentException var5) {
         return ResponseEntity.badRequest().body("Invalid request: " + var5.getMessage());
      } catch (IOException var6) {
         return ResponseEntity.status(404).body("File not found: " + fileName);
      } catch (Exception var7) {
         log.error("Error creating record in {}", fileName, var7);
         return ResponseEntity.status(500).body("Error creating record: " + var7.getMessage());
      }
   }

   @GetMapping({"/readFile/{fileName}"})
   public ResponseEntity<?> readFile(@PathVariable String fileName) {
      try {
         Map<String, Object> data = this.jSONDatabaseService.readJson(fileName);
         return data != null && !data.isEmpty() ? ResponseEntity.ok(data) : ResponseEntity.status(404).body("No records found in " + fileName);
      } catch (IOException var3) {
         return ResponseEntity.status(404).body("File not found: " + fileName);
      } catch (Exception var4) {
         log.error("Error reading file: {}", fileName, var4);
         return ResponseEntity.status(500).body("Error reading file: " + var4.getMessage());
      }
   }

   @PutMapping({"/updateRecord/{fileName}/{id}"})
   public ResponseEntity<String> updateRecord(@PathVariable String fileName, @PathVariable String id, @RequestBody Map<String, Object> record) {
      try {
         this.jSONDatabaseService.updateRecord(fileName, id, record);
         return ResponseEntity.ok("Record updated in " + fileName);
      } catch (IllegalArgumentException var5) {
         return ResponseEntity.status(404).body("Record with id " + id + " not found in " + fileName);
      } catch (IOException var6) {
         return ResponseEntity.status(404).body("File not found: " + fileName);
      } catch (Exception var7) {
         log.error("Error updating record in {}", fileName, var7);
         return ResponseEntity.status(500).body("Error updating record: " + var7.getMessage());
      }
   }

   @DeleteMapping({"/deleteRecord/{fileName}/{id}"})
   public ResponseEntity<String> deleteRecord(@PathVariable String fileName, @PathVariable String id) {
      try {
         this.jSONDatabaseService.deleteRecord(fileName, id);
         return ResponseEntity.ok("Record deleted from " + fileName);
      } catch (IllegalArgumentException var4) {
         return ResponseEntity.status(404).body("Record with id " + id + " not found in " + fileName);
      } catch (IOException var5) {
         return ResponseEntity.status(404).body("File not found: " + fileName);
      } catch (Exception var6) {
         log.error("Error deleting record from {}", fileName, var6);
         return ResponseEntity.status(500).body("Error deleting record: " + var6.getMessage());
      }
   }
}
