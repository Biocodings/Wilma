package com.epam.wilma.common.stream.helper;
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

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;

/**
 * Factory for creating a new instance of {@link StreamResult}.
 * @author Tunde_Kovacs
 *
 */
@Component
public class StreamResultFactory {

    /**
     * Creates a new instance of {@link StreamResult}.
     * @return the new instance.
     */
    public StreamResult createStreamResult() {
        return new StreamResult(new StringWriter());
    }

}
