package com.shion1305.ynufes.bodytemp2022;

import com.shion1305.ynufes.bodytemp2022.message.MessageSender;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@WebListener
public class ReminderSender implements ServletContextListener {

    static Logger logger = Logger.getLogger("TempInputReminder");
    /*
    Schedule at next 07:00
     */
    static Timer timer = new Timer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        schedule();
    }

    public static void schedule() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 6) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.SECOND, 0);
        Date d = calendar.getTime();
        logger.info("Scheduled at " + d);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MessageSender.broadcastReminder();
                logger.info("The scheduled reminder has been broadcasted");
                schedule();
            }
        }, d);
    }
}
