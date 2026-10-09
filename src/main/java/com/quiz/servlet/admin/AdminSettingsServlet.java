package com.quiz.servlet.admin;

import com.quiz.dao.SettingsDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Notification;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.util.AppSettings;
import com.quiz.util.SettingDef;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** System-wide settings (platform name, attempts, pass mark, sign-up switch ...). */
@WebServlet("/admin/settings")
public class AdminSettingsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("definitions", AppSettings.definitions());
        req.setAttribute("values", AppSettings.snapshot());
        render(req, resp, "admin/settings", "System settings", "settings");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        Map<String, String> toSave = new LinkedHashMap<>();
        for (SettingDef def : AppSettings.definitions()) {
            String raw = param(req, def.getKey());
            switch (def.getType()) {
                case "boolean":
                    toSave.put(def.getKey(), "true".equals(raw) ? "true" : "false");
                    break;
                case "number":
                    toSave.put(def.getKey(), String.valueOf(validateNumber(def, raw)));
                    break;
                default:
                    int max = "announcement".equals(def.getKey()) ? 300 : 60;
                    toSave.put(def.getKey(), "platform_name".equals(def.getKey())
                            ? Validator.required(raw, def.getLabel(), max)
                            : Validator.optional(raw, def.getLabel(), max));
            }
        }
        new SettingsDAO().saveAll(toSave);
        AppSettings.reload();
        NotificationService.toAdmins(Notification.Level.INFO, me.getName() + " updated the system settings.", "/admin/settings");
        flash(req, "success", "Settings saved.");
        redirect(req, resp, "/admin/settings");
    }

    private int validateNumber(SettingDef def, String raw) throws ValidationException {
        switch (def.getKey()) {
            case "max_attempts":
                return Validator.intInRange(raw, def.getLabel(), 1, 20);
            case "default_duration":
                return Validator.intInRange(raw, def.getLabel(), 1, 300);
            case "pass_percentage":
                return Validator.intInRange(raw, def.getLabel(), 1, 100);
            default:
                return Validator.intInRange(raw, def.getLabel(), 0, 100000);
        }
    }
}
