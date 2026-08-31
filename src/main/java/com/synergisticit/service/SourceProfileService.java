package com.synergisticit.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.synergisticit.dto.ColumnProfile;
import com.synergisticit.entity.SourceColumn;
import com.synergisticit.entity.SourceProfile;
import com.synergisticit.repository.SourceProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceProfileService {

    private final ExcelService excelService;
    private final SourceProfileRepository sourceProfileRepository;

    public SourceProfile createProfile(MultipartFile file, String sheetName) throws IOException {

        List<ColumnProfile> columnProfiles = excelService.profileSheet(file, sheetName);
        SourceProfile sourceProfile = new SourceProfile();

        sourceProfile.setFileName(file.getOriginalFilename());
        List<SourceColumn> sourceColumns = new ArrayList<>();

        for (ColumnProfile profile : columnProfiles) {

            SourceColumn sourceColumn = new SourceColumn();

            sourceColumn.setSheetName(sheetName);
            sourceColumn.setColumnName(profile.getColumnName());
            sourceColumn.setDataType(profile.getDataType());
            sourceColumn.setSampleValues(String.join(", ", profile.getSampleValues()));

            sourceColumn.setSourceProfile(sourceProfile);
            sourceColumns.add(sourceColumn);
        }

        sourceProfile.setColumns(sourceColumns);

        return sourceProfileRepository.save(sourceProfile);
    }
}
