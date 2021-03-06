/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.runtime.internal.subscriber;

import com.espertech.esper.common.internal.util.TypeWidenerSPI;
import com.espertech.esper.runtime.client.EPStatement;

/**
 * Implementation of a convertor for column results that renders the result as an object array itself.
 */
public class DeliveryConvertorWidenerWStatement implements DeliveryConvertor {
    private final TypeWidenerSPI[] wideners;
    private final EPStatement statement;

    public DeliveryConvertorWidenerWStatement(TypeWidenerSPI[] wideners, EPStatement statement) {
        this.wideners = wideners;
        this.statement = statement;
    }

    public Object[] convertRow(Object[] columns) {
        Object[] values = new Object[columns.length + 1];
        values[0] = statement;
        int offset = 1;
        for (int i = 0; i < columns.length; i++) {
            if (wideners[i] == null) {
                values[offset] = columns[i];
            } else {
                values[offset] = wideners[i].widen(columns[i]);
            }
            offset++;
        }
        return values;
    }
}
