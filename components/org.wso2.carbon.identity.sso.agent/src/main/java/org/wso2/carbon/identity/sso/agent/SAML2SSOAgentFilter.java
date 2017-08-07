/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.sso.agent;

import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.exception.InvalidSessionException;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.oauth2.SAML2GrantManager;
import org.wso2.carbon.identity.sso.agent.saml.SAML2SSOManager;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentFilterUtils;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentRequestResolver;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class SAML2SSOAgentFilter.
 */
public class SAML2SSOAgentFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(SSOAgentConstants.LOGGER_NAME);
    protected FilterConfig filterConfig = null;

    /**
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        this.filterConfig = fConfig;
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            SSOAgentConfig ssoAgentConfig = SSOAgentFilterUtils.getSSOAgentConfig(filterConfig);

            SSOAgentRequestResolver resolver =
                    new SSOAgentRequestResolver(request, response, ssoAgentConfig);

            if (resolver.isURLToSkip()) {
                chain.doFilter(servletRequest, servletResponse);
                return;
            }

            SAML2SSOManager samlSSOManager;
            SAML2GrantManager saml2GrantManager;

            if (resolver.isSLORequest()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                samlSSOManager.doSLO(request);
                request.setAttribute(SSOAgentConstants.SHOULD_GO_TO_WELCOME_PAGE, "true");

            } else if (resolver.isSAML2SSOResponse()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                try {
                    samlSSOManager.processResponse(request, response);
                } catch (SSOAgentException e) {
                    handleException(request, e);
                }

            } else if (resolver.isSLOURL()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                if (resolver.isHttpPostBinding()) {
                    boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    String htmlPayload = samlSSOManager.buildPostRequest(request, response, true);
                    ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);
                } else {
                    // If "SSOAgentConstants.HTTP_BINDING_PARAM" is not defined, default to redirect
                    boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    String redirectUrl = samlSSOManager.buildRedirectRequest(request, true);
                    ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                    response.sendRedirect(redirectUrl);
                }
                return;

            } else if (resolver.isSAML2SSOURL()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                if (resolver.isHttpPostBinding()) {
                    String htmlPayload = samlSSOManager.buildPostRequest(request, response, false);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);
                    return;
                }
                response.sendRedirect(samlSSOManager.buildRedirectRequest(request, false));
                return;

            } else if (resolver.isPassiveAuthnRequest()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                ssoAgentConfig.getSAML2().setPassiveAuthn(true);
                String redirectUrl = samlSSOManager.buildRedirectRequest(request, false);
                ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                response.sendRedirect(redirectUrl);
                return;

            } else if (resolver.isSAML2OAuth2GrantRequest()) {

                saml2GrantManager = new SAML2GrantManager(ssoAgentConfig);
                saml2GrantManager.getAccessToken(request, response);

            }

            if (SSOAgentFilterUtils.shouldGoToWelcomePage(request)) {
                response.sendRedirect(filterConfig.getServletContext().getContextPath());
                return;
            }
            // pass the request along the filter chain
            chain.doFilter(request, response);

        } catch (InvalidSessionException e) {
            // Redirect to the index page when session is expired or user already logged out.
            LOGGER.log(Level.FINE, "Invalid Session!", e);
            response.sendRedirect(filterConfig.getServletContext().getContextPath());
        }
    }


    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        return;
    }

    protected void handleException(HttpServletRequest request, SSOAgentException e)
            throws SSOAgentException {

        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
        }
        throw e;
    }

}
