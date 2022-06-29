/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import com.shion1305.ynufes.bodytemp2022.config.ConfigManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/maintenance/*")
public class ManualServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pass = req.getParameter("pass");
        if (pass == null || !pass.equals(ConfigManager.getConfig(ConfigManager.ConfigProperty.MAINTENANCE_PASS))) {
            resp.setStatus(401);
            resp.getWriter().write("パスワードが間違っています。");
            return;
        }
        String url = req.getPathInfo();
        String action = url.substring(url.lastIndexOf("/") + 1);
        switch (action) {
            case "reload":
                resp.setStatus(200);
                resp.getWriter().write("reloaded!");
                ProcessorManager.reload();
                break;
            default:
                resp.setStatus(400);
                resp.getWriter().write("BAD REQUEST");
        }
    }
}
