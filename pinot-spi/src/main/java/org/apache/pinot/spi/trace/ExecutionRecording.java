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
package org.apache.pinot.spi.trace;

import org.apache.pinot.spi.data.FieldSpec;


public interface ExecutionRecording {

  /**
   * Sets the number of docs scanned by the operator.
   * @param docsScanned how many docs were scanned.
   */
  void setDocsScanned(long docsScanned);

  /**
   * Sets the number of bytes scanned by the operator if this is possible to compute.
   * @param bytesScanned the number of bytes scanned
   */
  void setBytesProcessed(long bytesScanned);

  /**
   * If the operator is a filter, determines the filter type (scan or index) and the predicate type
   * @param filterType SCAN or INDEX
   * @param predicateType e.g. BETWEEN, REGEXP_LIKE
   */
  void setFilterType(FilterType filterType, String predicateType);

  /**
   * The phase of the query
   * @param phase the phase
   */
  void setPhase(Phase phase);

  /**
   * Records whether type transformation took place during the operator's invocation and what the types were
   * @param inputDataType the input data type
   * @param outputDataType the output data type
   */
  void setDataTypes(FieldSpec.DataType inputDataType, FieldSpec.DataType outputDataType);

  /**
   * Records the range of docIds during the operator invocation. This is useful for implicating a range of records
   * in a slow operator invocation.
   * @param firstDocId the first docId in the block
   * @param lastDocId the last docId in the block
   */
  void setDocIdRange(int firstDocId, int lastDocId);

  /**
   * If known, record the cardinality of the column within the segment this operator invoked on
   * @param cardinality the number of distinct values
   */
  void setColumnCardinality(int cardinality);
}
