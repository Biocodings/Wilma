package com.epam.wilma.router.helper;
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

import java.util.Map.Entry;
import org.springframework.stereotype.Component;
import com.epam.wilma.domain.http.WilmaHttpResponse;

/**
 * Copies a {@link WilmaHttpResponse}.
 * @author Tibor_Kovacs
 *
 */
@Component
public class WilmaHttpResponseCloner {
    /**
     * Copies a {@link WilmaHttpResponse}'s headers and body to new instance.
     * @param response the original response
     * @return the new instance
     */
    public WilmaHttpResponse cloneResponse(final WilmaHttpResponse response) {
        WilmaHttpResponse result = new WilmaHttpResponse();
        result.setStatusCode(response.getStatusCode());
        result.setContentType(response.getContentType());
        result.setBody(response.getBody());
        copyHeaders(response, result);
        return result;
    }

    private void copyHeaders(final WilmaHttpResponse response, final WilmaHttpResponse result) {
        for (Entry<String, String> header : response.getHeaders().entrySet()) {
            result.addHeader(header.getKey(), header.getValue());
        }
        for (Entry<String, String> header : response.getExtraHeaders().entrySet()) {
            result.addExtraHeader(header.getKey(), header.getValue());
        }
        for (Entry<String, String> header : response.getRequestHeaders().entrySet()) {
            result.addRequestHeader(header.getKey(), header.getValue());
        }
    }
}
