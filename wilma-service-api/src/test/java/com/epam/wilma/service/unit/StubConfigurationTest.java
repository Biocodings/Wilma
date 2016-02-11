package com.epam.wilma.service.unit;
/*==========================================================================
 Copyright 2013-2016 EPAM Systems

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

import com.epam.wilma.service.unit.helper.common.ConfigurationParameter;
import com.epam.wilma.service.unit.helper.common.StubConfigurationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for StubConfiguration class.
 *
 * @author Tamas_Kohegyi
 */
public class StubConfigurationTest {

    private StubConfiguration stubConfiguration;

    @Test
    public void testCreateStubMinimal() throws StubConfigurationException {
        //given
        stubConfiguration = new StubConfigurationBuilder("myGroup")
                .forRequestsLike().comingFrom("localhost")
                .willRespondWith().plainTextResponse("blah")
                .build();
        //when
        stubConfiguration.validateConfiguration();
        //then
        //if we are here, we are fine
    }

    @Test
    public void testCreateStubExtremelyComplex() throws StubConfigurationException {
        //given, when and then
        ConfigurationParameter[] formatterParameters = new ConfigurationParameter[1];
        formatterParameters[0] = new ConfigurationParameter("fromtext", "totext");
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike()
                .notStart()
                .orStart()
                .andStart().withTextInHeader("blah").withHeader("blah2", "blah2").condition("AlwaysTrueChecker").andEnd()
                .comingFrom("localhost")
                .comingFrom("192.168.0.1")
                .withTextInBody("somethingInBody")
                .negatedCondition("AlwaysFalseChecker")
                .orEnd()
                .notEnd()
                .willRespondWith().plainTextResponse("{ \"ERROR\":\"fromtext\" }").withStatus(404).withDelay(1000)
                .applyFormatter("StringReplaceTemplateFormatter", formatterParameters).applyFormatter("JsonTemplateFormatter")
                .build();
    }

    @Test
    public void testCreateStubGeneratedResponse() throws StubConfigurationException {
        //given, when and then
        ConfigurationParameter[] configurationParameters = new ConfigurationParameter[1];
        configurationParameters[0] = new ConfigurationParameter("Content-Type", "text/plain");
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().condition("HeaderParameterChecker", configurationParameters)
                .willRespondWith().generatedResponse("dummy.class").withMimeType("application/xml")
                .build();
    }

    @Test
    public void testCreateStubGeneratedResponseConditionNegated() throws StubConfigurationException {
        //given, when and then
        ConfigurationParameter[] conditionParameters = new ConfigurationParameter[1];
        conditionParameters[0] = new ConfigurationParameter("Content-Type", "text/plain");
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().negatedCondition("HeaderParameterChecker", conditionParameters)
                .willRespondWith().generatedResponse("dummy.class")
                .build();
    }

    @Test
    public void testCreateStubOrPatternCheckerWithBasicParameter() throws StubConfigurationException {
        //given, when and then
        ConfigurationParameter[] conditionParameters = new ConfigurationParameter[1];
        conditionParameters[0] = new ConfigurationParameter("soemwhereinheader");
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().condition("OrPatternChecker", conditionParameters)
                .willRespondWith().generatedResponse("dummy.class")
                .build();
    }

    @Test
    public void testCreateStubSimpleMock() throws StubConfigurationException {
        //given, when and then
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .willRespondWith().plainTextResponse("body").withStatus(200)
                .build();
    }

    @Test
    public void testCreateStubSimpleMockFromHtmlFile() throws StubConfigurationException {
        //given, when
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .willRespondWith().htmlFileResponse("filename").withStatus(200)
                .build();
        //then
        Assert.assertTrue(stubConfiguration.toString().contains("text/html"), "Bad mime type was set.");
    }

    @Test
    public void testCreateStubSimpleMockFromTextFile() throws StubConfigurationException {
        //given, when
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .willRespondWith().textFileResponse("filename").withStatus(200)
                .build();
        //then
        Assert.assertTrue(stubConfiguration.toString().contains("text/plain"), "Bad mime type was set.");
    }

    @Test
    public void testCreateStubSimpleMockFromJsonFile() throws StubConfigurationException {
        //given, when
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .willRespondWith().jsonFileResponse("filename").withStatus(200)
                .build();
        //then
        Assert.assertTrue(stubConfiguration.toString().contains("application/json"), "Bad mime type was set.");
    }

    @Test
    public void testCreateStubSimpleMockFromXmlFile() throws StubConfigurationException {
        //given, when
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .willRespondWith().xmlFileResponse("filename").withStatus(200)
                .build();
        //then
        Assert.assertTrue(stubConfiguration.toString().contains("application/xml"), "Bad mime type was set.");
    }

    @Test(expectedExceptions = {StubConfigurationException.class})
    public void testInvalidConfiguration() throws StubConfigurationException {
        //given, when and then
        stubConfiguration = new StubConfigurationBuilder()
                .forRequestsLike().textInUrl("/blah")
                .andStart()
                .willRespondWith().plainTextResponse("body").withStatus(200)
                .build();
        //then
        Assert.fail("We should not reach this.");
    }
}
