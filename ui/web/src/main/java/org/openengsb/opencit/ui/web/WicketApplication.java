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

package org.openengsb.opencit.ui.web;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.openengsb.ui.common.OpenEngSBWicketApplication;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;

public class WicketApplication extends OpenEngSBWicketApplication {
    private final ApplicationLifecycleListener lifecycleListener;

    public WicketApplication(ApplicationLifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Index.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }

    @Override
    protected void init() {
        lifecycleListener.onInit(this);
        super.init();
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return AdminWebSession.class;
    }

    @Override
    protected void onDestroy() {
        lifecycleListener.onDestroy(this);
        super.onDestroy();
    }
}
