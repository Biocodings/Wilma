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

import com.epam.wilma.domain.http.WilmaHttpResponse;
import com.epam.wilma.domain.stubconfig.interceptor.ResponseInterceptor;
import com.epam.wilma.domain.stubconfig.parameter.ParameterList;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by tkohegyi on 2016. 02. 20.
 */
public class ShortCircuitInterceptor implements ResponseInterceptor {

    private final String timeoutParameterName = "timeout";

    @Override
    public void onResponseReceive(WilmaHttpResponse wilmaHttpResponse, ParameterList parameterList) {
        String shortCircuitHashCode = wilmaHttpResponse.getRequestHeader(ShortCircuitChecker.SHORT_CIRCUIT_HEADER);
        if (shortCircuitHashCode != null) { //this
            long timeout;
            //do we have the response already, or we need to catch it right now?
            Map<String, ShortCircuitResponseInformation> shortCircuitMap = ShortCircuitChecker.getShortCircuitMap();
            ShortCircuitResponseInformation shortCircuitResponseInformation = shortCircuitMap.get(shortCircuitHashCode);
            if (shortCircuitResponseInformation == null) {
                //we need to store the response now

                if (parameterList != null && parameterList.get(timeoutParameterName) != null) {
                    timeout = Long.valueOf(parameterList.get(timeoutParameterName))
                        + Calendar.getInstance().getTimeInMillis();
                } else {
                    timeout = Long.MAX_VALUE; //forever
                }
                shortCircuitResponseInformation = new ShortCircuitResponseInformation(wilmaHttpResponse, timeout);
                shortCircuitMap.put(shortCircuitHashCode, shortCircuitResponseInformation);
            } else { //we have response
                //take care about timeout
                timeout = Calendar.getInstance().getTimeInMillis();
                if (timeout > shortCircuitResponseInformation.getTimeout()) {
                    shortCircuitMap.remove(shortCircuitHashCode);
                }
            }
        }
    }

}
