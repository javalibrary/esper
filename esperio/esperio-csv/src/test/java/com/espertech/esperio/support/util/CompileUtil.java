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
package com.espertech.esperio.support.util;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPUndeployException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompileUtil {
    public static EPDeployment compileDeploy(EPRuntime epService, String epl) {
        try {
            CompilerArguments args = new CompilerArguments(epService.getConfigurationDeepCopy());
            args.getPath().add(epService.getRuntimePath());
            EPCompiled compiled = EPCompilerProvider.getCompiler().compile(epl, args);
            return epService.getDeploymentService().deploy(compiled);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void tryInvalidCompileGraph(EPRuntime epService, String graph, String expected) {
        try {
            CompilerArguments args = new CompilerArguments(epService.getConfigurationDeepCopy());
            args.getPath().add(epService.getRuntimePath());
            EPCompilerProvider.getCompiler().compile(graph, args);
            fail();
        } catch (EPCompileException ex) {
            if (!ex.getMessage().startsWith(expected)) {
                assertEquals(expected, ex.getMessage());
            }
        }
    }

    public static void undeployAll(EPRuntime epService) {
        try {
            epService.getDeploymentService().undeployAll();
        } catch (EPUndeployException e) {
            throw new RuntimeException(e);
        }
    }
}
