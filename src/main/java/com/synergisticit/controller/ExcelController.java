package com.synergisticit.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergisticit.dto.ColumnProfile;
import com.synergisticit.service.ExcelService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> uploadExcel(@RequestParam MultipartFile file) throws IOException {

        return excelService.getSheetNames(file);
    }
    
    @PostMapping(value = "/headers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> getHeaders(@RequestParam MultipartFile file, @RequestParam("sheet") String sheetName) throws IOException {

        return excelService.getHeaders(file, sheetName);
    }
    
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ColumnProfile> profileSheet(@RequestParam MultipartFile file, @RequestParam("sheet") String sheetName) throws IOException {

        return excelService.profileSheet(file, sheetName);
    }
}