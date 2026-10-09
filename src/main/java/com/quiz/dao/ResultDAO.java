package com.quiz.dao;

import com.quiz.exception.QuizException;
import com.quiz.model.AnswerDetail;
import com.quiz.model.LeaderboardEntry;
import com.quiz.model.Question;
import com.quiz.model.QuizResult;
import com.quiz.util.AppSettings;
import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Database access for quiz attempts, answers, grading and the leaderboard. */
public class ResultDAO {

    /**
     * Attempt + quiz title/duration + participant and grader names.
     * elapsed = time used so far (open attempt) or time taken (submitted attempt),
     * calculated by the database so timer maths never depends on the Java clock.
     */
    private static final String SELECT_ATTEMPT =
            "SELECT a.*, q.title AS quiz_title, q.duration_minutes, u.name AS participant_name, g.name AS grader_name, "
          + " IF(a.submitted_at IS NULL, TIMESTAMPDIFF(SECOND, a.started_at, NOW()), "
          + "    TIMESTAMPDIFF(SECOND, a.started_at, a.submitted_at)) AS elapsed "
          + "FROM quiz_attempts a "
          + "JOIN quizzes q ON q.id = a.quiz_id "
          + "JOIN users u ON u.id = a.participant_id "
          + "LEFT JOIN users g ON g.id = a.graded_by ";

    // ------------------------------------------------------------------ taking a quiz

