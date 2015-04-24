package com.epam.wilma.gepard.test.stubconfig;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.epam.gepard.annotations.TestClass;
import com.epam.gepard.annotations.TestParameter;

import com.epam.wilma.gepard.WilmaTestCase;
import com.epam.wilma.gepard.testclient.RequestParameters;

/**
 * Tests wilma's stub config.
 * @author Tunde_Kovacs
 *
 */
@TestClass(id = "StubConfig", name = "Stub config")
public class StubConfigTest extends WilmaTestCase {

    private String stubConfigXml;

    @TestParameter(id = "PAR0")
    private String tcName;
    @TestParameter(id = "PAR1")
    private String tcContentType;
    @TestParameter(id = "PAR2")
    private String tcAcceptHeader;
    @TestParameter(id = "PAR3")
    private String tcContentEncoding;
    @TestParameter(id = "PAR4")
    private String tcAcceptEncoding;
    @TestParameter(id = "PAR5")
    private String tcStubConfig;
    @TestParameter(id = "PAR6")
    private String tcResponse;

    public void testStubConfigXmlHandling() throws Exception {
        stubConfigXml = tcStubConfig;
        setOriginalRequestMessageForStubConfig(stubConfigXml);
        setExpectedResponseMessageFromFile(tcResponse);
        RequestParameters requestParameters = createRequestParameters();
        callWilmaWithPostMethodAndAssertResponse(requestParameters);
    }

    protected RequestParameters createRequestParameters() throws FileNotFoundException {
        String testServerUrl = getWilmaInternalUrl() + STUB_CONFIG_URL;
        String wilmaHost = getClassData().getEnvironment().getProperty("wilma.host");
        Integer wilmaPort = Integer.parseInt(getClassData().getEnvironment().getProperty("wilma.port.external"));
        String contentType = "application/" + tcContentType;
        String acceptHeader = "application/" + tcAcceptHeader;
        String contentEncoding = tcContentEncoding;
        String acceptEncoding = tcAcceptEncoding;
        return new RequestParameters().testServerUrl(testServerUrl).useProxy(false).wilmaHost(wilmaHost).wilmaPort(wilmaPort)
                .xmlIS(new FileInputStream(stubConfigXml)).contentType(contentType).acceptHeader(acceptHeader).contentEncoding(contentEncoding)
                .acceptEncoding(acceptEncoding);
    }

}
