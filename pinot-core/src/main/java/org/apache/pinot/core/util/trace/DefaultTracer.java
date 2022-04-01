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

import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.trace.FilterType;
import org.apache.pinot.spi.trace.OperatorExecution;
import org.apache.pinot.spi.trace.Phase;
import org.apache.pinot.spi.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultTracer implements Tracer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTracer.class);

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

    public MillisExecution(Class<?> operator) {
      _operator = operator;
    }

    @Override
    public void close() {
      String operatorName = _operator.getSimpleName();
      long duration = System.currentTimeMillis() - _startTimeMillis;
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Time spent in {}: {}", operatorName, duration);
      }
      TraceContext.logTime(operatorName, duration);
    }
  }

  @Override
  public void register(long requestId) {
    TraceContext.register(requestId);
  }

  @Override
  public OperatorExecution startOperatorExecution(Class<?> operatorClass) {
    return TraceContext.traceEnabled() ? new MillisExecution(operatorClass) : NO_OP_SPAN;
  }
}
