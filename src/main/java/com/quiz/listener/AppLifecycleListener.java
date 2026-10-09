package com.quiz.listener;

import com.quiz.service.ReminderService;
import com.quiz.util.AppSettings;
import com.quiz.util.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts a background thread that turns due quiz reminders into notifications
 * every 30 seconds, and cleans everything up when the application stops.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppLifecycleListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        AppSettings.reload();
        ThreadFactory factory = task -> {
            Thread t = new Thread(task, "reminder-scheduler");
            t.setDaemon(true);
            return t;
        };
        scheduler = Executors.newSingleThreadScheduledExecutor(factory);
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int sent = ReminderService.dispatchDue();
                if (sent > 0) {
                    LOG.info("Dispatched " + sent + " quiz reminder(s)");
                }
            } catch (Exception e) {
                // never let an exception cancel the periodic task
                LOG.log(Level.WARNING, "Reminder dispatch failed", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        DBConnection.shutdown();
    }
}
