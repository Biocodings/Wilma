package com.epam.wilma.service.configuration.stub.helper.common;
/*==========================================================================
 Copyright 2013-2017 EPAM Systems

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

import com.epam.wilma.service.util.WilmaServiceException;

/**
 * This exception class is used for exceptions during stub configuration creation.
 * @author tkohegyi
 */
public class StubConfigurationException extends WilmaServiceException {

    /**
     * Constructs a new exception.
     * @param message is the error text.
     */
    public StubConfigurationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception.
     * @param message is the error text.
     * @param cause is the root cause of this exception.
     */
    public StubConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
