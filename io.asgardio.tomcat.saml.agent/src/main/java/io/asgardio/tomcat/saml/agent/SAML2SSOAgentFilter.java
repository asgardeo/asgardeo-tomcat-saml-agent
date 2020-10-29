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

package io.asgardio.tomcat.saml.agent;

import io.asgardio.java.saml.sdk.SAML2SSOManager;
import io.asgardio.java.saml.sdk.bean.LoggedInSessionBean;
import io.asgardio.java.saml.sdk.bean.SSOAgentConfig;
import io.asgardio.java.saml.sdk.exception.InvalidSessionException;
import io.asgardio.java.saml.sdk.exception.SSOAgentException;
import io.asgardio.java.saml.sdk.util.SSOAgentConstants;
import io.asgardio.java.saml.sdk.util.SSOAgentFilterUtils;
import io.asgardio.java.saml.sdk.util.SSOAgentRequestResolver;
import io.asgardio.java.saml.sdk.util.SSOAgentUtils;
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

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                LogoutResponse logoutResponse = samlSSOManager.doSLO(request);
                String encodedRequestMessage = samlSSOManager.buildPostResponse(logoutResponse);
                SSOAgentUtils.sendPostResponse(request, response, encodedRequestMessage);
                return;
            } else if (resolver.isSAML2SSOResponse()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                try {
                    samlSSOManager.processResponse(request, response);
                } catch (SSOAgentException e) {
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }

            } else if (resolver.isSAML2ArtifactResponse()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                try {
                    samlSSOManager.processArtifactResponse(request);
                } catch (SSOAgentException e) {
                    handleException(request, response, ssoAgentConfig, e);
                    return;
                }
            } else if (resolver.isSLOURL()) {

                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                if (resolver.isHttpPostBinding()) {

                    String htmlPayload = samlSSOManager.buildPostRequest(request, response, true);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);

                } else {
                    //if "SSOAgentConstants.HTTP_BINDING_PARAM" is not defined, default to redirect
                    String redirectUrl = samlSSOManager.buildRedirectRequest(request, true);
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
           		SSOAgentConfig ssoAgentConfigTemp = ssoAgentConfig.copyFrom( ssoAgentConfig );
           		ssoAgentConfigTemp.getSAML2().setPassiveAuthn(true);
           		samlSSOManager = new SAML2SSOManager(ssoAgentConfigTemp);
                String redirectUrl = samlSSOManager.buildRedirectRequest(request, false);
                response.sendRedirect(redirectUrl);
                return;
            }
            String indexPage = ssoAgentConfig.getIndexPage();
            if (request.getSession(false) != null &&
                    request.getSession(false).getAttribute(SSOAgentConstants.SESSION_BEAN_NAME) == null) {
                request.getSession().invalidate();
                response.sendRedirect(indexPage);
                return;
            }

            HttpSession session = request.getSession();
            LoggedInSessionBean
                    sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);

            if (sessionBean == null || sessionBean.getSAML2SSO() == null) {
                response.sendRedirect(indexPage);
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
