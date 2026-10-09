package com.quiz.service;

import com.quiz.dao.ReminderDAO;
import com.quiz.model.Notification;
import com.quiz.model.Reminder;
import java.sql.SQLException;
import java.util.List;

/** Turns due reminders into notifications. Run periodically by the background scheduler. */
public final class ReminderService {

    private static final ReminderDAO DAO = new ReminderDAO();

    private ReminderService() {
    }

    /**
     * Synchronized so two overlapping runs can never notify the same reminder twice.
     *
     * @return how many reminders were dispatched
     */
    public static synchronized int dispatchDue() throws SQLException {
        List<Reminder> due = DAO.findDueUnnotified();
        for (Reminder r : due) {
            DAO.markNotified(r.getId());
            String text = "Reminder: time to take \"" + r.getQuizTitle() + "\""
                    + (r.getNote() == null || r.getNote().isBlank() ? "." : " - " + r.getNote());
            NotificationService.toUser(r.getParticipantId(), Notification.Level.INFO, text, "/participant/quizzes");
        }
        return due.size();
    }
}
