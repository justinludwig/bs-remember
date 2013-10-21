package com.pitchstone.plugin.rememberme

import java.io.IOException
import java.io.PrintWriter
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * HttpServletResponseWrapper implementation that writes the rememberme cookie
 * right before content/redirects/errors are written to the response.
 */
class ResponseWrapper extends HttpServletResponseWrapper {

    Closure writeCookie

    ResponseWrapper(HttpServletResponse rs, Closure writeCookie) {
        super(rs)
        this.writeCookie = writeCookie
    }
    
    ServletOutputStream getOutputStream() throws IOException {
        writeCookie()
        super.outputStream
    }

    PrintWriter getWriter() throws IOException {
        writeCookie()
        super.writer
    }

    void sendError(int code) throws IOException {
        writeCookie()
        super.sendError code
    }

    void sendError(int code, String msg) throws IOException {
        writeCookie()
        super.sendError code, msg
    }

    void sendRedirect(String location) throws IOException {
        writeCookie()
        super.sendRedirect location
    }

    void setStatus(int code) {
        writeCookie()
        super.setStatus code
    }

    void setStatus(int code, String msg) {
        writeCookie()
        super.setStatus code, msg
    }

}
