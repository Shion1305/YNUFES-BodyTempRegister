/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.model.event.Event;
import com.shion1305.ynufes.bodytemp2022.config.InstanceData;
import com.shion1305.ynufes.bodytemp2022.config.JsonConfigManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

public class ProcessorManager {
    static Logger logger = Logger.getLogger("ProcessManager");
    static HashMap<String, RequestProcessor> processors = new HashMap<>();

    static {
        try {
            init();
        } catch (IOException e) {
            logger.info("Error on initiating ProcessManager");
        }
    }

    private ProcessorManager() {
    }

    private static void init() throws IOException {
        InstanceData[] data = JsonConfigManager.readJson();
        for (InstanceData d : data) {
            processors.put(d.name, new RequestProcessor(d.gasUrl, d.name, d.lineToken));
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
