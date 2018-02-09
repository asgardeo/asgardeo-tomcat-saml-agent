/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sso.agent.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.component name="org.wso2.carbon.identity.sso.agent" immediate="true"
 */
public class SSOAgentServiceComponent {

    private static Log log = LogFactory.getLog(SSOAgentServiceComponent.class);
    private static RealmService realmService;

    public static RealmService getRealmService() {

        return SSOAgentServiceComponent.realmService;
    }

    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the SSO agent bundle");
        }
        SSOAgentServiceComponent.realmService = realmService;
    }

    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.info("SSO Agent bundle is activated");
        }
    }

    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.info("SSO Agent bundle is deactivated");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the SSO Agent bundle");
        }
        SSOAgentServiceComponent.realmService = null;
    }
}
