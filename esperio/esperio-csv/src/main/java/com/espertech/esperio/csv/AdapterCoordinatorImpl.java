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
package com.espertech.esperio.csv;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.internal.schedule.ScheduleBucket;
import com.espertech.esper.common.internal.util.ExecutionPathDebugLog;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.util.AdapterState;
import com.espertech.esper.runtime.client.util.InputAdapter;
import com.espertech.esper.runtime.internal.kernel.service.EPRuntimeSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An implementation of AdapterCoordinator.
 */
public class AdapterCoordinatorImpl extends AbstractCoordinatedAdapter implements AdapterCoordinator {
    private static final Logger log = LoggerFactory.getLogger(AdapterCoordinatorImpl.class);

    private final Map<SendableEvent, CoordinatedAdapter> eventsFromAdapters = new HashMap<SendableEvent, CoordinatedAdapter>();
    private final Set<CoordinatedAdapter> emptyAdapters = new HashSet<CoordinatedAdapter>();
    private final boolean usingEngineThread, usingExternalTimer;
    private final ScheduleBucket scheduleBucket;
    private final EPRuntime runtime;

    /**
     * Ctor.
     *
     * @param runtime           - the EPServiceProvider for the runtimeservices and runtime
     * @param usingEngineThread - true if the coordinator should set time by the scheduling service in the runtime,
     *                          false if it should set time externally through the calling thread
     */
    public AdapterCoordinatorImpl(EPRuntime runtime, boolean usingEngineThread) {
        this(runtime, usingEngineThread, false, false);
    }

    /**
     * Ctor.
     *
     * @param runtime             - the EPServiceProvider for the runtimeservices and runtime
     * @param usingEngineThread   - true if the coordinator should set time by the scheduling service in the runtime,
     *                            false if it should set time externally through the calling thread
     * @param usingExternalTimer  - true to use esper's external timer mechanism instead of internal timing
     * @param usingTimeSpanEvents - true for time span events
     */
    public AdapterCoordinatorImpl(EPRuntime runtime, boolean usingEngineThread, boolean usingExternalTimer, boolean usingTimeSpanEvents) {
        super(runtime, usingEngineThread, usingExternalTimer, usingTimeSpanEvents);
        if (runtime == null) {
            throw new NullPointerException("runtime cannot be null");
        }
        if (!(runtime instanceof EPRuntimeSPI)) {
            throw new IllegalArgumentException("Illegal type of EPServiceProvider");
        }
        this.runtime = runtime;
        this.scheduleBucket = new ScheduleBucket(-1);
        this.usingEngineThread = usingEngineThread;
        this.usingExternalTimer = usingExternalTimer;
    }


    /* (non-Javadoc)
     * @see com.espertech.esperio.ReadableAdapter#read()
     */
    public SendableEvent read() throws EPException {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
            log.debug(".read");
        }
        pollEmptyAdapters();

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
            log.debug(".read eventsToSend.isEmpty==" + eventsToSend.isEmpty());
            log.debug(".read eventsFromAdapters.isEmpty==" + eventsFromAdapters.isEmpty());
            log.debug(".read emptyAdapters.isEmpty==" + emptyAdapters.isEmpty());
        }

        if (eventsToSend.isEmpty() && eventsFromAdapters.isEmpty() && emptyAdapters.isEmpty()) {
            stop();
        }

        if (stateManager.getState() == AdapterState.DESTROYED || eventsToSend.isEmpty()) {
            return null;
        }

        SendableEvent result = eventsToSend.first();

        replaceFirstEventToSend();

        return result;
    }

    /* (non-Javadoc)
     * @see com.espertech.esperio.csv.AdapterCoordinator#add(com.espertech.esperio.Adapter)
     */
    public void coordinate(InputAdapter inputAdapter) {
        if (inputAdapter == null) {
            throw new NullPointerException("AdapterSpec cannot be null");
        }

        if (!(inputAdapter instanceof CoordinatedAdapter)) {
            throw new IllegalArgumentException("Cannot coordinate a Adapter of type " + inputAdapter.getClass());
        }
        CoordinatedAdapter adapter = (CoordinatedAdapter) inputAdapter;
        if (eventsFromAdapters.values().contains(adapter) || emptyAdapters.contains(adapter)) {
            return;
        }
        adapter.disallowStateTransitions();
        adapter.setEPService(runtime);
        adapter.setUsingEngineThread(usingEngineThread);
        adapter.setUsingExternalTimer(usingExternalTimer);
        adapter.setScheduleSlot(scheduleBucket.allocateSlot());
        addNewEvent(adapter);
    }

    /**
     * Does nothing.
     */
    protected void close() {
        // Do nothing
    }

    /**
     * Replace the first member of eventsToSend with the next
     * event returned by the read() method of the same Adapter that
     * provided the first event.
     */
    protected void replaceFirstEventToSend() {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
            log.debug(".replaceFirstEventToSend Replacing event");
        }
        SendableEvent theEvent = eventsToSend.first();
        eventsToSend.remove(theEvent);
        addNewEvent(eventsFromAdapters.get(theEvent));
        eventsFromAdapters.remove(theEvent);
        pollEmptyAdapters();
    }

    /**
     * Reset all the changeable state of this ReadableAdapter, as if it were just created.
     */
    protected void reset() {
        eventsFromAdapters.clear();
        emptyAdapters.clear();
    }

    private void addNewEvent(CoordinatedAdapter adapter) {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
            log.debug(".addNewEvent eventsFromAdapters==" + eventsFromAdapters);
        }
        SendableEvent theEvent = adapter.read();
        if (theEvent != null) {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
                log.debug(".addNewEvent event==" + theEvent);
            }
            eventsToSend.add(theEvent);
            eventsFromAdapters.put(theEvent, adapter);
        } else {
            if (adapter.getState() == AdapterState.DESTROYED) {
                eventsFromAdapters.values().removeAll(Collections.singleton(adapter));
            } else {
                emptyAdapters.add(adapter);
            }
        }
    }

    private void pollEmptyAdapters() {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled())) {
            log.debug(".pollEmptyAdapters emptyAdapters.size==" + emptyAdapters.size());
        }

        for (Iterator<CoordinatedAdapter> iterator = emptyAdapters.iterator(); iterator.hasNext(); ) {
            CoordinatedAdapter adapter = iterator.next();
            if (adapter.getState() == AdapterState.DESTROYED) {
                iterator.remove();
                continue;
            }

            SendableEvent theEvent = adapter.read();
            if (theEvent != null) {
                eventsToSend.add(theEvent);
                eventsFromAdapters.put(theEvent, adapter);
                iterator.remove();
            }
        }
    }
}
