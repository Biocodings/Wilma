package com.epam.wilma.extras.shortcircuit;
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

import com.epam.wilma.domain.http.WilmaHttpRequest;
import com.epam.wilma.domain.http.WilmaHttpResponse;
import com.epam.wilma.domain.stubconfig.interceptor.RequestInterceptor;
import com.epam.wilma.domain.stubconfig.interceptor.ResponseInterceptor;
import com.epam.wilma.domain.stubconfig.parameter.ParameterList;
import com.epam.wilma.webapp.service.external.ExternalWilmaService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * Interceptor that implements all three possibilities.
 * Request interceptor marks the message with a hash code, for ShortCircuitChecker, that registers the request, if it is not yet registered.
 * Response interceptor captures the response, if it is not yet captured.
 * ExternalWilmaService offers the possibility of getting the actual status of the internal req-resp map,
 * and offers the possibility of saving the req-resp map into and loading from a folder.
 *
 * @author tkohegyi
 */
public class ShortCircuitInterceptor implements ResponseInterceptor, RequestInterceptor, ExternalWilmaService {

    private static Map<String, ShortCircuitResponseInformation> shortCircuitMap = ShortCircuitChecker.getShortCircuitMap();
    private final Logger logger = LoggerFactory.getLogger(ShortCircuitChecker.class);

    /**
     * This is the Response Interceptor implementation. In case the response is marked with hashcode,
     * that means the response should be preserved.
     *
     * @param wilmaHttpResponse is the response
     * @param parameterList     may contain the response validity timeout - if not response will be valid forever
     */
    @Override
    public void onResponseReceive(WilmaHttpResponse wilmaHttpResponse, ParameterList parameterList) {
        String shortCircuitHashCode = wilmaHttpResponse.getRequestHeader(ShortCircuitChecker.SHORT_CIRCUIT_HEADER);
        if (shortCircuitHashCode != null) { //this was marked, check if it is in the map
            long timeout;
            if (shortCircuitMap.containsKey(shortCircuitHashCode)) { //only if this needs to be handled
                //do we have the response already, or we need to catch it right now?
                ShortCircuitResponseInformation shortCircuitResponseInformation = shortCircuitMap.get(shortCircuitHashCode);
                if (shortCircuitResponseInformation == null) {
                    //we need to store the response now
                    String timeoutParameterName = "timeout";
                    if (parameterList != null && parameterList.get(timeoutParameterName) != null) {
                        timeout = Long.valueOf(parameterList.get(timeoutParameterName))
                                + Calendar.getInstance().getTimeInMillis();
                    } else {
                        timeout = Long.MAX_VALUE; //forever
                    }
                    shortCircuitResponseInformation = new ShortCircuitResponseInformation(wilmaHttpResponse, timeout);
                    shortCircuitMap.put(shortCircuitHashCode, shortCircuitResponseInformation);
                    logger.info("ShortCircuit: Message captured for hashcode: " + shortCircuitHashCode);
                } else { //we have response
                    //take care about timeout
                    timeout = Calendar.getInstance().getTimeInMillis();
                    if (timeout > shortCircuitResponseInformation.getTimeout()) {
                        shortCircuitMap.remove(shortCircuitHashCode);
                        logger.debug("ShortCircuit: Timeout has happened for hashcode: " + shortCircuitHashCode);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestReceive(WilmaHttpRequest wilmaHttpRequest, ParameterList parameterList) {
        wilmaHttpRequest.addHeaderUpdate(ShortCircuitChecker.SHORT_CIRCUIT_HEADER, wilmaHttpRequest.getRequestLine() + "_" + wilmaHttpRequest.getBody().hashCode());
    }

    @Override
    public String handleRequest(HttpServletRequest httpServletRequest, String request, HttpServletResponse httpServletResponse) {
        String myMethod = httpServletRequest.getMethod();
        String myQueryString = httpServletRequest.getQueryString();
        boolean myCall = request.equalsIgnoreCase(this.getClass().getSimpleName() + "/circuits");

        //set default response
        String response = "{ \"unknownServiceCall\": \"" + myMethod + ":" + request + "\" }";
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);

        //handle basic call
        if (myCall && httpServletRequest.getQueryString() == null) {
            response = handleBasicCall(myMethod, httpServletResponse);
        }

        //handle complex calls
        if (myCall && httpServletRequest.getQueryString() != null) {
            response = handleComplexCall(myMethod, myQueryString, httpServletResponse);
        }
        return response;
    }

    private String handleBasicCall(String myMethod, HttpServletResponse httpServletResponse) {
        String response = null;
        if ("get".equalsIgnoreCase(myMethod)) {
            //list the map (circuits + get)
            response = handleCircuitRequest(httpServletResponse);
        }
        if ("delete".equalsIgnoreCase(myMethod)) {
            //invalidate map (remove all from map) (circuits + delete)
            shortCircuitMap.clear();
            response = handleCircuitRequest(httpServletResponse);
        }
        return response;
    }

    private String handleComplexCall(String myMethod, String myQueryString, HttpServletResponse httpServletResponse) {
        String response = null;
        if ("post".equalsIgnoreCase(myMethod)) {
            //save map (to files) (circuits?folder + post)
            //TODO
            response = handleCircuitRequest(httpServletResponse);
        }
        if ("get".equalsIgnoreCase(myMethod)) {
            //load map (from files) (circuits?folder + get)
            //TODO
            response = handleCircuitRequest(httpServletResponse);
        }
        if ("delete".equalsIgnoreCase(myMethod)) {
            //invalidate a single entry (remove a specific entry) (circuits/n)
            //TODO
            response = handleCircuitRequest(httpServletResponse);
        }
        return response;
    }

    private String handleCircuitRequest(HttpServletResponse httpServletResponse) {
        StringBuilder response = new StringBuilder("{\n  \"shortCircuitMap\": [\n");
        if (!shortCircuitMap.isEmpty()) {
            String[] keySet = shortCircuitMap.keySet().toArray(new String[shortCircuitMap.size()]);
            for (int i = 0; i < keySet.length; i++) {
                String entryKey = keySet[i];
                response.append("    \"").append(i).append("\": \"").append(entryKey).append("\"");
                if (i < keySet.length - 1) {
                    response.append(",");
                }
                response.append("\n");
            }
        }
        response.append("  ]\n}\n");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return response.toString();
    }

    @Override
    public Set<String> getHandlers() {
        return Sets.newHashSet(
                this.getClass().getSimpleName() + "/circuits"
        );
    }
}
