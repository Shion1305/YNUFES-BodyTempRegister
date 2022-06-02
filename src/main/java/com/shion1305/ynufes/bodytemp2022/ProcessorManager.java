/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.model.event.Event;
import com.shion1305.ynufes.bodytemp2022.config.InstanceData;
import com.shion1305.ynufes.bodytemp2022.config.JsonConfigManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

@WebListener
public class ProcessorManager implements ServletContextListener {
    static Logger logger = Logger.getLogger("ProcessManager");
    static HashMap<String, RequestProcessor> processors = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            init();
        } catch (IOException e) {
            logger.info("Error on initiating ProcessManager");
        }
    }

    private void init() throws IOException {
        InstanceData[] data = JsonConfigManager.readJson();
        if (data == null) {
            logger.info("Json Configuration file loaded, but no profile found.");
            return;
        }
        for (InstanceData d : data) {
            processors.put(d.processName, new RequestProcessor(d));
            logger.info(String.format("[%s]Process Registered", d.processName));
        }
    }

    public static void processEvent(String reqName, Event e) throws BackingStoreException, IOException {
        RequestProcessor processor = processors.get(reqName);
        if (processor != null) processor.processEvent(e);
        else logger.info("Received unknown event: reqName: " + reqName);
    }

    public static void broadcastReminder() {
        for (var processor : processors.values()) {
            processor.broadcastReminder();
        }
    }

    public static void checkNoSubmission() throws BackingStoreException, IOException {
        for (var processor : processors.values()) {
            processor.checkNoSubmission();
        }
    }
}
