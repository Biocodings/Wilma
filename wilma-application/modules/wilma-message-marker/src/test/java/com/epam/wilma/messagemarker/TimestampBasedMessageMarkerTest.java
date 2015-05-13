package com.epam.wilma.messagemarker;
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
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.epam.wilma.domain.exception.TooManyRequestsException;
import com.epam.wilma.domain.http.WilmaHttpRequest;
import com.epam.wilma.messagemarker.idgenerator.IdGenerator;

/**
 * Test class for {@link TimestampBasedMessageMarker}.
 * @author Marton_Sereg
 */
public class TimestampBasedMessageMarkerTest {

    @InjectMocks
    private TimestampBasedMessageMarker underTest;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private WilmaHttpRequest request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMarkMessageHeaderShouldGetIdentifierAndMarkRequest() throws TooManyRequestsException {
        // GIVEN
        given(idGenerator.nextIdentifier()).willReturn("1234");
        // WHEN
        underTest.markMessageHeader(request);
        // THEN
        verify(idGenerator).nextIdentifier();
        verify(request).addWilmaLoggerId("1234");
    }

}
