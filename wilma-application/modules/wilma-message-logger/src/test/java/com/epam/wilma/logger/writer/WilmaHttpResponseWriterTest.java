package com.epam.wilma.logger.writer;
/*==========================================================================
Copyright 2013-2015 EPAM Systems

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

import com.epam.wilma.domain.http.WilmaHttpResponse;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link WilmaHttpResponseWriter}.
 * @author Tunde_Kovacs
 *
 */
public class WilmaHttpResponseWriterTest {

    private static final String COULD_NOT_WRITE_MESSAGE_ERROR = "Could not write message to file:src/test/resources/outputFile.txt!";
    private static final String OUTPUT_FILE = "src/test/resources/outputFile.txt";
    private static final String RESPONSE_TYPE = "resp";
    private static final String HEADERS = "headers";
    private static final String BODY = "body";
    private static final String MESSAGE_ID = "201306271455.0001";
    private static final int OUTPUT_BUFFER_SIZE = 262144;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WilmaHttpResponse response;
    @Mock
    private BufferedWriter bufferedWriter;
    @Mock
    private BufferedWriterFactory bufferedWriterFactory;
    @Mock
    private DirectoryFactory directoryFactory;
    @Mock
    private Logger logger;

    @InjectMocks
    private WilmaHttpResponseWriter underTest;

    @BeforeMethod
    public void setUp() {
        underTest = spy(new WilmaHttpResponseWriter());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWriteShouldContentToWriter() throws IOException {
        //GIVEN
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willReturn(bufferedWriter);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        given(response.getHeaders().toString()).willReturn(HEADERS);
        given(response.getBody()).willReturn(BODY);
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(bufferedWriter).append(BODY);
    }

    @Test
    public void testWriteShouldNotAppendBodyWhenItIsNull() throws IOException {
        //GIVEN
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willReturn(bufferedWriter);
        given(response.getBody()).willReturn(null);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        given(response.getHeaders().toString()).willReturn(HEADERS);
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(bufferedWriter, never()).append(null);
    }

    @Test
    public void testWriteWhenBufferedWriterThrowsIOExceptionShouldLogError() throws IOException {
        //GIVEN
        Whitebox.setInternalState(underTest, "logger", logger);
        IOException e = new IOException();
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willThrow(e);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(logger).error(COULD_NOT_WRITE_MESSAGE_ERROR, e);
    }

    @Test
    public void testWriteWhenCannotCloseFileShouldLogError() throws IOException {
        //GIVEN
        Whitebox.setInternalState(underTest, "logger", logger);
        IOException e = new IOException();
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willReturn(bufferedWriter);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        willThrow(e).given(bufferedWriter).close();
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(logger).error(COULD_NOT_WRITE_MESSAGE_ERROR, e);
    }

    @Test
    public void testWriteWhenWriterIsNullShouldDoNothing() throws IOException {
        //GIVEN
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willReturn(null);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(bufferedWriter, never()).append(Mockito.anyString());
    }

    @Test
    public void testWriteShouldAppendStatusCodeToWriter() throws IOException {
        //GIVEN
        doReturn(OUTPUT_FILE).when(underTest).getOutputFileName(RESPONSE_TYPE, MESSAGE_ID, true);
        given(bufferedWriterFactory.createBufferedWriter(OUTPUT_FILE, OUTPUT_BUFFER_SIZE)).willReturn(bufferedWriter);
        given(response.getWilmaMessageId()).willReturn(MESSAGE_ID);
        given(response.getStatusCode()).willReturn(200);
        //WHEN
        underTest.write(response, true);
        //THEN
        verify(bufferedWriter).append("Status code:200");
    }

}
