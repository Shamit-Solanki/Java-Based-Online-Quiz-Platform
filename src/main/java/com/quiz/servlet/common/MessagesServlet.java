package com.quiz.servlet.common;

import com.quiz.dao.MessageDAO;
import com.quiz.dao.UserDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Contact;
import com.quiz.model.Notification;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Chat-style messaging between quiz creators and participants.
 * Creators see participants, participants see creators.
 */
@WebServlet("/messages")
public class MessagesServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient MessageDAO messages = new MessageDAO();

    private Role otherRole(User me) throws QuizException {
        switch (me.getRole()) {
            case CREATOR:
                return Role.PARTICIPANT;
            case PARTICIPANT:
                return Role.CREATOR;
            default:
                throw new QuizException("Messaging is available to quiz creators and participants only.");
        }
    }

    @Override
    protected String failurePath(HttpServletRequest req) {
        String with = req.getParameter("with");
        return with == null || !with.matches("\\d+") ? "/messages" : "/messages?with=" + with;
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        User me = currentUser(req);
        Role other = otherRole(me);
        int withId = intParam(req, "with");

        if (withId > 0) {
            User partner = new UserDAO().findById(withId);
            if (partner == null || partner.getRole() != other) {
                throw new QuizException("That conversation does not exist.");
            }
            messages.markThreadRead(me.getId(), withId);
            req.setAttribute("partner", partner);
            req.setAttribute("thread", messages.thread(me.getId(), withId, 200));
        }
        // loaded after marking read so the unread badges are up to date
        List<Contact> contacts = messages.contacts(me.getId(), other);
        req.setAttribute("contacts", contacts);
        req.setAttribute("withId", withId);
        render(req, resp, "common/messages", "Messages", "messages");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        Role other = otherRole(me);
        int toId = intParam(req, "with");
        String text = Validator.required(req.getParameter("text"), "Message", 1000);

        User partner = new UserDAO().findById(toId);
        if (partner == null || partner.getRole() != other) {
            throw new QuizException("You cannot message that user.");
        }
        messages.send(me.getId(), toId, text);
        NotificationService.toUser(toId, Notification.Level.INFO,
                "New message from " + me.getName() + ".", "/messages?with=" + me.getId());
        redirect(req, resp, "/messages?with=" + toId);
    }
}
