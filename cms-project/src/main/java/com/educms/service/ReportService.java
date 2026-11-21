package com.educms.service;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.ContentDAO;
import com.educms.dao.MediaDAO;
import com.educms.dao.UserDAO;
import com.educms.model.ContentItem;
import com.educms.model.ContentType;
import com.educms.model.MediaFile;
import com.educms.model.User;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates PDF reports (content summary, media inventory, user activity)
 * for record-keeping purposes, using iText 7.
 */
public class ReportService {

    private final ContentDAO contentDAO = new ContentDAO();
    private final MediaDAO mediaDAO = new MediaDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Builds a full CMS report: content summary, media inventory, users, recent activity. */
    public void generateFullReport(String outputPath) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(outputPath);
        try (PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {

            doc.add(new Paragraph("Educational CMS - System Report")
                    .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(FMT))
                    .setFontSize(10).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            addSummarySection(doc);
            addContentSection(doc, "Articles", ContentType.ARTICLE);
            addContentSection(doc, "Blog Posts", ContentType.BLOG);
            addMediaSection(doc);
            addUsersSection(doc);
            addActivitySection(doc);
        }
    }

    private void addSummarySection(Document doc) {
        doc.add(sectionHeader("Summary"));
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        addRow(table, "Total Articles", String.valueOf(contentDAO.countByType(ContentType.ARTICLE)));
        addRow(table, "Total Blog Posts", String.valueOf(contentDAO.countByType(ContentType.BLOG)));
        addRow(table, "Total Media Files", String.valueOf(mediaDAO.countAll()));
        addRow(table, "Total Registered Users", String.valueOf(userDAO.countAll()));
        MediaFile dummy = new MediaFile();
        dummy.setFileSize(mediaDAO.totalStorageBytes());
        addRow(table, "Total Media Storage Used", dummy.getReadableSize());
        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addContentSection(Document doc, String heading, ContentType type) {
        doc.add(sectionHeader(heading));
        List<ContentItem> items = contentDAO.findByType(type);
        if (items.isEmpty()) {
            doc.add(new Paragraph("No records.").setItalic().setFontColor(ColorConstants.GRAY));
            return;
        }
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2, 2})).useAllAvailableWidth();
        headerRow(table, "Title", "Author", "Category", "Status", "Updated");
        for (ContentItem c : items) {
            addRow(table, c.getTitle(), c.getAuthorName(),
                    c.getCategory() == null ? "-" : c.getCategory(),
                    c.getStatus().name(),
                    c.getUpdatedAt() != null ? c.getUpdatedAt().format(FMT) : "-");
        }
        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addMediaSection(Document doc) {
        doc.add(sectionHeader("Media Library"));
        List<MediaFile> media = mediaDAO.findAll();
        if (media.isEmpty()) {
            doc.add(new Paragraph("No records.").setItalic().setFontColor(ColorConstants.GRAY));
            return;
        }
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1, 2, 2})).useAllAvailableWidth();
        headerRow(table, "Filename", "Type", "Size", "Uploaded By", "Uploaded");
        for (MediaFile m : media) {
            addRow(table, m.getFilename(), nullSafe(m.getFileType()), m.getReadableSize(),
                    m.getUploaderName(), m.getUploadedAt() != null ? m.getUploadedAt().format(FMT) : "-");
        }
        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addUsersSection(Document doc) {
        doc.add(sectionHeader("Registered Users"));
        List<User> users = userDAO.findAll();
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2})).useAllAvailableWidth();
        headerRow(table, "Full Name", "Username", "Role", "Last Login");
        for (User u : users) {
            addRow(table, u.getFullName(), u.getUsername(), u.getRole().name(),
                    u.getLastLogin() != null ? u.getLastLogin().format(FMT) : "Never");
        }
        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addActivitySection(Document doc) {
        doc.add(sectionHeader("Recent Activity (last 25)"));
        List<ActivityLogDAO.Entry> entries = activityLogDAO.recent(25);
        if (entries.isEmpty()) {
            doc.add(new Paragraph("No activity recorded yet.").setItalic().setFontColor(ColorConstants.GRAY));
            return;
        }
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 2})).useAllAvailableWidth();
        headerRow(table, "User", "Action", "Details", "When");
        for (ActivityLogDAO.Entry e : entries) {
            addRow(table, nullSafe(e.userName()), e.action(), nullSafe(e.details()),
                    e.timestamp() != null ? e.timestamp().format(FMT) : "-");
        }
        doc.add(table);
    }

    private Paragraph sectionHeader(String text) {
        return new Paragraph(text).setFontSize(14).setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(52, 73, 94))
                .setPadding(6);
    }

    private void headerRow(Table table, String... headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        }
    }

    private void addRow(Table table, String... cells) {
        for (String c : cells) {
            table.addCell(new Cell().add(new Paragraph(c == null ? "-" : c)).setFontSize(9));
        }
    }

    private String nullSafe(String s) {
        return s == null ? "-" : s;
    }
}