    public int startAttempt(int quizId, int participantId, int totalQuestions) throws SQLException {
        String sql = "INSERT INTO quiz_attempts(quiz_id, participant_id, total_questions) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, quizId);
            p.setInt(2, participantId);
            p.setInt(3, totalQuestions);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public QuizResult findById(int attemptId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(SELECT_ATTEMPT + "WHERE a.id = ?")) {
            p.setInt(1, attemptId);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** The unfinished attempt of this participant for this quiz, if any (lets a page refresh resume). */
    public QuizResult findOpenAttempt(int quizId, int participantId) throws SQLException {
        String sql = SELECT_ATTEMPT + "WHERE a.quiz_id = ? AND a.participant_id = ? AND a.submitted_at IS NULL "
                   + "ORDER BY a.id DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, quizId);
            p.setInt(2, participantId);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public int countAttempts(int quizId, int participantId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id=? AND participant_id=?")) {
            p.setInt(1, quizId);
            p.setInt(2, participantId);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /**
     * Stores the answers and closes the attempt in one transaction.
     * The UPDATE only matches an attempt that is still open, so a double submit
     * (double click, two tabs) can never be graded twice.
     */
    public void saveSubmission(int attemptId, int score, List<AnswerDetail> answers)
            throws SQLException, QuizException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String close = "UPDATE quiz_attempts SET score=?, auto_score=?, submitted_at=NOW() "
                             + "WHERE id=? AND submitted_at IS NULL";
                try (PreparedStatement p = c.prepareStatement(close)) {
                    p.setInt(1, score);
                    p.setInt(2, score);
                    p.setInt(3, attemptId);
                    if (p.executeUpdate() == 0) {
                        throw new QuizException("This attempt was already submitted.");
                    }
                }
                String ins = "INSERT INTO answers(attempt_id, question_id, selected_option, is_correct) VALUES(?,?,?,?)";
                try (PreparedStatement p = c.prepareStatement(ins)) {
                    for (AnswerDetail a : answers) {
                        p.setInt(1, attemptId);
                        p.setInt(2, a.getQuestion().getId());
                        p.setString(3, a.isAnswered() ? a.getSelectedOption() : null);
                        p.setBoolean(4, a.isCorrect());
                        p.addBatch();
                    }
                    p.executeBatch();
                }
                c.commit();
            } catch (SQLException | QuizException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Every question of the attempt's quiz with the option the participant picked (or null). */
    public List<AnswerDetail> getAnswerDetails(int attemptId, int quizId) throws SQLException {
        String sql = "SELECT q.*, a.selected_option FROM questions q "
                   + "LEFT JOIN answers a ON a.question_id = q.id AND a.attempt_id = ? "
                   + "WHERE q.quiz_id = ? ORDER BY q.id";
        List<AnswerDetail> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, attemptId);
            p.setInt(2, quizId);
            try (ResultSet r = p.executeQuery()) {
                int n = 1;
                while (r.next()) {
                    Question q = new Question(r.getInt("id"), quizId, r.getString("question_text"),
                            r.getString("option_a"), r.getString("option_b"), r.getString("option_c"),
                            r.getString("option_d"), r.getString("correct_option"), r.getString("explanation"));
                    list.add(new AnswerDetail(n++, q, r.getString("selected_option")));
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------------ lists

    public List<QuizResult> findByParticipant(int participantId) throws SQLException {
        return query(SELECT_ATTEMPT + "WHERE a.participant_id = ? AND a.submitted_at IS NOT NULL "
                   + "ORDER BY a.submitted_at DESC, a.id DESC", participantId);
    }

    /** Submitted attempts on quizzes owned by this creator. */
    public List<QuizResult> findByCreator(int creatorId) throws SQLException {
        return query(SELECT_ATTEMPT + "WHERE q.creator_id = ? AND a.submitted_at IS NOT NULL "
                   + "ORDER BY a.submitted_at DESC, a.id DESC", creatorId);
    }

    // ------------------------------------------------------------------ grading

    /**
     * Creator review: adjust the score (0..total) and leave feedback.
     * The join on quizzes makes sure a creator can only grade attempts on their own quizzes.
     */
    public boolean grade(int attemptId, int creatorId, int newScore, String feedback) throws SQLException {
        String sql = "UPDATE quiz_attempts a JOIN quizzes q ON q.id = a.quiz_id "
                   + "SET a.score = ?, a.feedback = ?, a.graded_by = ?, a.graded_at = NOW() "
                   + "WHERE a.id = ? AND q.creator_id = ? AND a.submitted_at IS NOT NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, newScore);
            p.setString(2, feedback == null || feedback.isBlank() ? null : feedback);
            p.setInt(3, creatorId);
            p.setInt(4, attemptId);
            p.setInt(5, creatorId);
            return p.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------ leaderboard

    /**
     * Overall ranking: each participant's best attempt per quiz, averaged as a percentage.
     * Ties on the average share a rank (broken visually by total score).
     *
     * @param limit maximum rows, or 0 for everybody
     */
    public List<LeaderboardEntry> leaderboard(int limit) throws SQLException {
        String sql = "SELECT u.id, u.name, COUNT(*) AS quizzes_taken, SUM(b.best) AS total_score, "
                   + "       ROUND(AVG(b.best_pct), 1) AS avg_pct "
                   + "FROM (SELECT participant_id, quiz_id, MAX(score) AS best, "
                   + "             MAX(score * 100 / total_questions) AS best_pct "
                   + "      FROM quiz_attempts WHERE submitted_at IS NOT NULL AND total_questions > 0 "
                   + "      GROUP BY participant_id, quiz_id) b "
                   + "JOIN users u ON u.id = b.participant_id AND u.active = 1 "
                   + "GROUP BY u.id, u.name "
                   + "ORDER BY avg_pct DESC, total_score DESC, u.name ASC"
                   + (limit > 0 ? " LIMIT " + limit : "");
        List<LeaderboardEntry> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            int position = 0;
            int rank = 0;
            double previous = Double.NaN;
            int previousScore = -1;
            while (r.next()) {
                position++;
                LeaderboardEntry e = new LeaderboardEntry();
                e.setUserId(r.getInt("id"));
                e.setName(r.getString("name"));
                e.setQuizzesTaken(r.getInt("quizzes_taken"));
                e.setTotalScore(r.getInt("total_score"));
                e.setAvgPercent(r.getDouble("avg_pct"));
                if (e.getAvgPercent() != previous || e.getTotalScore() != previousScore) {
                    rank = position;
                }
                previous = e.getAvgPercent();
                previousScore = e.getTotalScore();
                e.setRank(rank);
                list.add(e);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------ helpers

    private List<QuizResult> query(String sql, int param) throws SQLException {
        List<QuizResult> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, param);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    private QuizResult map(ResultSet r) throws SQLException {
        QuizResult x = new QuizResult();
        x.setAttemptId(r.getInt("id"));
        x.setQuizId(r.getInt("quiz_id"));
        x.setParticipantId(r.getInt("participant_id"));
        x.setScore(r.getInt("score"));
        x.setAutoScore(r.getInt("auto_score"));
        x.setTotal(r.getInt("total_questions"));
        x.setQuizTitle(r.getString("quiz_title"));
        x.setParticipantName(r.getString("participant_name"));
        x.setDurationMinutes(r.getInt("duration_minutes"));
        x.setStartedAt(r.getTimestamp("started_at"));
        x.setSubmittedAt(r.getTimestamp("submitted_at"));
        x.setFeedback(r.getString("feedback"));
        x.setGradedByName(r.getString("grader_name"));
        x.setElapsedSeconds(r.getInt("elapsed"));
        x.setPassPercent(AppSettings.getInt("pass_percentage"));
        return x;
    }
}
