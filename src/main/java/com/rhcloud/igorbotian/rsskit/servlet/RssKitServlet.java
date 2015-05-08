package com.rhcloud.igorbotian.rsskit.servlet;

import com.rhcloud.igorbotian.rsskit.utils.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public abstract class RssKitServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(RssKitServlet.class);

    @Override
    public void init() throws ServletException {
        if(!Configuration.isSuccessfullyLoaded()) {
            LOGGER.fatal("Configuration file is not found!");
            throw new ServletException(Configuration.FILE + " configuration file is not found!");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            LOGGER.error("Failed to process request", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            LOGGER.error("Failed to process request", e);
        }
    }

    protected void respond(byte[] data, String contentType, Charset encoding, HttpServletResponse response)
            throws IOException {

        assert data != null;
        assert contentType != null;
        assert encoding != null;
        assert response != null;

        sendHeader(data, contentType, encoding, response);
        sendBody(data, response);
    }

    private void sendHeader(byte[] data, String contentType, Charset encoding, HttpServletResponse response) throws IOException {
        assert data != null;
        assert contentType != null;
        assert response != null;

        response.setStatus(200); // OK
        response.setCharacterEncoding(encoding != null ? encoding.name().toLowerCase() : null);
        response.setContentType(contentType);
        response.setContentLength(data.length);
    }

    private void sendBody(byte[] data, HttpServletResponse response) throws IOException {
        assert response != null;

        try (InputStream is = new ByteArrayInputStream(data)) {
            try (OutputStream os = response.getOutputStream()) {
                IOUtils.copy(is, os);
            }
        }
    }

    protected abstract void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;
}
