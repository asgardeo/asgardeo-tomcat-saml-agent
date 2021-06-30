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
 */

package io.asgardeo.tomcat.saml.agent;

import io.asgardeo.java.saml.sdk.SAML2SSOManager;
import io.asgardeo.java.saml.sdk.bean.LoggedInSessionBean;
import io.asgardeo.java.saml.sdk.bean.SSOAgentConfig;
import io.asgardeo.java.saml.sdk.exception.InvalidSessionException;
import io.asgardeo.java.saml.sdk.exception.SSOAgentException;
import io.asgardeo.java.saml.sdk.util.SSOAgentConstants;
import io.asgardeo.java.saml.sdk.util.SSOAgentFilterUtils;
import io.asgardeo.java.saml.sdk.util.SSOAgentRequestResolver;
import io.asgardeo.java.saml.sdk.util.SSOAgentUtils;
import org.opensaml.saml.saml2.core.LogoutResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

            if (resolver.isSLORequest()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    LogoutResponse logoutResponse = samlSSOManager.doSLO(request);
                    String encodedRequestMessage = samlSSOManager.buildPostResponse(logoutResponse);
                    SSOAgentUtils.sendPostResponse(request, response, encodedRequestMessage);
                } catch (SSOAgentException e) {
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
                return;
            } else if (resolver.isSAML2SSOResponse()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    samlSSOManager.processResponse(request, response);
                    if (!SSOAgentFilterUtils.shouldGoToWelcomePage(request)) {
                        response.sendRedirect(ssoAgentConfig.getSAML2().getACSURL());
                    }
                } catch (SSOAgentException e) {
                    if (e instanceof InvalidSessionException) {
                        // Redirect to the index page when session is expired or user already logged out.
                        LOGGER.log(Level.FINE, "Invalid Session!", e);
                        response.sendRedirect(filterConfig.getServletContext().getContextPath());
                        return;
                    }
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }

            } else if (resolver.isSAML2ArtifactResponse()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    samlSSOManager.processArtifactResponse(request);
                } catch (SSOAgentException e) {
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
            } else if (resolver.isSLOURL()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    if (resolver.isHttpPostBinding()) {
                        boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                        ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                        String htmlPayload = samlSSOManager.buildPostRequest(request, response, true);
                        ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                        SSOAgentUtils.sendPostResponse(request, response, htmlPayload);

                    } else {
                        // If "SSOAgentConstants.HTTP_BINDING_PARAM" is not defined, default to redirect.
                        boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                        ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                        String redirectUrl = samlSSOManager.buildRedirectRequest(request, true);
                        ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                        response.sendRedirect(redirectUrl);
                    }
                } catch (SSOAgentException e) {
                    if (e instanceof InvalidSessionException) {
                        // Redirect to the index page when session is expired or user already logged out.
                        LOGGER.log(Level.FINE, "Invalid Session!", e);
                        response.sendRedirect(filterConfig.getServletContext().getContextPath());
                        return;
                    }
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
                return;

            } else if (resolver.isSAML2SSOURL()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    if (resolver.isHttpPostBinding()) {
                        String htmlPayload = samlSSOManager.buildPostRequest(request, response, false);
                        SSOAgentUtils.sendPostResponse(request, response, htmlPayload);
                        return;
                    }
                    response.sendRedirect(samlSSOManager.buildRedirectRequest(request, false));
                } catch (SSOAgentException e) {
                    if (e instanceof InvalidSessionException) {
                        // Redirect to the index page when session is expired or user already logged out.
                        LOGGER.log(Level.FINE, "Invalid Session!", e);
                        response.sendRedirect(filterConfig.getServletContext().getContextPath());
                        return;
                    }
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
                return;

            } else if (resolver.isPassiveAuthnRequest()) {

                try {
                    samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                    boolean isPassiveAuth = ssoAgentConfig.getSAML2().isPassiveAuthn();
                    ssoAgentConfig.getSAML2().setPassiveAuthn(true);
                    String redirectUrl = samlSSOManager.buildRedirectRequest(request, false);
                    ssoAgentConfig.getSAML2().setPassiveAuthn(isPassiveAuth);
                    response.sendRedirect(redirectUrl);
                } catch (SSOAgentException e) {
                    if (e instanceof InvalidSessionException) {
                        // Redirect to the index page when session is expired or user already logged out.
                        LOGGER.log(Level.FINE, "Invalid Session!", e);
                        response.sendRedirect(filterConfig.getServletContext().getContextPath());
                        return;
                    }
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
                return;
            }
            String indexPage = ssoAgentConfig.getIndexPage();
            if (request.getSession(false) != null &&
                    request.getSession(false).getAttribute(SSOAgentConstants.SESSION_BEAN_NAME) == null) {
                response.sendRedirect(indexPage);
                return;
            }

            HttpSession session = request.getSession();
            LoggedInSessionBean
                    sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);

            if (sessionBean == null || sessionBean.getSAML2SSO() == null) {
                request.getSession().invalidate();
                response.sendRedirect(indexPage);
                return;
            }
            // Pass the request along the filter chain.
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

    protected void handleException(HttpServletRequest request, HttpServletResponse response,
                                   SSOAgentConfig ssoAgentConfig, SSOAgentException e)
            throws IOException, ServletException {

        String errorPage = ssoAgentConfig.getErrorPage();
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
        }
        LOGGER.log(Level.SEVERE, e.getMessage());
        request.setAttribute(SSOAgentConstants.SSO_AGENT_EXCEPTION, e);
        RequestDispatcher requestDispatcher = request.getServletContext().getRequestDispatcher(errorPage);
        requestDispatcher.forward(request, response);
    }
}
