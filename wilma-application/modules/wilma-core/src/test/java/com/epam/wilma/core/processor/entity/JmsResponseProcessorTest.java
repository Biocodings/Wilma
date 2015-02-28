package com.epam.wilma.core.processor.entity;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.epam.wilma.common.helper.WilmaConstants;
import com.epam.wilma.core.processor.response.jms.JmsResponseBuilder;
import com.epam.wilma.domain.exception.ApplicationException;
import com.epam.wilma.domain.http.WilmaHttpResponse;

/**
 * Provides unit tests for the class {@link JmsResponseProcessor}.
 * @author Tunde_Kovacs
 *
 */
public class JmsResponseProcessorTest {

    @Mock
    private WilmaHttpResponse response;
    @Mock
    private JmsResponseBuilder jmsResponseBuilder;

    @InjectMocks
    private JmsResponseProcessor underTest;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessShouldCallBuildMessageWhenResponseHasWilmaLoggerId() throws ApplicationException {
        //GIVEN
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(WilmaConstants.WILMA_LOGGER_ID.getConstant(), "date.nnnresp");
        given(response.getRequestHeaders()).willReturn(requestHeaders);
        //WHEN
        underTest.process(response);
        //THEN
        verify(jmsResponseBuilder).buildResponse(response);
    }

    @Test
    public void testProcessShouldNotCallBuildMessageWhenResponseDoesNotHaveWilmaLoggerId() throws ApplicationException {
        //GIVEN
        Map<String, String> requestHeaders = new HashMap<>();
        given(response.getRequestHeaders()).willReturn(requestHeaders);
        //WHEN
        underTest.process(response);
        //THEN
        verify(jmsResponseBuilder, never()).buildResponse(response);
    }

}
