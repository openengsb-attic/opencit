/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class ProjectStateInfo implements Serializable {

    private Date lastpollDate;
    private Project.State state;

    public synchronized Date getLastpollDate() {
        return this.lastpollDate;
    }

    public synchronized Project.State getState() {
        return this.state;
    }

    public synchronized void setState(Project.State state) {
        this.state = state;
    }

    public synchronized void setLastpollDate(Date lastpollDate) {
        this.lastpollDate = lastpollDate;
    }

}
