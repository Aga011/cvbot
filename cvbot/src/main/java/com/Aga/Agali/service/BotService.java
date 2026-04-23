package com.Aga.Agali.service;

import com.Aga.Agali.bot.CvBot;
import com.Aga.Agali.enums.CvTemplate;
import com.Aga.Agali.enums.UserState;
import com.Aga.Agali.entity.CvData;
import com.Aga.Agali.entity.User;
import com.Aga.Agali.repository.CvDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Slf4j
@Service
public class BotService {

    private final UserService userService;
    private final CvService cvService;
    private final ExportService exportService;
    private final CvDataRepository cvDataRepository;
    private final ApplicationContext applicationContext;

    private final Map<Long, Map<String, String>> tempExpData = new HashMap<>();
    private final Map<Long, Map<String, String>> tempEduData = new HashMap<>();

    public BotService(UserService userService,
                      CvService cvService,
                      ExportService exportService,
                      CvDataRepository cvDataRepository,
                      ApplicationContext applicationContext) {
        this.userService = userService;
        this.cvService = cvService;
        this.exportService = exportService;
        this.cvDataRepository = cvDataRepository;
        this.applicationContext = applicationContext;
    }

    private CvBot getBot() {
        return applicationContext.getBean(CvBot.class);
    }

    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            User user = userService.getOrCreate(chatId, message.getFrom());
            if (message.hasText()) {
                String text = message.getText().trim();
                if ("/stop".equals(text)) {
                    handleStop(user);
                    return;
                }
                handleText(user, text);
            } else if (message.hasPhoto()) {
                handlePhoto(user, message);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        String data = callback.getData();
        User user = userService.getOrCreate(chatId, callback.getFrom());

        try {
            getBot().execute(new AnswerCallbackQuery(callback.getId()));
        } catch (TelegramApiException e) {
            log.error("AnswerCallbackQuery xətası: {}", e.getMessage());
        }

        switch (data) {
            case "template_1" -> {
                CvData cv1 = CvData.builder().user(user).template(CvTemplate.TEMPLATE_1).build();
                cvDataRepository.save(cv1);
                sendText(chatId, "✅ Şablon seçildi!\n\nAdınızı və soyadınızı daxil edin:\n(məs: Ağalı Ağa)");
                userService.updateState(user, UserState.COLLECTING_FULL_NAME);
            }
            case "template_2" -> {
                CvData cv2 = CvData.builder().user(user).template(CvTemplate.TEMPLATE_2).build();
                cvDataRepository.save(cv2);
                sendText(chatId, "✅ Şablon seçildi!\n\nAdınızı və soyadınızı daxil edin:\n(məs: Ağalı Ağa)");
                userService.updateState(user, UserState.COLLECTING_FULL_NAME);
            }
            case "template_3" -> {
                CvData cv3 = CvData.builder().user(user).template(CvTemplate.TEMPLATE_3).build();
                cvDataRepository.save(cv3);
                sendText(chatId, "✅ Şablon seçildi!\n\nAdınızı və soyadınızı daxil edin:\n(məs: Ağalı Ağa)");
                userService.updateState(user, UserState.COLLECTING_FULL_NAME);
            }
            case "template_4" -> {
                CvData cv4 = CvData.builder().user(user).template(CvTemplate.TEMPLATE_4).build();
                cvDataRepository.save(cv4);
                sendText(chatId, "✅ Şablon seçildi!\n\nAdınızı və soyadınızı daxil edin:\n(məs: Ağalı Ağa)");
                userService.updateState(user, UserState.COLLECTING_FULL_NAME);
            }
            case "template_5" -> {
                CvData cv5 = CvData.builder().user(user).template(CvTemplate.TEMPLATE_5).build();
                cvDataRepository.save(cv5);
                sendText(chatId, "✅ Şablon seçildi!\n\nAdınızı və soyadınızı daxil edin:\n(məs: Ağalı Ağa)");
                userService.updateState(user, UserState.COLLECTING_FULL_NAME);
            }
            case "exp_yes" -> {
                sendText(chatId, "Növbəti iş yerinizin adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EXP_COMPANY);
            }
            case "exp_no" -> {
                sendText(chatId, "İndi təhsilinizi toplayacağıq.");
                startEduCollection(user);
            }
            case "edu_bakalavr" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("degree", "Bakalavr");
                sendText(chatId, "Bakalavr universitetinin adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_UNIVERSITY);
            }
            case "edu_magistr" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("nextDegree", "Magistr");
                sendText(chatId, "Magistr dərəcəniz üçün əvvəlcə Bakalavr məlumatlarınızı daxil edin.\n\nBakalavr universitetinin adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_UNIVERSITY);
            }
            case "edu_kollec" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("degree", "Kollec");
                sendText(chatId, "Kollec adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_COLLEGE_UNIVERSITY);
            }
            case "edu_yoxdur" -> {
                sendText(chatId, "Bacarıqlarınızı daxil edin:\n\nVergüllə ayırın:\n(məs: Java, Spring Boot, SQL)");
                userService.updateState(user, UserState.COLLECTING_SKILLS);
            }
            case "edu_more_bakalavr" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("degree", "Bakalavr");
                sendText(chatId, "Universitetin adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_UNIVERSITY);
            }
            case "edu_more_magistr" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("degree", "Magistr");
                sendText(chatId, "Magistr universitetinin adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_MASTER_UNIVERSITY);
            }
            case "edu_more_kollec" -> {
                tempEduData.computeIfAbsent(chatId, k -> new HashMap<>()).put("degree", "Kollec");
                sendText(chatId, "Kollec adını daxil edin:");
                userService.updateState(user, UserState.COLLECTING_EDU_COLLEGE_UNIVERSITY);
            }
            case "edu_more_yoxdur" -> {
                sendText(chatId, "Bacarıqlarınızı daxil edin:\n\nVergüllə ayırın:\n(məs: Java, Spring Boot, SQL)");
                userService.updateState(user, UserState.COLLECTING_SKILLS);
            }
        }
    }

    private void handleStop(User user) {
        cvDataRepository.findTopByUserOrderByCreatedAtDesc(user).ifPresent(cvDataRepository::delete);
        tempExpData.remove(user.getChatId());
        tempEduData.remove(user.getChatId());
        userService.updateState(user, UserState.IDLE);
        sendText(user.getChatId(), "Əməliyyat dayandırıldı.\nYenidən başlamaq üçün /start yazın.");
    }

    private void handleText(User user, String text) {
        switch (user.getState()) {
            case IDLE -> handleIdle(user, text);
            case COLLECTING_FULL_NAME -> handleFullName(user, text);
            case COLLECTING_PROFESSION -> handleProfession(user, text);
            case COLLECTING_PHONE -> handlePhone(user, text);
            case COLLECTING_EMAIL -> handleEmail(user, text);
            case COLLECTING_ADDRESS -> handleAddress(user, text);
            case COLLECTING_ABOUT -> handleAbout(user, text);
            case COLLECTING_PHOTO -> sendText(user.getChatId(), "Zəhmət olmasa foto göndərin.");
            case COLLECTING_EXP_COMPANY -> handleExpCompany(user, text);
            case COLLECTING_EXP_POSITION -> handleExpPosition(user, text);
            case COLLECTING_EXP_START_DATE -> handleExpStartDate(user, text);
            case COLLECTING_EXP_END_DATE -> handleExpEndDate(user, text);
            case COLLECTING_EXP_DUTIES -> handleExpDuties(user, text);
            case COLLECTING_EXP_MORE -> sendExpMoreInline(user.getChatId());
            case COLLECTING_EDU_DEGREE -> sendEduDegreeInline(user.getChatId(), "Zəhmət olmasa düymələrdən birini seçin:");
            case COLLECTING_EDU_MORE -> sendEduMoreInline(user.getChatId());
            case COLLECTING_EDU_BACHELOR_UNIVERSITY -> handleEduBachelorUniversity(user, text);
            case COLLECTING_EDU_BACHELOR_FACULTY -> handleEduBachelorFaculty(user, text);
            case COLLECTING_EDU_BACHELOR_START -> handleEduBachelorStart(user, text);
            case COLLECTING_EDU_BACHELOR_END -> handleEduBachelorEnd(user, text);
            case COLLECTING_EDU_MASTER_UNIVERSITY -> handleEduMasterUniversity(user, text);
            case COLLECTING_EDU_MASTER_FACULTY -> handleEduMasterFaculty(user, text);
            case COLLECTING_EDU_MASTER_START -> handleEduMasterStart(user, text);
            case COLLECTING_EDU_MASTER_END -> handleEduMasterEnd(user, text);
            case COLLECTING_EDU_COLLEGE_UNIVERSITY -> handleEduCollegeUniversity(user, text);
            case COLLECTING_EDU_COLLEGE_FACULTY -> handleEduCollegeFaculty(user, text);
            case COLLECTING_EDU_COLLEGE_START -> handleEduCollegeStart(user, text);
            case COLLECTING_EDU_COLLEGE_END -> handleEduCollegeEnd(user, text);
            case COLLECTING_SKILLS -> handleSkills(user, text);
            case COLLECTING_LANGUAGES -> handleLanguages(user, text);
            case COLLECTING_GITHUB -> handleGithub(user, text);
            case COLLECTING_LINKEDIN -> handleLinkedin(user, text);
            case COLLECTING_PROJECTS -> handleProjects(user, text);
            case COLLECTING_CERTIFICATIONS -> handleCertifications(user, text);
            case COLLECTING_COMPUTER_SKILLS -> handleComputerSkills(user, text);
            case COLLECTING_PERSONAL_SKILLS -> handlePersonalSkills(user, text);
            case COLLECTING_CERTIFICATIONS_SIMPLE -> handleCertificationsSimple(user, text);
            default -> sendText(user.getChatId(), "Başlamaq üçün /start yazın.");
        }
    }

    private void handleIdle(User user, String text) {
        if ("/start".equals(text)) {
            sendText(user.getChatId(), "Salam! CV botuna xoş gəlmisiniz!\n\nAşağıdan CV şablonunu seçin.\nİstənilən vaxt /stop yazaraq dayandıra bilərsiniz.");
            sendTemplates(user.getChatId());
            userService.updateState(user, UserState.SELECTING_TEMPLATE);
        } else {
            sendText(user.getChatId(), "Başlamaq üçün /start yazın.");
        }
    }

    private void sendTemplates(Long chatId) {
        String[] names = {
                "Şablon 1 — Tünd Göy Sidebar",
                "Şablon 2 — Ağ Minimal",
                "Şablon 3 — Tünd Header",
                "Şablon 4 — Peşəkar",
                "Şablon 5 — Timeline"
        };
        String[] cbs = {"template_1", "template_2", "template_3", "template_4", "template_5"};
        String[] paths = {
                "/templates/cv/previews/preview1.png",
                "/templates/cv/previews/preview2.png",
                "/templates/cv/previews/preview3.png",
                "/templates/cv/previews/preview4.png",
                "/templates/cv/previews/preview5.png"
        };

        for (int i = 0; i < 5; i++) {
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText("✅ Bu şablonu seç");
            btn.setCallbackData(cbs[i]);
            row.add(btn);
            rows.add(row);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(rows);
            try {
                InputStream is = getClass().getResourceAsStream(paths[i]);
                if (is != null) {
                    getBot().execute(SendPhoto.builder()
                            .chatId(chatId.toString())
                            .photo(new InputFile(is, "preview" + (i+1) + ".png"))
                            .caption((i+1) + ". " + names[i])
                            .replyMarkup(markup).build());
                } else {
                    getBot().execute(SendMessage.builder()
                            .chatId(chatId.toString())
                            .text((i+1) + ". " + names[i])
                            .replyMarkup(markup).build());
                }
            } catch (TelegramApiException e) {
                log.error("Şablon göndərmə xətası: {}", e.getMessage());
            }
        }
    }

    private void handleFullName(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setFullName(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Peşənizi/vəzifənizi daxil edin:\n(məs: Java Backend Developer, Mühasib, Satış Mütəxəssisi)");
        userService.updateState(user, UserState.COLLECTING_PROFESSION);
    }

    private void handleProfession(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setProfession(text);
        cvData.setItProfession(isItProfession(text));
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Telefon nömrənizi daxil edin:\n(məs: +994 77 553 63 13)");
        userService.updateState(user, UserState.COLLECTING_PHONE);
    }

    private void handlePhone(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setPhone(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Email ünvanınızı daxil edin:\n(məs: ad.soyad@gmail.com)");
        userService.updateState(user, UserState.COLLECTING_EMAIL);
    }

    private void handleEmail(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setEmail(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Ünvanınızı daxil edin:\n(məs: Bakı, Yasamal rayonu)");
        userService.updateState(user, UserState.COLLECTING_ADDRESS);
    }

    private void handleAddress(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setAddress(text);
        cvDataRepository.save(cvData);
        CvTemplate t = cvData.getTemplate();
        if (t == CvTemplate.TEMPLATE_1 || t == CvTemplate.TEMPLATE_3 || t == CvTemplate.TEMPLATE_4) {
            sendText(user.getChatId(), "Profiliniz üçün foto göndərin:");
            userService.updateState(user, UserState.COLLECTING_PHOTO);
        } else {
            askAbout(user);
        }
    }

    private void askAbout(User user) {
        sendText(user.getChatId(), "Özünüz haqqında qısa məlumat yazın:\n\nNümunə:\nJava Backend Developer kimi 2 illik təcrübəyə sahibəm. Spring Boot, PostgreSQL və REST API sahəsində güclü biliklərim var.");
        userService.updateState(user, UserState.COLLECTING_ABOUT);
    }

    private void handlePhoto(User user, Message message) {
        if (user.getState() != UserState.COLLECTING_PHOTO) return;
        try {
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize largest = photos.get(photos.size() - 1);
            org.telegram.telegrambots.meta.api.objects.File telegramFile = getBot().execute(new GetFile(largest.getFileId()));
            String fileUrl = "https://api.telegram.org/file/bot" + getBot().getBotToken() + "/" + telegramFile.getFilePath();
            byte[] photoBytes;
            try (InputStream in = new URL(fileUrl).openStream()) {
                photoBytes = in.readAllBytes();
            }
            CvData cvData = getLatestCvData(user);
            cvData.setPhoto(photoBytes);
            cvDataRepository.save(cvData);
            askAbout(user);
        } catch (Exception e) {
            log.error("Foto xətası: {}", e.getMessage());
            sendText(user.getChatId(), "Foto yüklənərkən xəta baş verdi. Yenidən göndərin.");
        }
    }

    private void handleAbout(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setAbout(text);
        cvDataRepository.save(cvData);
        startExpCollection(user);
    }

    private void startExpCollection(User user) {
        tempExpData.put(user.getChatId(), new HashMap<>());
        sendText(user.getChatId(), "İndi iş təcrübənizi toplayacağıq.\n\nİş yerinizin adını daxil edin:\n(məs: Gemza Group, Emerson MMC)");
        userService.updateState(user, UserState.COLLECTING_EXP_COMPANY);
    }

    private void handleExpCompany(User user, String text) {
        tempExpData.computeIfAbsent(user.getChatId(), k -> new HashMap<>()).put("company", text);
        sendText(user.getChatId(), "Vəzifənizi daxil edin:\n(məs: CNC Mütəxəssisi, Backend Developer)");
        userService.updateState(user, UserState.COLLECTING_EXP_POSITION);
    }

    private void handleExpPosition(User user, String text) {
        tempExpData.get(user.getChatId()).put("position", text);
        sendText(user.getChatId(), "Başlama tarixini daxil edin:\n(məs: 08/2023)");
        userService.updateState(user, UserState.COLLECTING_EXP_START_DATE);
    }

    private void handleExpStartDate(User user, String text) {
        tempExpData.get(user.getChatId()).put("startDate", text);
        sendText(user.getChatId(), "Bitmə tarixini daxil edin:\n(məs: 07/2024 — hələ işləyirsinizsə \"indiyədək\" yazın)");
        userService.updateState(user, UserState.COLLECTING_EXP_END_DATE);
    }

    private void handleExpEndDate(User user, String text) {
        tempExpData.get(user.getChatId()).put("endDate", text);
        sendText(user.getChatId(), "Bu iş yerindəki vəzifə öhdəliklərini yazın:\n\nHər öhdəliyi yeni sətrdən yazın:\n- Avadanlıqların gündəlik yoxlanılması\n- Texniki sənədlərə uyğun detal emalı");
        userService.updateState(user, UserState.COLLECTING_EXP_DUTIES);
    }

    private void handleExpDuties(User user, String text) {
        Map<String, String> expMap = tempExpData.get(user.getChatId());
        expMap.put("duties", text);
        CvData cvData = getLatestCvData(user);
        String existing = cvData.getExperience() != null ? cvData.getExperience() : "";
        String newExp = "**" + expMap.get("company") + " — " + expMap.get("position") + "**\n" +
                "**" + expMap.get("startDate") + " — " + expMap.get("endDate") + "**\n" +
                expMap.get("duties");
        cvData.setExperience(existing.isEmpty() ? newExp : existing + "\n\n" + newExp);
        cvDataRepository.save(cvData);
        tempExpData.put(user.getChatId(), new HashMap<>());
        userService.updateState(user, UserState.COLLECTING_EXP_MORE);
        sendExpMoreInline(user.getChatId());
    }

    private void startEduCollection(User user) {
        tempEduData.put(user.getChatId(), new HashMap<>());
        userService.updateState(user, UserState.COLLECTING_EDU_DEGREE);
        sendEduDegreeInline(user.getChatId(), "Təhsil dərəcənizi seçin:");
    }

    private void handleEduBachelorUniversity(User user, String text) {
        tempEduData.get(user.getChatId()).put("bachUniversity", text);
        sendText(user.getChatId(), "Bakalavr ixtisasınızı daxil edin:\n(məs: Proseslərin avtomatlaşdırılması mühəndisliyi)");
        userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_FACULTY);
    }

    private void handleEduBachelorFaculty(User user, String text) {
        tempEduData.get(user.getChatId()).put("bachFaculty", text);
        sendText(user.getChatId(), "Bakalavr başlama ilini daxil edin:\n(məs: 2019)");
        userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_START);
    }

    private void handleEduBachelorStart(User user, String text) {
        tempEduData.get(user.getChatId()).put("bachStart", text);
        sendText(user.getChatId(), "Bakalavr bitmə ilini daxil edin:\n(məs: 2023)");
        userService.updateState(user, UserState.COLLECTING_EDU_BACHELOR_END);
    }

    private void handleEduBachelorEnd(User user, String text) {
        Map<String, String> edu = tempEduData.get(user.getChatId());
        edu.put("bachEnd", text);
        CvData cvData = getLatestCvData(user);
        String existing = cvData.getEducation() != null ? cvData.getEducation() : "";
        String bachEdu = "**Bakalavr**\n" + edu.get("bachUniversity") + "\n" +
                "__" + edu.get("bachFaculty") + "__\n" +
                "**" + edu.get("bachStart") + " — " + edu.get("bachEnd") + "**";
        cvData.setEducation(existing.isEmpty() ? bachEdu : existing + "\n\n" + bachEdu);
        cvDataRepository.save(cvData);
        if ("Magistr".equals(edu.get("nextDegree"))) {
            edu.remove("nextDegree");
            sendText(user.getChatId(), "İndi Magistr məlumatlarınızı daxil edin.\n\nMagistr universitetinin adını daxil edin:");
            userService.updateState(user, UserState.COLLECTING_EDU_MASTER_UNIVERSITY);
        } else {
            askMoreEdu(user);
        }
    }

    private void handleEduMasterUniversity(User user, String text) {
        tempEduData.get(user.getChatId()).put("mastUniversity", text);
        sendText(user.getChatId(), "Magistr ixtisasınızı daxil edin:");
        userService.updateState(user, UserState.COLLECTING_EDU_MASTER_FACULTY);
    }

    private void handleEduMasterFaculty(User user, String text) {
        tempEduData.get(user.getChatId()).put("mastFaculty", text);
        sendText(user.getChatId(), "Magistr başlama ilini daxil edin:\n(məs: 2023)");
        userService.updateState(user, UserState.COLLECTING_EDU_MASTER_START);
    }

    private void handleEduMasterStart(User user, String text) {
        tempEduData.get(user.getChatId()).put("mastStart", text);
        sendText(user.getChatId(), "Magistr bitmə ilini daxil edin:\n(məs: 2025 — davam edirsə \"davam edir\" yazın)");
        userService.updateState(user, UserState.COLLECTING_EDU_MASTER_END);
    }

    private void handleEduMasterEnd(User user, String text) {
        Map<String, String> edu = tempEduData.get(user.getChatId());
        edu.put("mastEnd", text);
        CvData cvData = getLatestCvData(user);
        String existing = cvData.getEducation() != null ? cvData.getEducation() : "";
        String mastEdu = "**Magistr**\n" + edu.get("mastUniversity") + "\n" +
                "__" + edu.get("mastFaculty") + "__\n" +
                "**" + edu.get("mastStart") + " — " + edu.get("mastEnd") + "**";
        cvData.setEducation(existing.isEmpty() ? mastEdu : existing + "\n\n" + mastEdu);
        cvDataRepository.save(cvData);
        askMoreEdu(user);
    }

    private void handleEduCollegeUniversity(User user, String text) {
        tempEduData.get(user.getChatId()).put("collUniversity", text);
        sendText(user.getChatId(), "Kollec ixtisasınızı daxil edin:");
        userService.updateState(user, UserState.COLLECTING_EDU_COLLEGE_FACULTY);
    }

    private void handleEduCollegeFaculty(User user, String text) {
        tempEduData.get(user.getChatId()).put("collFaculty", text);
        sendText(user.getChatId(), "Kollec başlama ilini daxil edin:");
        userService.updateState(user, UserState.COLLECTING_EDU_COLLEGE_START);
    }

    private void handleEduCollegeStart(User user, String text) {
        tempEduData.get(user.getChatId()).put("collStart", text);
        sendText(user.getChatId(), "Kollec bitmə ilini daxil edin:");
        userService.updateState(user, UserState.COLLECTING_EDU_COLLEGE_END);
    }

    private void handleEduCollegeEnd(User user, String text) {
        Map<String, String> edu = tempEduData.get(user.getChatId());
        edu.put("collEnd", text);
        CvData cvData = getLatestCvData(user);
        String existing = cvData.getEducation() != null ? cvData.getEducation() : "";
        String collEdu = "**Kollec**\n" + edu.get("collUniversity") + "\n" +
                "__" + edu.get("collFaculty") + "__\n" +
                "**" + edu.get("collStart") + " — " + edu.get("collEnd") + "**";
        cvData.setEducation(existing.isEmpty() ? collEdu : existing + "\n\n" + collEdu);
        cvDataRepository.save(cvData);
        askMoreEdu(user);
    }

    private void askMoreEdu(User user) {
        tempEduData.put(user.getChatId(), new HashMap<>());
        userService.updateState(user, UserState.COLLECTING_EDU_MORE);
        sendEduMoreInline(user.getChatId());
    }

    private void handleSkills(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setSkills(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Dil biliklərini daxil edin:\n\n(məs: Azərbaycan — Ana dili, İngilis — B2, Rus — A2)");
        userService.updateState(user, UserState.COLLECTING_LANGUAGES);
    }

    private void handleLanguages(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setLanguages(text);
        cvDataRepository.save(cvData);
        if (cvData.isItProfession()) {
            sendText(user.getChatId(), "GitHub profilinizin linkini daxil edin:\n(məs: github.com/username)\n\nYoxdursa — \"yoxdur\" yazın");
            userService.updateState(user, UserState.COLLECTING_GITHUB);
        } else {
            sendText(user.getChatId(), "Komputer biliklərini daxil edin:\n\n(məs: Microsoft Word, Excel, PowerPoint, 1C)");
            userService.updateState(user, UserState.COLLECTING_COMPUTER_SKILLS);
        }
    }

    private void handleGithub(User user, String text) {
        CvData cvData = getLatestCvData(user);
        if (!"yoxdur".equalsIgnoreCase(text.trim())) cvData.setGithubLink(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "LinkedIn profilinizin linkini daxil edin:\n(məs: linkedin.com/in/username)\n\nYoxdursa — \"yoxdur\" yazın");
        userService.updateState(user, UserState.COLLECTING_LINKEDIN);
    }

    private void handleLinkedin(User user, String text) {
        CvData cvData = getLatestCvData(user);
        if (!"yoxdur".equalsIgnoreCase(text.trim())) cvData.setLinkedinLink(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Layihələriniz varmı?\n\nVarsa hər layihəni belə yazın:\nLayihə adı — texnologiyalar — qısa izahat\n\nYoxdursa — \"yoxdur\" yazın");
        userService.updateState(user, UserState.COLLECTING_PROJECTS);
    }

    private void handleProjects(User user, String text) {
        CvData cvData = getLatestCvData(user);
        if (!"yoxdur".equalsIgnoreCase(text.trim())) cvData.setProjects(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Sertifikatlarınız varmı?\n\nVarsa belə yazın:\nSertifikat adı — qurum — il\n\nYoxdursa — \"yoxdur\" yazın");
        userService.updateState(user, UserState.COLLECTING_CERTIFICATIONS);
    }

    private void handleCertifications(User user, String text) {
        CvData cvData = getLatestCvData(user);
        if (!"yoxdur".equalsIgnoreCase(text.trim())) cvData.setCertifications(text);
        cvDataRepository.save(cvData);
        generateAndSendCv(user);
    }

    private void handleComputerSkills(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setComputerSkills(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Şəxsi bacarıqlarınızı daxil edin:\n\n(məs: Komanda ilə işləmək, Analitik düşüncə, Problem həll etmə)");
        userService.updateState(user, UserState.COLLECTING_PERSONAL_SKILLS);
    }

    private void handlePersonalSkills(User user, String text) {
        CvData cvData = getLatestCvData(user);
        cvData.setPersonalSkills(text);
        cvDataRepository.save(cvData);
        sendText(user.getChatId(), "Sertifikatlarınız varmı?\n\nVarsa belə yazın:\nSertifikat adı — qurum — il\n\nYoxdursa — \"yoxdur\" yazın");
        userService.updateState(user, UserState.COLLECTING_CERTIFICATIONS_SIMPLE);
    }

    private void handleCertificationsSimple(User user, String text) {
        CvData cvData = getLatestCvData(user);
        if (!"yoxdur".equalsIgnoreCase(text.trim())) cvData.setCertifications(text);
        cvDataRepository.save(cvData);
        generateAndSendCv(user);
    }

    private void generateAndSendCv(User user) {
        CvData cvData = getLatestCvData(user);
        sendText(user.getChatId(), "CV hazırlanır, zəhmət olmasa gözləyin...");
        userService.updateState(user, UserState.GENERATING_CV);
        try {
            cvService.polishWithGemini(cvData);
            File pdf = exportService.generatePdf(cvData);
            sendDocument(user.getChatId(), pdf, "CV.pdf");
            userService.updateState(user, UserState.IDLE);
            sendText(user.getChatId(), "CV-niz hazırdır! Uğurlar!\n\nYeni CV üçün /start yazın.");
        } catch (Exception e) {
            log.error("CV generasiya xətası: {}", e.getMessage(), e);
            sendText(user.getChatId(), "Xəta baş verdi. Zəhmət olmasa /start ilə yenidən cəhd edin.");
            userService.updateState(user, UserState.IDLE);
        }
    }

    private void sendExpMoreInline(Long chatId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton beli = new InlineKeyboardButton();
        beli.setText("✅ Bəli");
        beli.setCallbackData("exp_yes");
        InlineKeyboardButton xeyr = new InlineKeyboardButton();
        xeyr.setText("❌ Xeyr");
        xeyr.setCallbackData("exp_no");
        row.add(beli);
        row.add(xeyr);
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        try {
            getBot().execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Başqa iş yeriniz varmı?")
                    .replyMarkup(markup).build());
        } catch (TelegramApiException e) {
            log.error("sendExpMoreInline xətası: {}", e.getMessage());
        }
    }

    private void sendEduDegreeInline(Long chatId, String questionText) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton k = new InlineKeyboardButton();
        k.setText("🎓 Kollec"); k.setCallbackData("edu_kollec");
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText("🎓 Bakalavr"); b.setCallbackData("edu_bakalavr");
        row1.add(k); row1.add(b);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton m = new InlineKeyboardButton();
        m.setText("🎓 Magistr"); m.setCallbackData("edu_magistr");
        InlineKeyboardButton y = new InlineKeyboardButton();
        y.setText("❌ Yoxdur"); y.setCallbackData("edu_yoxdur");
        row2.add(m); row2.add(y);
        rows.add(row1); rows.add(row2);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        try {
            getBot().execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(questionText)
                    .replyMarkup(markup).build());
        } catch (TelegramApiException e) {
            log.error("sendEduDegreeInline xətası: {}", e.getMessage());
        }
    }

    private void sendEduMoreInline(Long chatId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton k = new InlineKeyboardButton();
        k.setText("🎓 Kollec"); k.setCallbackData("edu_more_kollec");
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText("🎓 Bakalavr"); b.setCallbackData("edu_more_bakalavr");
        row1.add(k); row1.add(b);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton m = new InlineKeyboardButton();
        m.setText("🎓 Magistr"); m.setCallbackData("edu_more_magistr");
        InlineKeyboardButton y = new InlineKeyboardButton();
        y.setText("❌ Yoxdur"); y.setCallbackData("edu_more_yoxdur");
        row2.add(m); row2.add(y);
        rows.add(row1); rows.add(row2);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        try {
            getBot().execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Başqa təhsiliniz varmı?")
                    .replyMarkup(markup).build());
        } catch (TelegramApiException e) {
            log.error("sendEduMoreInline xətası: {}", e.getMessage());
        }
    }

