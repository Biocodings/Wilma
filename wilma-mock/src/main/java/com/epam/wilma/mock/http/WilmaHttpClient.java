package com.epam.wilma.mock.http;

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

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class WilmaHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(WilmaHttpClient.class);

    public Optional<String> sendGetterRequest(String url) {
        String response = null;

        HttpClient httpclient = new HttpClient();
        GetMethod method = new GetMethod(url);

        try {
            int statusCode = httpclient.executeMethod(method);
            if (HttpStatus.SC_OK == statusCode) {
                response = method.getResponseBodyAsString();
            }
        } catch (HttpException e) {
            LOG.error("Protocol exception occurred when called: " + url, e);
        } catch (IOException e) {
            LOG.error("I/O (transport) error occurred when called: " + url, e);
        } finally {
            method.releaseConnection();
        }

        return Optional.fromNullable(response);
    }

    public boolean sendSetterRequest(String url) {
        boolean requestSuccessful = false;

        HttpClient httpclient = new HttpClient();
        GetMethod method = new GetMethod(url);

        try {
            int statusCode = httpclient.executeMethod(method);
            if (HttpStatus.SC_OK == statusCode) {
                requestSuccessful = true;
            }
        } catch (HttpException e) {
            LOG.error("Protocol exception occurred when called: " + url, e);
        } catch (IOException e) {
            LOG.error("I/O (transport) error occurred when called: " + url, e);
        } finally {
            method.releaseConnection();
        }

        return requestSuccessful;
    }

}
