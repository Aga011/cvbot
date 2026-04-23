package com.Aga.Agali.service;

import com.Aga.Agali.entity.CvData;
import com.Aga.Agali.repository.CvDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvService {

    private final GeminiService geminiService;
    private final CvDataRepository cvDataRepository;

    public void polishWithGemini(CvData cvData) {
        try {
            log.info("CV məlumatları polish edilir...");

            cvData.setFullName(geminiService.polishText(cvData.getFullName()));
            cvData.setProfession(geminiService.polishText(cvData.getProfession()));
            cvData.setAddress(geminiService.polishText(cvData.getAddress()));

            if (cvData.getAbout() != null) {
                cvData.setAbout(geminiService.polishAbout(cvData.getAbout(), cvData.getProfession()));
            }

            if (cvData.getExperience() != null) {
                String[] expBlocks = cvData.getExperience().split("\n\n");
                StringBuilder polishedExp = new StringBuilder();
                for (String block : expBlocks) {
                    if (block.trim().isEmpty()) continue;
                    String[] lines = block.split("\n", 3);
                    if (lines.length >= 3) {
                        String header = lines[0];
                        String dates = lines[1];

                        String position = "";
                        String cleanHeader = header.replaceAll("\\*\\*", "");
                        if (cleanHeader.contains("—")) {
                            position = cleanHeader.split("—")[1].trim();
                        }
                        String duties = geminiService.polishDuties(lines[2], position);
                        if (polishedExp.length() > 0) polishedExp.append("\n\n");
                        polishedExp.append(header).append("\n")
                                .append(dates).append("\n")
                                .append(duties);
                    } else {
                        if (polishedExp.length() > 0) polishedExp.append("\n\n");
                        polishedExp.append(geminiService.polishText(block));
                    }
                }
                cvData.setExperience(polishedExp.toString());
            }

            if (cvData.getEducation() != null) {
                String[] eduBlocks = cvData.getEducation().split("\n\n");
                StringBuilder polishedEdu = new StringBuilder();
                for (String block : eduBlocks) {
                    if (block.trim().isEmpty()) continue;
                    if (polishedEdu.length() > 0) polishedEdu.append("\n\n");
                    polishedEdu.append(geminiService.polishText(block));
                }
                cvData.setEducation(polishedEdu.toString());
            }

            cvData.setSkills(geminiService.polishText(cvData.getSkills()));
            cvData.setLanguages(geminiService.polishText(cvData.getLanguages()));

            if (cvData.getProjects() != null) {
                cvData.setProjects(geminiService.polishText(cvData.getProjects()));
            }
            if (cvData.getCertifications() != null) {
                cvData.setCertifications(geminiService.polishText(cvData.getCertifications()));
            }


            if (cvData.getComputerSkills() != null) {
                cvData.setComputerSkills(geminiService.polishText(cvData.getComputerSkills()));
            }
            if (cvData.getPersonalSkills() != null) {
                cvData.setPersonalSkills(geminiService.polishText(cvData.getPersonalSkills()));
            }

            cvDataRepository.save(cvData);
            log.info("CV məlumatları uğurla polish edildi");

        } catch (Exception e) {
            log.error("CvService polish xətası: {}", e.getMessage());
        }
    }
}