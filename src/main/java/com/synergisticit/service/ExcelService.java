package com.synergisticit.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.synergisticit.dto.ColumnProfile;

@Service
public class ExcelService {

    public List<String> getSheetNames(MultipartFile file) throws IOException {

        List<String> sheetNames = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        }

        return sheetNames;
    }

    public List<String> getHeaders(MultipartFile file, String sheetName) throws IOException {

        List<String> headers = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)) {
        	
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return headers;
            }

            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
        }

        return headers;
    }
    
    public List<ColumnProfile> profileSheet(MultipartFile file, String sheetName) throws IOException {

        List<ColumnProfile> profiles = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return profiles;
            }

            for (Cell headerCell : headerRow) {

                String columnName = headerCell.getStringCellValue().trim();

                List<Cell> sampleCells = new ArrayList<>();
                List<String> sampleValues = new ArrayList<>();

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum() && sampleCells.size() < 5; rowIndex++) {

                    Row row = sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    Cell cell = row.getCell(headerCell.getColumnIndex());

                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        continue;
                    }

                    String value = getCellValueAsString(cell);

                    if (!value.isBlank()) {
                    	
                        sampleCells.add(cell);
                        sampleValues.add(value);
                    }
                }

                String dataType = inferDataType(sampleCells);

                profiles.add(new ColumnProfile(columnName, dataType, sampleValues));
            }
        }

        return profiles;
    }
    
    private String getCellValueAsString(Cell cell) {

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }
    
    private String inferDataType(List<Cell> cells) {

        if (cells.isEmpty()) {
            return "UNKNOWN";
        }

        boolean allNumeric = true;
        boolean allInteger = true;
        boolean allDate = true;
        boolean allBoolean = true;

        for (Cell cell : cells) {

            if (cell == null || cell.getCellType() == CellType.BLANK) {
                continue;
            }

            switch (cell.getCellType()) {

                case NUMERIC:

                    allBoolean = false;

                    if (DateUtil.isCellDateFormatted(cell)) {
                        allNumeric = false;
                        allInteger = false;
                    } else {
                        allDate = false;

                        double value = cell.getNumericCellValue();

                        if (value % 1 != 0) {
                            allInteger = false;
                        }
                    }

                    break;

                case BOOLEAN:

                    allNumeric = false;
                    allInteger = false;
                    allDate = false;
                    break;

                case STRING:

                    allNumeric = false;
                    allInteger = false;
                    allDate = false;

                    if (!isBooleanText(cell.getStringCellValue())) {
                        allBoolean = false;
                    }

                    break;

                default:

                    allNumeric = false;
                    allInteger = false;
                    allDate = false;
                    allBoolean = false;
            }
        }

        if (allDate) {
            return "DATE";
        }

        if (allBoolean) {
            return "BOOLEAN";
        }

        if (allNumeric && allInteger) {
            return "INTEGER";
        }

        if (allNumeric) {
            return "DECIMAL";
        }

        return "STRING";
    }
    
    private boolean isBooleanText(String value) {

        if (value == null) {
            return false;
        }

        return value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false");
    }
    
    public List<Map<String, String>> readRows(
            MultipartFile file,
            String sheetName) throws IOException {

        List<Map<String, String>> rows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet not found: " + sheetName
                );
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return rows;
            }

            List<String> headers = new ArrayList<>();

            for (Cell cell : headerRow) {
                headers.add(
                        getCellValueAsString(cell)
                );
            }

            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                Map<String, String> rowData =
                        new LinkedHashMap<>();

                boolean hasValue = false;

                for (int columnIndex = 0;
                     columnIndex < headers.size();
                     columnIndex++) {

                    Cell cell =
                            row.getCell(
                                    columnIndex,
                                    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                            );

                    String value = "";

                    if (cell != null) {
                        value =
                                getCellValueAsString(cell);
                    }

                    if (!value.isBlank()) {
                        hasValue = true;
                    }

                    rowData.put(
                            headers.get(columnIndex),
                            value
                    );
                }

                if (hasValue) {
                    rows.add(rowData);
                }
            }
        }

        return rows;
    }
}
