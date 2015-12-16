/*
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.europeana.sounds.skos;

import org.junit.After;
import org.junit.Before;

import eu.europeana.sounds.utils.concept.SkosUtils;

/**
 * This class implements base methods for SKOS testing.
 */
public abstract class BaseSkosTest {

	protected String TEST_RDF_VOCABULARY_FILE_PATH  = "./src/test/resources/Music-Genres.rdf";

	
	private SkosUtils skosUtils = null;

    
	@Before
    public void setUp() throws Exception {
		setSkosUtils(new SkosUtils());
    }

    @After
    public void tearDown() throws Exception {
    }

        	    
	public SkosUtils getSkosUtils() {
		return skosUtils;
	}

	public void setSkosUtils(SkosUtils skosUtils) {
		this.skosUtils = skosUtils;
	}
	

}
