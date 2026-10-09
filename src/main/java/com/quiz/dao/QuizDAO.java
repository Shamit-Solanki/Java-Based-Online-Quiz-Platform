package com.quiz.dao;

import com.quiz.exception.QuizException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Database access for quizzes and their questions. */
public class QuizDAO implements CrudDAO<Quiz<Question>> {

    /** Base query: quiz + creator name + question/attempt counters + class average. */
    private static final String SELECT_BASE =
            "SELECT q.*, u.name AS creator_name, "
          + " (SELECT COUNT(*) FROM questions x WHERE x.quiz_id = q.id) AS q_count, "
          + " (SELECT COUNT(*) FROM quiz_attempts a WHERE a.quiz_id = q.id AND a.submitted_at IS NOT NULL) AS a_count, "
          + " (SELECT COALESCE(ROUND(AVG(a.score * 100 / a.total_questions), 1), 0) FROM quiz_attempts a "
          + "   WHERE a.quiz_id = q.id AND a.submitted_at IS NOT NULL AND a.total_questions > 0) AS avg_pct "
          + "FROM quizzes q JOIN users u ON u.id = q.creator_id ";

    // ------------------------------------------------------------------ create / update

    /** Inserts a quiz with all its questions in one transaction and returns the new quiz id. */
    public int create(Quiz<Question> quiz) throws SQLException {
        String sql = "INSERT INTO quizzes(title, description, duration_minutes, creator_id, status) VALUES(?,?,?,?,'DRAFT')";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, quiz.getTitle());
                p.setString(2, quiz.getDescription());
                p.setInt(3, quiz.getDurationMinutes());
                p.setInt(4, quiz.getCreatorId());
                p.executeUpdate();
                int id;
                try (ResultSet keys = p.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No id generated for quiz");
                    }
                    id = keys.getInt(1);
                }
                insertQuestions(c, id, quiz.getQuestions());
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Replaces title, settings and all questions of a quiz that is still editable.
     * An edited (previously rejected) quiz goes back to DRAFT.
     */
    public void update(Quiz<Question> quiz) throws SQLException, QuizException {
        String sql = "UPDATE quizzes SET title=?, description=?, duration_minutes=?, status='DRAFT', review_note=NULL "
                   + "WHERE id=? AND creator_id=? AND status IN ('DRAFT','REJECTED')";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, quiz.getTitle());
                p.setString(2, quiz.getDescription());
                p.setInt(3, quiz.getDurationMinutes());
                p.setInt(4, quiz.getId());
                p.setInt(5, quiz.getCreatorId());
                if (p.executeUpdate() == 0) {
                    throw new QuizException("This quiz cannot be edited (it is pending, approved or not yours).");
                }
                try (PreparedStatement del = c.prepareStatement("DELETE FROM questions WHERE quiz_id=?")) {
                    del.setInt(1, quiz.getId());
                    del.executeUpdate();
                }
                insertQuestions(c, quiz.getId(), quiz.getQuestions());
                c.commit();
            } catch (SQLException | QuizException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void insertQuestions(Connection c, int quizId, List<Question> questions) throws SQLException {
        String sql = "INSERT INTO questions(quiz_id, question_text, option_a, option_b, option_c, option_d, "
                   + "correct_option, explanation) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (Question q : questions) {
                p.setInt(1, quizId);
                p.setString(2, q.getQuestionText());
                p.setString(3, q.getOptionA());
                p.setString(4, q.getOptionB());
                p.setString(5, q.getOptionC());
                p.setString(6, q.getOptionD());
                p.setString(7, q.getCorrectOption());
                p.setString(8, q.getExplanation());
                p.addBatch();
            }
            p.executeBatch();
        }
    }

    // ------------------------------------------------------------------ workflow

    /** Creator sends a DRAFT/REJECTED quiz to the admin for approval. */
    public void submitForApproval(int quizId, int creatorId) throws SQLException, QuizException {
        String sql = "UPDATE quizzes SET status='PENDING', review_note=NULL WHERE id=? AND creator_id=? "
                   + "AND status IN ('DRAFT','REJECTED') AND EXISTS (SELECT 1 FROM questions WHERE quiz_id = ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, quizId);
            p.setInt(2, creatorId);
            p.setInt(3, quizId);
            if (p.executeUpdate() == 0) {
                throw new QuizException("Only draft or rejected quizzes with at least one question can be submitted.");
            }
        }
    }

    /** Admin decision. Only PENDING quizzes can be approved or rejected. */
    public void review(int quizId, QuizStatus decision, String note) throws SQLException, QuizException {
        if (decision != QuizStatus.APPROVED && decision != QuizStatus.REJECTED) {
            throw new QuizException("Invalid review decision.");
        }
        String sql = "UPDATE quizzes SET status=?, review_note=? WHERE id=? AND status='PENDING'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, decision.name());
            p.setString(2, note == null || note.isBlank() ? null : note);
            p.setInt(3, quizId);
            if (p.executeUpdate() == 0) {
                throw new QuizException("That quiz is not waiting for approval any more.");
            }
        }
    }

    // ------------------------------------------------------------------ queries

    @Override
    public Quiz<Question> findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(SELECT_BASE + "WHERE q.id = ?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    @Override
    public List<Quiz<Question>> findAll() throws SQLException {
        return query(SELECT_BASE + "ORDER BY q.created_at DESC, q.id DESC", 0);
    }

    public List<Quiz<Question>> findByCreator(int creatorId) throws SQLException {
        return query(SELECT_BASE + "WHERE q.creator_id = ? ORDER BY q.created_at DESC, q.id DESC", creatorId);
    }

    public List<Quiz<Question>> findByStatus(QuizStatus status) throws SQLException {
        List<Quiz<Question>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(SELECT_BASE + "WHERE q.status = ? ORDER BY q.created_at DESC")) {
            p.setString(1, status.name());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    /** Approved quizzes with how many attempts this participant has used and their best score. */
    public List<Quiz<Question>> findApprovedFor(int participantId) throws SQLException {
        String sql = "SELECT t.*, "
                + " (SELECT COUNT(*) FROM quiz_attempts a WHERE a.quiz_id = t.id AND a.participant_id = ?) AS used, "
                + " (SELECT COALESCE(MAX(a.score), -1) FROM quiz_attempts a WHERE a.quiz_id = t.id "
                + "   AND a.participant_id = ? AND a.submitted_at IS NOT NULL) AS best "
                + "FROM (" + SELECT_BASE + "WHERE q.status = 'APPROVED') t ORDER BY t.created_at DESC, t.id DESC";
        List<Quiz<Question>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, participantId);
            p.setInt(2, participantId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Quiz<Question> q = map(r);
                    q.setAttemptsUsed(r.getInt("used"));
                    q.setBestScore(r.getInt("best"));
                    list.add(q);
                }
            }
        }
        return list;
    }

    public List<Question> getQuestions(int quizId) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM questions WHERE quiz_id = ? ORDER BY id")) {
            p.setInt(1, quizId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(new Question(r.getInt("id"), quizId, r.getString("question_text"),
                            r.getString("option_a"), r.getString("option_b"), r.getString("option_c"),
                            r.getString("option_d"), r.getString("correct_option"), r.getString("explanation")));
                }
            }
        }
        return list;
    }

    public Map<QuizStatus, Integer> countByStatus() throws SQLException {
        Map<QuizStatus, Integer> counts = new EnumMap<>(QuizStatus.class);
        for (QuizStatus s : QuizStatus.values()) {
            counts.put(s, 0);
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT status, COUNT(*) FROM quizzes GROUP BY status");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                counts.put(QuizStatus.valueOf(r.getString(1)), r.getInt(2));
            }
        }
        return counts;
    }

    // ------------------------------------------------------------------ delete

    /** Admin delete (any status). */
    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM quizzes WHERE id = ?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    /** Creator delete: only own quizzes that are still draft or rejected. */
    public boolean deleteOwned(int id, int creatorId) throws SQLException {
        String sql = "DELETE FROM quizzes WHERE id = ? AND creator_id = ? AND status IN ('DRAFT','REJECTED')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.setInt(2, creatorId);
            return p.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------ helpers

    private List<Quiz<Question>> query(String sql, int param) throws SQLException {
        List<Quiz<Question>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            if (param > 0) {
                p.setInt(1, param);
            }
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    private Quiz<Question> map(ResultSet r) throws SQLException {
        Quiz<Question> q = new Quiz<>();
        q.setId(r.getInt("id"));
        q.setTitle(r.getString("title"));
        q.setDescription(r.getString("description"));
        q.setDurationMinutes(r.getInt("duration_minutes"));
        q.setCreatorId(r.getInt("creator_id"));
        q.setCreatorName(r.getString("creator_name"));
        q.setStatus(QuizStatus.valueOf(r.getString("status")));
        q.setReviewNote(r.getString("review_note"));
        q.setCreatedAt(r.getTimestamp("created_at"));
        q.setQuestionCount(r.getInt("q_count"));
        q.setAttemptCount(r.getInt("a_count"));
        q.setAvgPercent(r.getDouble("avg_pct"));
        return q;
    }
}
