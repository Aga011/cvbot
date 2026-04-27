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

    private final CvDataRepository cvDataRepository;

    public void saveCvData(CvData cvData) {
        cvDataRepository.save(cvData);
        log.info("CV məlumatları saxlanıldı.");
    }
}