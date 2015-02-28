package com.epam.wilma.browsermob.transformer;
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

import java.util.ArrayList;
import java.util.List;

import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.proxy.http.BrowserMobHttpResponse;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.epam.wilma.browsermob.transformer.helper.WilmaResponseFactory;
import com.epam.wilma.domain.http.WilmaHttpResponse;

/**
 * Provides unit tests for the <tt>HttpResponseTransformer</tt> class.
 * @author Tunde_Kovacs
 *
 */
public class HttpResponseTransformerTest {

    private static final String RESPONSE_BODY = "response";
    private static final String CONTENT_TYPE = "application/xml";
    private Header[] responseHeaders;
    private List<HarNameValuePair> requestHeaders;

    @Mock
    private WilmaHttpResponse response;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BrowserMobHttpResponse browserMobHttpResponse;
    @Mock
    private WilmaResponseFactory responseFactory;

    @InjectMocks
    private HttpResponseTransformer underTest;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        given(responseFactory.createNewWilmaHttpResponse()).willReturn(response);
        responseHeaders = new Header[1];
        requestHeaders = new ArrayList<>();
        responseHeaders[0] = new BasicHeader("respName", "respValue");
        requestHeaders.add(new HarNameValuePair("reqName", "reqValue"));
    }

    @Test
    public void testTransformShouldSetRequestHeader() {
        //GIVEN
        given(browserMobHttpResponse.getRawResponse().getAllHeaders()).willReturn(responseHeaders);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response).addRequestHeader("reqName", "reqValue");
    }

    @Test
    public void testTransformShouldSetResponseHeader() {
        given(browserMobHttpResponse.getRawResponse().getAllHeaders()).willReturn(responseHeaders);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response).addHeader("respName", "respValue");
    }

    @Test
    public void testTransformShouldNotSetResponseHeaderWhenRawResponseIsNull() {
        given(browserMobHttpResponse.getRawResponse()).willReturn(null);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response, never()).addHeader("respName", "respValue");
    }

    @Test
    public void testTransformShouldSetBody() {
        given(browserMobHttpResponse.getRawResponse().getAllHeaders()).willReturn(responseHeaders);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        given(browserMobHttpResponse.getEntry().getResponse().getContent().getText()).willReturn(RESPONSE_BODY);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response).setBody(RESPONSE_BODY);
    }

    @Test
    public void testTransformShouldSetContentType() {
        given(browserMobHttpResponse.getRawResponse().getAllHeaders()).willReturn(responseHeaders);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        given(browserMobHttpResponse.getContentType()).willReturn(CONTENT_TYPE);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response).setContentType(CONTENT_TYPE);
    }

    @Test
    public void testTransformShouldSetStatusCode() {
        given(browserMobHttpResponse.getRawResponse().getAllHeaders()).willReturn(responseHeaders);
        given(browserMobHttpResponse.getEntry().getRequest().getHeaders()).willReturn(requestHeaders);
        given(browserMobHttpResponse.getEntry().getResponse().getStatus()).willReturn(200);
        //WHEN
        underTest.transformResponse(browserMobHttpResponse);
        //THEN
        verify(response).setStatusCode(200);
    }
}
