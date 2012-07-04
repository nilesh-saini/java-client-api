/*
 * Copyright 2012 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client;

import com.marklogic.client.io.marker.XMLReadHandle;
import com.marklogic.client.io.marker.XMLWriteHandle;

/**
 * A XML Document Manager supports database operations on XML documents.
 */
public interface XMLDocumentManager extends DocumentManager<XMLReadHandle, XMLWriteHandle> {
    /**
     * The DocumentRepair enumeration specifies whether an XML document is repaired as much as possible or not at all.
     */
    public enum DocumentRepair {
        FULL, NONE;
    }

	/**
	 * Returns the repair policy for XML documents written by the manager.
	 * 
	 * @return	the repair policy for written documents
	 */
    public DocumentRepair getDocumentRepair();
    /**
	 * Specifies whether poorly formed XML documents written by the manager should be repaired on the server.
	 * 
     * @param policy	the repair policy for written documents
     */
    public void setDocumentRepair(DocumentRepair policy);
}
