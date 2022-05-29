package com.shion1305.ynufes.bodytemp2022;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GASConnectionTest {
    @Test
    public void gasTest() throws IOException {
        GASConnector connector = new GASConnector("https://script.google.com/macros/s/AKfycbyAVRwwhLBqQ0vTWjA3gmr95XksEQAlVFwF7RxzmK32FmYlPSH8XmyGMZKh1bmeect0Qg/exec");
        Assert.assertTrue(connector.check("市川詩恩"));
        Assert.assertEquals(connector.register("市川詩恩", "36.3"), 202);
    }

}
