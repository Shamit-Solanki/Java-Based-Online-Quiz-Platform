package com.quiz.filter;

import com.quiz.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * First line of defence for every request:
 * <ul>
 *   <li>lets public pages and static files through;</li>
 *   <li>redirects anonymous users to the login page;</li>
 *   <li>validates a per-session CSRF token on every POST;</li>
 *   <li>stops browsers caching private pages (back button after logout).</li>
 * </ul>
 */
public class AuthFilter implements Filter {

    public static final String CSRF_ATTRIBUTE = "csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        String path = pathOf(req);
        if (isStatic(path)) {
            chain.doFilter(request, response);
            return;
        }

        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user != null) {
            if (session.getAttribute(CSRF_ATTRIBUTE) == null) {
                session.setAttribute(CSRF_ATTRIBUTE, newToken());
            }
            if ("POST".equalsIgnoreCase(req.getMethod()) && !validCsrf(req, session)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the page and try again.");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private static String pathOf(HttpServletRequest req) {
        String path = req.getServletPath() == null ? "" : req.getServletPath();
        if (req.getPathInfo() != null) {
            path += req.getPathInfo();
        }
        return path.isEmpty() ? "/" : path;
    }

    private static boolean isStatic(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/")
                || path.equals("/favicon.ico");
    }

    private static boolean isPublic(String path) {
        return path.equals("/") || path.equals("/login") || path.equals("/register") || path.equals("/index.jsp");
    }

    private static boolean validCsrf(HttpServletRequest req, HttpSession session) {
        String expected = (String) session.getAttribute(CSRF_ATTRIBUTE);
        String sent = req.getParameter("_csrf");
        if (sent == null) {
            sent = req.getHeader("X-CSRF-Token");
        }
        return expected != null && sent != null
                && MessageDigest.isEqual(expected.getBytes(), sent.getBytes());
    }

    public static String newToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
