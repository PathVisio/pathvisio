/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.encoding;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializerFactory;

import java.util.Iterator;
import java.util.Vector;

/**
 * SerializerFactory for DataSer
 *
 * @author Rich Scheuerle <scheu@us.ibm.com>
 */
public class DataSerFactory implements SerializerFactory {
    private Vector mechanisms;

    public DataSerFactory() {
    }
    public javax.xml.rpc.encoding.Serializer getSerializerAs(String mechanismType) {
        return new DataSer();
    }
    public Iterator getSupportedMechanismTypes() {
        if (mechanisms == null) {
            mechanisms = new Vector();
            mechanisms.add(Constants.AXIS_SAX);
        }
        return mechanisms.iterator();
    }
}
