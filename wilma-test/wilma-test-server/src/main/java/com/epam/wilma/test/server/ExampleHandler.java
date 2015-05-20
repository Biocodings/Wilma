package com.epam.wilma.test.server;
/*==========================================================================
Copyright 2015 EPAM Systems

This file is part of Wilma.

Wilma is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wilma is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wilma.  If not, see <http://www.gnu.org/licenses/>.
===========================================================================*/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.epam.wilma.test.server.compress.Decompressor;
import com.epam.wilma.test.server.compress.fis.FastInfosetDecompressor;
import com.epam.wilma.test.server.compress.gzip.GzipCompressor;
import com.epam.wilma.test.server.compress.gzip.GzipDecompressor;

/**
 * Jetty handler that is able to handle requests coming to /example with one of the given XMLs in the request body.
 * @author Marton_Sereg
 *
 */
public class ExampleHandler extends AbstractHandler {

    private static final String FIS_RESPONSE = "example.xml.fis";
    private static final String EXAMPLE_XML = "example.xml";

    private static final String PATH_NOT_IMPLEMENTED = "/sendnotimplemented";
    private static final String PATH_BAD_GATWAY = "/sendbadgateway";
    private static final String PATH_SERVICE_UNAVAILABLE = "/sendserviceunavailable";
    private static final String PATH_INTERNAL_SERVER_ERROR = "/sendinternalservererror";
    private static final int WAIT_IN_MILLIS = 61000;
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE = "Content-type";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP_TYPE = "gzip";
    private static final String FASTINFOSET_TYPE = "application/fastinfoset";
    private static final String XML_TYPE = "application/xml";
    private static final String ANY_TYPE = "*/*";

    private static final String PATH_TO_HANDLE = "/example";
    private static final String PATH_TO_TIMEOUT = "/sendtimeout";

    private final InputStreamConverter inputStreamConverter;

    private final Decompressor fisDecompressor = new FastInfosetDecompressor();

    private final GzipDecompressor gzipDecompresser = new GzipDecompressor();

    private final GzipCompressor gzipCompressor = new GzipCompressor();

    /**
     * Constructor with InputStreamConverter.
     * @param inputStreamConverter to inject
     */
    public ExampleHandler(final InputStreamConverter inputStreamConverter) {
        this.inputStreamConverter = inputStreamConverter;
    }

    @Override
    public void handle(final String path, final Request baseRequest, final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) throws IOException, ServletException {
        if (PATH_TO_HANDLE.equals(path)) {
            String requestBody = "";
            byte[] byteArray = null;
            String contentEncodingHeader = httpServletRequest.getHeader(CONTENT_ENCODING);
            if (contentEncodingHeader != null && contentEncodingHeader.contains(GZIP_TYPE)) {
                byteArray = gzipDecompresser.decompress(httpServletRequest.getInputStream()).toByteArray();
                requestBody = IOUtils.toString(byteArray, "utf-8");
            }
            String headerField = httpServletRequest.getHeader(CONTENT_TYPE);
            if (headerField != null && headerField.contains(FASTINFOSET_TYPE)) {
                if (byteArray != null) {
                    requestBody = fisDecompressor.decompress(new ByteArrayInputStream(byteArray));
                } else {
                    requestBody = fisDecompressor.decompress(httpServletRequest.getInputStream());
                }
            } else if (headerField != null && headerField.contains(XML_TYPE)) {
                if (byteArray == null) {
                    requestBody = inputStreamConverter.getStringFromStream(httpServletRequest.getInputStream());
                }
            }
            setAnswer(baseRequest, httpServletRequest, httpServletResponse, requestBody);
        } else {
            generateErrorCode(path, httpServletResponse, baseRequest);

        }
    }

    private void generateErrorCode(final String path, final HttpServletResponse httpServletResponse, final Request baseRequest)
        throws ServletException, IOException {
        if (PATH_TO_TIMEOUT.equals(path)) {
            try {
                Thread.sleep(WAIT_IN_MILLIS);
            } catch (Exception e) {
                throw new ServletException("Thread's wait failed....", e);
            }
        } else if (PATH_INTERNAL_SERVER_ERROR.equals(path)) {
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else if (PATH_SERVICE_UNAVAILABLE.equals(path)) {
            httpServletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else if (PATH_BAD_GATWAY.equals(path)) {
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        } else if (PATH_NOT_IMPLEMENTED.equals(path)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }

    }

    private void setAnswer(final Request baseRequest, final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
            final String requestBody) throws IOException {
        if (answerShouldBeSent(requestBody)) {
            byte[] responseBodyAsBytes = null;
            if (httpServletRequest.getHeader(ACCEPT_HEADER) == null) {
                httpServletResponse.getWriter().println("Missing accept header!");
            } else if (httpServletRequest.getHeader(ACCEPT_HEADER).contains(XML_TYPE) || httpServletRequest.getHeader(ACCEPT_HEADER).contains(ANY_TYPE)) {
                InputStream xml = getXmlFromFile(EXAMPLE_XML);
                httpServletResponse.setContentType("application/xml;charset=UTF-8");
                responseBodyAsBytes = (inputStreamConverter.getStringFromStream(xml)).getBytes();
            } else if (httpServletRequest.getHeader(ACCEPT_HEADER).contains(FASTINFOSET_TYPE)) {
                InputStream xml = getXmlFromFile(FIS_RESPONSE);
                httpServletResponse.setContentType("application/fastinfoset");
                httpServletResponse.setCharacterEncoding("UTF-8");
                responseBodyAsBytes = IOUtils.toByteArray(xml, xml.available());
            }
            //Encodes response body with gzip if client accepts gzip encoding
            if (httpServletRequest.getHeader(ACCEPT_ENCODING) != null && httpServletRequest.getHeader(ACCEPT_ENCODING).contains(GZIP_TYPE)) {
                ByteArrayOutputStream gzipped = gzipCompressor.compress(new ByteArrayInputStream(responseBodyAsBytes));
                responseBodyAsBytes = gzipped.toByteArray();
                httpServletResponse.addHeader(CONTENT_ENCODING, GZIP_TYPE);
            }
            httpServletResponse.getOutputStream().write(responseBodyAsBytes);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    private boolean answerShouldBeSent(final String requestBody) {
        return requestBody.contains("exampleID=\"123\"") || requestBody.contains("exampleID=\"456\"")
                || requestBody.contains("<exampleID2>101</exampleID2>");
    }

    InputStream getXmlFromFile(final String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }
}
