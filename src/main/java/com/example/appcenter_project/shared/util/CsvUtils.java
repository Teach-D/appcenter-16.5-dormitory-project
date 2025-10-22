package com.example.appcenter_project.shared.util;

import com.example.appcenter_project.domain.complaint.dto.response.ResponseComplaintCsvDto;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class CsvUtils {

    public byte[] generateComplaintCsv(List<ResponseComplaintCsvDto> complaints) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '"', "\r\n")) {

            // BOM 추가 (한글 Excel 호환성을 위해)
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            // CSV 헤더 작성 (민원 조회 필드와 민원번호)
            String[] header = {
                "민원번호", "날짜", "유형", "제목", "현황", "담당자", 
                "기숙사", "호실정보"
            };
            csvWriter.writeNext(header);

            // 데이터 행 작성 (민원 조회 필드와 민원번호)
            for (ResponseComplaintCsvDto complaint : complaints) {
                // 호실정보를 "동 호수 침대번호" 형태로 조합
                String roomInfo = "";
                if (complaint.getBuilding() != null) {
                    roomInfo += complaint.getBuilding();
                }
                if (complaint.getRoomNumber() != null) {
                    roomInfo += " " + complaint.getRoomNumber();
                }
                if (complaint.getBedNumber() != null) {
                    roomInfo += " " + complaint.getBedNumber();
                }
                
                String[] row = {
                    complaint.getId() != null ? String.valueOf(complaint.getId()) : "",
                    complaint.getDate() != null ? complaint.getDate() : "",
                    complaint.getType() != null ? complaint.getType() : "",
                    complaint.getTitle() != null ? complaint.getTitle() : "",
                    complaint.getStatus() != null ? complaint.getStatus() : "",
                    complaint.getOfficer() != null ? complaint.getOfficer() : "",
                    complaint.getDormType() != null ? complaint.getDormType() : "",
                    roomInfo.trim()
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("CSV 생성 중 오류 발생", e);
            throw new RuntimeException("CSV 파일 생성에 실패했습니다.", e);
        }
    }
}
