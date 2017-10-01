/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.identity.sso.agent.util;

import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Comprised of Util methods for SSOAgentFilters.
 */
public class SSOAgentFilterUtils {

    public static SSOAgentConfig getSSOAgentConfig(FilterConfig filterConfig) throws SSOAgentException{

        return getSSOAgentConfig(filterConfig.getServletContext());
    }

    public static SSOAgentConfig getSSOAgentConfig(ServletContext application) throws SSOAgentException {

        // Make sure SSOAgentConstants.CONFIG_BEAN_NAME attribute is added on servlet context initialization.
        // It should be in the type of SSOAgentConfig.
        Object configBeingAttribute = application.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        if (!(configBeingAttribute instanceof SSOAgentConfig)) {
            throw new SSOAgentException("Cannot find " + SSOAgentConstants.CONFIG_BEAN_NAME +
                    " attribute of SSOAgentConfig type in the servletContext. Cannot proceed further.");
        }

        return (SSOAgentConfig) configBeingAttribute;
    }

    public static boolean shouldGoToWelcomePage(HttpServletRequest request) {

        if (request == null) {
            return true;
        }

        //check should go to welcome page, if so go to welcome page
        Object shouldGoToWelcomePage = request.getAttribute(SSOAgentConstants.SHOULD_GO_TO_WELCOME_PAGE);
        return shouldGoToWelcomePage instanceof String && Boolean.parseBoolean((String) shouldGoToWelcomePage);
    }
}
