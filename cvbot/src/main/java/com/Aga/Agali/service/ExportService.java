package com.Aga.Agali.service;

import com.Aga.Agali.enums.CvTemplate;
import com.Aga.Agali.entity.CvData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Slf4j
@Service
public class ExportService {

    @Value("${puppeteer.script.path}")
    private String puppeteerScriptPath;

    public File generatePdf(CvData cvData) throws IOException {
        String templateName = getTemplateName(cvData.getTemplate());
        String html = loadTemplate(templateName);
        html = fillTemplate(html, cvData);

        File htmlFile = File.createTempFile("cv_", ".html");
        Files.writeString(htmlFile.toPath(), html, StandardCharsets.UTF_8);

        File pdfFile = File.createTempFile("cv_", ".pdf");

        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Program Files\\nodejs\\node.exe",
                puppeteerScriptPath,
                htmlFile.getAbsolutePath(),
                pdfFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Puppeteer xətası: {}", output);
                throw new IOException("PDF yaradıla bilmədi: " + output);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PDF prosesi kəsildi", e);
        } finally {
            htmlFile.delete();
        }

        log.info("PDF yaradıldı");
        return pdfFile;
    }

    private String getTemplateName(CvTemplate template) {
        if (template == null) return "template1.html";
        return switch (template) {
            case TEMPLATE_1 -> "template1.html";
            case TEMPLATE_2 -> "template2.html";
            case TEMPLATE_3 -> "template3.html";
            case TEMPLATE_4 -> "template4.html";
            case TEMPLATE_5 -> "template5.html";
        };
    }

    private String loadTemplate(String name) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/templates/cv/" + name)) {
            if (is == null) throw new IOException("Şablon tapılmadı: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String fillTemplate(String html, CvData cvData) {
        // Əsas məlumatlar
        html = html.replace("${fullName}", safe(cvData.getFullName()));
        html = html.replace("${firstNameHtml}", buildFirstName(cvData.getFullName()));
        html = html.replace("${profession}", safe(cvData.getProfession()));
        html = html.replace("${phone}", safe(cvData.getPhone()));
        html = html.replace("${email}", safe(cvData.getEmail()));
        html = html.replace("${address}", safe(cvData.getAddress()));
        html = html.replace("${about}", safe(cvData.getAbout()));

        // Foto
        html = html.replace("${photoHtml}", buildPhotoHtml(cvData.getPhoto()));

        // Linklər
        html = html.replace("${githubHtml}", buildContactItem("🔗 GitHub: ", cvData.getGithubLink()));
        html = html.replace("${linkedinHtml}", buildContactItem("💼 LinkedIn: ", cvData.getLinkedinLink()));
        html = html.replace("${githubLineHtml}", buildSeparatorLink(cvData.getGithubLink()));
        html = html.replace("${linkedinLineHtml}", buildSeparatorLink(cvData.getLinkedinLink()));

        // Sidebar linklər (template1 üçün)
        html = html.replace("${githubSidebarHtml}", buildSidebarLink("GitHub", cvData.getGithubLink()));
        html = html.replace("${linkedinSidebarHtml}", buildSidebarLink("LinkedIn", cvData.getLinkedinLink()));

        // Bacarıqlar
        html = html.replace("${skillsHtml}", buildSkillsHtml(cvData.getSkills(), cvData.getTemplate()));
        html = html.replace("${languagesHtml}", buildLanguagesHtml(cvData.getLanguages()));

        // İş təcrübəsi
        html = html.replace("${experienceHtml}", buildExperienceHtml(cvData.getExperience(), cvData.getTemplate()));

        // Təhsil
        html = html.replace("${educationHtml}", buildEducationHtml(cvData.getEducation(), cvData.getTemplate()));

        // Layihələr
        html = html.replace("${projectsHtml}", buildProjectsHtml(cvData.getProjects(), cvData.getTemplate()));
        html = html.replace("${projectsSectionHtml}", buildProjectsSectionHtml(cvData.getProjects()));

        // Sertifikatlar
        html = html.replace("${certificationsHtml}", buildCertificationsHtml(cvData.getCertifications(), cvData.getTemplate()));
        html = html.replace("${certificationsSectionHtml}", buildCertificationsSectionHtml(cvData.getCertifications()));

        // Qeyri-IT
        html = html.replace("${computerSkillsHtml}", buildComputerSkillsHtml(cvData.getComputerSkills(), cvData.getTemplate()));
        html = html.replace("${personalSkillsHtml}", buildPersonalSkillsHtml(cvData.getPersonalSkills(), cvData.getTemplate()));
        html = html.replace("${computerSkillsSectionHtml}", buildSectionHtml("Komputer Bilikləri", cvData.getComputerSkills()));
        html = html.replace("${personalSkillsSectionHtml}", buildSectionHtml("Şəxsi Bacarıqlar", cvData.getPersonalSkills()));

        return html;
    }

    private String safe(String v) {
        if (v == null || v.isEmpty()) return "";
        v = v.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        v = v.replaceAll("\\*\\*(.+?)\\*\\*", "<strong class=\"bold-title\">$1</strong>");
        v = v.replaceAll("__(.+?)__", "<em class=\"italic-text\">$1</em>");
        return v;
    }

    private String buildFirstName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.trim().split(" ", 2);
        if (parts.length == 1) return "<strong>" + safe(parts[0]) + "</strong>";
        return "<strong>" + safe(parts[0]) + "</strong> <span>" + safe(parts[1]) + "</span>";
    }

    private String buildPhotoHtml(byte[] photo) {
        if (photo == null || photo.length == 0) {
            return "<div class=\"photo-placeholder\">👤</div>";
        }
        String base64 = Base64.getEncoder().encodeToString(photo);
        return "<div class=\"photo\"><img src=\"data:image/jpeg;base64," + base64 + "\"/></div>";
    }

    private String buildContactItem(String label, String link) {
        if (link == null || link.isEmpty()) return "";
        return "<p class=\"contact-item\">" + label + safe(link) + "</p>";
    }

    private String buildSeparatorLink(String link) {
        if (link == null || link.isEmpty()) return "";
        return " <span class=\"separator\"></span> " + safe(link);
    }

    private String buildSidebarLink(String label, String link) {
        if (link == null || link.isEmpty()) return "";
        return "<div class=\"link-item\">" + label + ": " + safe(link) + "</div>";
    }

    private String buildSkillsHtml(String skills, CvTemplate template) {
        if (skills == null || skills.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String skill : skills.split("[,\n]")) {
            String s = skill.trim();
            if (s.isEmpty()) continue;
            if (template == CvTemplate.TEMPLATE_1) {
                sb.append("<div class=\"skill-item\">")
                        .append("<div class=\"skill-name\">").append(safe(s)).append("</div>")
                        .append("<div class=\"skill-bar\"><div class=\"skill-fill\" style=\"width:80%\"></div></div>")
                        .append("</div>");
            } else if (template == CvTemplate.TEMPLATE_2) {
                sb.append("<span class=\"skill-tag\">").append(safe(s)).append("</span>");
            } else {
                sb.append("<div class=\"skill-item\">• ").append(safe(s)).append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildLanguagesHtml(String languages) {
        if (languages == null || languages.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String lang : languages.split("[,\n]")) {
            String l = lang.trim();
            if (!l.isEmpty()) {
                sb.append("<div class=\"lang-item\">").append(safe(l)).append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildExperienceHtml(String experience, CvTemplate template) {
        if (experience == null || experience.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] blocks = experience.split("\n\n");
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;
            String[] lines = block.split("\n", 3);

            if (template == CvTemplate.TEMPLATE_2) {
                sb.append("<div class=\"exp-item\">");
                sb.append("<div class=\"exp-left\">");
                if (lines.length >= 1) {
                    String header = lines[0].replaceAll("\\*\\*", "");
                    String[] parts = header.split(" — ", 2);
                    sb.append("<div class=\"exp-company\">").append(safe(parts[0])).append("</div>");
                    if (parts.length > 1)
                        sb.append("<div class=\"exp-position\">").append(safe(parts[1])).append("</div>");
                }
                if (lines.length >= 2)
                    sb.append("<div class=\"exp-date\">").append(safe(lines[1].replaceAll("\\*\\*", ""))).append("</div>");
                sb.append("</div>");
                sb.append("<div class=\"exp-right\">");
                if (lines.length >= 3)
                    sb.append("<div class=\"exp-duties\">").append(safe(lines[2]).replace("\n", "<br/>")).append("</div>");
                sb.append("</div>");
                sb.append("</div>");
            } else {
                sb.append("<div class=\"exp\">");
                if (lines.length >= 1)
                    sb.append("<div class=\"exp-header\">").append(safe(lines[0])).append("</div>");
                if (lines.length >= 2)
                    sb.append("<div class=\"exp-date\">").append(safe(lines[1])).append("</div>");
                if (lines.length >= 3)
                    sb.append("<div class=\"exp-duties\">").append(safe(lines[2]).replace("\n", "<br/>")).append("</div>");
                sb.append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildEducationHtml(String education, CvTemplate template) {
        if (education == null || education.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] blocks = education.split("\n\n");
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;
            String[] lines = block.split("\n");

            if (template == CvTemplate.TEMPLATE_2) {
                sb.append("<div class=\"edu-item\">");
                sb.append("<div class=\"edu-left\">");
                if (lines.length >= 2)
                    sb.append("<div class=\"edu-university\">").append(safe(lines[1])).append("</div>");
                if (lines.length >= 4)
                    sb.append("<div class=\"edu-date\">").append(safe(lines[3])).append("</div>");
                sb.append("</div>");
                sb.append("<div class=\"edu-right\">");
                if (lines.length >= 1)
                    sb.append("<div class=\"edu-degree\">").append(safe(lines[0])).append("</div>");
                if (lines.length >= 3)
                    sb.append("<div class=\"edu-faculty\">").append(safe(lines[2])).append("</div>");
                sb.append("</div>");
                sb.append("</div>");
            } else {
                sb.append("<div class=\"edu-item\">");
                if (lines.length >= 1)
                    sb.append("<div class=\"edu-degree\">").append(safe(lines[0])).append("</div>");
                if (lines.length >= 2)
                    sb.append("<div class=\"edu-university\">").append(safe(lines[1])).append("</div>");
                if (lines.length >= 3)
                    sb.append("<div class=\"edu-faculty\">").append(safe(lines[2])).append("</div>");
                if (lines.length >= 4)
                    sb.append("<div class=\"edu-date\">").append(safe(lines[3])).append("</div>");
                sb.append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildProjectsHtml(String projects, CvTemplate template) {
        if (projects == null || projects.isEmpty()) return "";
        if (template == CvTemplate.TEMPLATE_2) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"section\"><h2>LAYİHƏLƏR</h2>");
        for (String p : projects.split("\n")) {
            if (p.trim().isEmpty()) continue;
            String[] parts = p.split(" — ", 3);
            sb.append("<div class=\"project-item\">");
            sb.append("<div class=\"project-name\">").append(safe(parts[0])).append("</div>");
            if (parts.length > 1)
                sb.append("<div class=\"project-desc\">").append(safe(parts[1])).append("</div>");
            if (parts.length > 2)
                sb.append("<div class=\"project-desc\">").append(safe(parts[2])).append("</div>");
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String buildProjectsSectionHtml(String projects) {
        if (projects == null || projects.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"section\"><div class=\"section-title\">Layihələr</div>");
        for (String p : projects.split("\n")) {
            if (p.trim().isEmpty()) continue;
            String[] parts = p.split(" — ", 3);
            sb.append("<div class=\"project-item\">");
            sb.append("<div class=\"project-name\">").append(safe(parts[0])).append("</div>");
            if (parts.length > 1)
                sb.append("<div class=\"project-desc\">").append(safe(parts[1])).append("</div>");
            if (parts.length > 2)
                sb.append("<div class=\"project-desc\">").append(safe(parts[2])).append("</div>");
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String buildCertificationsHtml(String certs, CvTemplate template) {
        if (certs == null || certs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        if (template == CvTemplate.TEMPLATE_1) {
            sb.append("<h3>Sertifikatlar</h3>");
        } else {
            sb.append("<h2>SERTİFİKATLAR</h2>");
        }
        for (String c : certs.split("\n")) {
            if (!c.trim().isEmpty())
                sb.append("<div class=\"cert-item\">").append(safe(c.trim())).append("</div>");
        }
        return sb.toString();
    }

    private String buildCertificationsSectionHtml(String certs) {
        if (certs == null || certs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"section\"><div class=\"section-title\">Sertifikatlar</div>");
        for (String c : certs.split("\n")) {
            if (!c.trim().isEmpty())
                sb.append("<div class=\"cert-item\">").append(safe(c.trim())).append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String buildComputerSkillsHtml(String skills, CvTemplate template) {
        if (skills == null || skills.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        if (template == CvTemplate.TEMPLATE_1) {
            sb.append("<h3>Komputer Bilikləri</h3>");
            for (String s : skills.split("[,\n]")) {
                if (!s.trim().isEmpty())
                    sb.append("<div class=\"skill-item\"><div class=\"skill-name\">").append(safe(s.trim())).append("</div></div>");
            }
        } else {
            sb.append("<h2>KOMPUTER BİLİKLƏRİ</h2>");
            for (String s : skills.split("[,\n]")) {
                if (!s.trim().isEmpty())
                    sb.append("<div class=\"skill-item\">• ").append(safe(s.trim())).append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildPersonalSkillsHtml(String skills, CvTemplate template) {
        if (skills == null || skills.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        if (template == CvTemplate.TEMPLATE_1) {
            sb.append("<h3>Şəxsi Bacarıqlar</h3>");
            for (String s : skills.split("[,\n]")) {
                if (!s.trim().isEmpty())
                    sb.append("<div class=\"skill-item\"><div class=\"skill-name\">").append(safe(s.trim())).append("</div></div>");
            }
        } else {
            sb.append("<h2>ŞƏXSİ BACARIQLAR</h2>");
            for (String s : skills.split("[,\n]")) {
                if (!s.trim().isEmpty())
                    sb.append("<div class=\"skill-item\">• ").append(safe(s.trim())).append("</div>");
            }
        }
        return sb.toString();
    }

    private String buildSectionHtml(String title, String items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"section\"><div class=\"section-title\">").append(title).append("</div><div class=\"skills-grid\">");
        for (String s : items.split("[,\n]")) {
            if (!s.trim().isEmpty())
                sb.append("<span class=\"skill-tag\">").append(safe(s.trim())).append("</span>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }
}