    private boolean isItProfession(String profession) {
        String lower = profession.toLowerCase();
        String[] keywords = {"developer", "programmer", "engineer", "frontend", "backend",
                "fullstack", "devops", "data", "software", "mobile", "web", "qa", "tester",
                "analyst", "architect", "proqramçı", "tərtibatçı", "it ", "cyber", "network",
                "database", "cloud"};
        for (String keyword : keywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private CvData getLatestCvData(User user) {
        return cvDataRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("CvData tapılmadı: " + user.getChatId()));
    }

    public void sendText(Long chatId, String text) {
        try {
            getBot().execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Mesaj göndərmə xətası: {}", e.getMessage());
        }
    }

    public void sendText(Long chatId, String text,
                         org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard keyboard) {
        try {
            getBot().execute(SendMessage.builder().chatId(chatId.toString()).text(text).replyMarkup(keyboard).build());
        } catch (TelegramApiException e) {
            log.error("Mesaj göndərmə xətası: {}", e.getMessage());
        }
    }

    private void sendDocument(Long chatId, File file, String fileName) {
        try {
            getBot().execute(SendDocument.builder().chatId(chatId.toString())
                    .document(new InputFile(file, fileName)).build());
        } catch (TelegramApiException e) {
            log.error("Fayl göndərmə xətası: {}", e.getMessage());
        }
    }

    private ReplyKeyboardRemove removeKeyboard() {
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        return remove;
    }
}