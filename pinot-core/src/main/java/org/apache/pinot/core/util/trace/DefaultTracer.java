/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.util.trace;

import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.trace.ExecutionRecording;
import org.apache.pinot.spi.trace.FilterType;
import org.apache.pinot.spi.trace.OperatorExecution;
import org.apache.pinot.spi.trace.Phase;
import org.apache.pinot.spi.trace.TraceContext;
import org.apache.pinot.spi.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultTracer implements Tracer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTracer.class);
  private static final ThreadLocal<Deque<ExecutionRecording>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

  private static class NoOpExecution implements OperatorExecution {

    @Override
    public void setDocsScanned(long docsScanned) {
    }

    @Override
    public void setBytesProcessed(long bytesScanned) {
    }

    @Override
    public void setFilterType(FilterType filterType, String predicateType) {
    }

    @Override
    public void setPhase(Phase phase) {
    }

    @Override
    public void setDataTypes(FieldSpec.DataType inputDataType, FieldSpec.DataType outputDataType) {
    }

    @Override
    public void setDocIdRange(int firstDocId, int lastDocId) {
    }

    @Override
    public void setColumnCardinality(int cardinality) {
    }

    @Override
    public void close() {
    }
  }

  private static final NoOpExecution NO_OP_SPAN = new NoOpExecution();

  private static final class MillisExecution extends NoOpExecution {

    private final long _startTimeMillis = System.currentTimeMillis();
    private final Class<?> _operator;
    private final Runnable _onClose;

    public MillisExecution(Class<?> operator, Runnable onClose) {
      _operator = operator;
      _onClose = onClose;
    }

    @Override
    public void close() {
      String operatorName = _operator.getSimpleName();
      long duration = System.currentTimeMillis() - _startTimeMillis;
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Time spent in {}: {}", operatorName, duration);
      }
      org.apache.pinot.core.util.trace.TraceContext.logTime(operatorName, duration);
      _onClose.run();
    }
  }

  @Override
  public void register(long requestId) {
    org.apache.pinot.core.util.trace.TraceContext.register(requestId);
  }

  @Override
  public OperatorExecution startOperatorExecution(Class<?> operatorClass) {
    if (org.apache.pinot.core.util.trace.TraceContext.traceEnabled()) {
      Deque<ExecutionRecording> stack = getStack();
      MillisExecution execution = new MillisExecution(operatorClass, stack::removeLast);
      stack.addLast(execution);
      return execution;
    }
    return NO_OP_SPAN;
  }

  @Override
  public ExecutionRecording activeRecording() {
    Deque<ExecutionRecording> stack = getStack();
    return stack.isEmpty() ? NO_OP_SPAN : stack.peekLast();
  }

  private Deque<ExecutionRecording> getStack() {
    Thread thread = Thread.currentThread();
    if (thread instanceof TraceContext) {
      return ((TraceContext) thread).getRecordings();
    }
    return STACK.get();
  }
}
