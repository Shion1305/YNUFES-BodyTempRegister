package com.shion1305.ynufes.bodytemp2022;

import org.junit.Test;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

public class UserHandlerTest {
    @Test
    public void check() throws IOException {
        GasResponse response = UserHandler.connector.check("市川詩恩");
        System.out.println(response.currentValue.equals(""));
        System.out.println(response);
    }

    @Test
    public void checkNoSubmission() throws BackingStoreException, IOException {
        UserHandler.checkNoSubmission();
    }
}
