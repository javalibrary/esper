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
package com.espertech.esper.common.internal.epl.agg.groupbylocal;

import com.espertech.esper.common.internal.epl.agg.core.AggregationRowFactory;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.serde.DataInputOutputSerdeWCollation;

public class AggregationLocalGroupByLevel {

    private final AggregationRowFactory rowFactory;
    private final DataInputOutputSerdeWCollation rowSerde;
    private final Class[] groupKeyTypes;
    private final ExprEvaluator groupKeyEval;
    private final boolean isDefaultLevel;

    public AggregationLocalGroupByLevel(AggregationRowFactory rowFactory, DataInputOutputSerdeWCollation rowSerde, Class[] groupKeyTypes, ExprEvaluator groupKeyEval, boolean isDefaultLevel) {
        this.rowFactory = rowFactory;
        this.rowSerde = rowSerde;
        this.groupKeyTypes = groupKeyTypes;
        this.groupKeyEval = groupKeyEval;
        this.isDefaultLevel = isDefaultLevel;
    }

    public AggregationRowFactory getRowFactory() {
        return rowFactory;
    }

    public DataInputOutputSerdeWCollation getRowSerde() {
        return rowSerde;
    }

    public Class[] getGroupKeyTypes() {
        return groupKeyTypes;
    }

    public ExprEvaluator getGroupKeyEval() {
        return groupKeyEval;
    }

    public boolean isDefaultLevel() {
        return isDefaultLevel;
    }
}
