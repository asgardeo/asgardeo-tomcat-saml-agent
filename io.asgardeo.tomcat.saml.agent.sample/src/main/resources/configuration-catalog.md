# Configuration Catalog

This document describes all the configuration parameters that are used in Asgardeo Tomcat SAML Agent.

## Required Parameters
### Assertion Consumer URL
**Property Name:** `SAML2.AssertionConsumerURL`

**Description:** The URL of the SAML 2.0 Assertion Consumer. This refers to the service provider's endpoint (URL) that
 is responsible for receiving and parsing a SAML assertion.
 
 **Sample:** 
 
```
 SAML2.AssertionConsumerURL=https://my-app/home.jsp
```
 
### Service Provider Entity ID
**Property Name:** `SAML2.SPEntityId`

**Description:** A unique identifier for the SAML 2.0 application.

**Sample:** 

```
SAML2.SPEntityId=my-app
```
 
### Identity Provider Entity ID
**Property Name:** `SAML2.IdPEntityId`
 
**Description:** A unique identifier for the SAML 2.0 identity provider

**Sample:** 

```
SAML2.IdPEntityId=accounts.asgardeo.io
```

### Identity Provider URL
 
**Property Name:** `SAML2.IdPURL`
 
**Description:** The URL of the SAML 2.0 identity provider

**Sample:** 

```
SAML2.IdPURL=https://accounts.asgardeo.io/t/a/samlsso
```

### SAML2 SSO Authentication Request Endpoint
**Property Name:** `SAML2SSOURL`
 
**Description:** This specifies the URL to send the SAML2 SSO Authentication Request.

**Sample:** 

```
SAML2SSOURL=samlsso
```

## Conditional Parameters
### Single Logout URL
**Property Name:** `SAML2.SLOURL`
 
**Description:** In case Single Logout is enabled for the application, this parameter is mandatory. In such cases
, the Single Logout URL specifies the URL that is used for SLO.

**Sample:** 

```
SAML2.SLOURL=logout
```
## Keystore related Configurations
**Note:**
The following conditional configurations are required if you want to enable **Response Signing**, **Request Signing**
, **Assertion Signing**, and **Assertion Encryption**.

### Identity Provider Public Certificate
**Property Name:** `IdPPublicCert`
 
**Description:** This parameter specifies the pem content of the IDP public certificate.

**Sample:** 

```
IdPPublicCert=<CERTIFICATE_PEM_CONTENT>
```

### KeyStore Password
**Property Name:** `KeyStorePassword`
 
**Description:** This parameter specifies the password of the Key Store for the SAML application.

**Sample:** 

```
KeyStorePassword=wso2carbon
```

### Identity Provider Public Certificate Alias
**Property Name:** `IdPPublicCertAlias`
 
**Description:** This parameter specifies the alias of the IdP's public certificate.

**Sample:** 

```
IdPPublicCertAlias=wso2carbon
```

### Service Provider Private Key Alias
**Property Name:** `PrivateKeyAlias`
 
**Description:** This parameter specifies the alias of the service provider's private key.

**Sample:** 

```
PrivateKeyAlias=wso2carbon
```

### Private Key Password
**Property Name:** `PrivateKeyPassword`
 
**Description:** This parameter specifies the private key password to retrieve the private key that is used to sign
 Authentication
 Request and Logout Request messages.

**Sample:** 

```
PrivateKeyPassword=wso2carbon
```

## Optional Parameters
### Single Sign-On
**Property Name:** `EnableSAML2SSOLogin`
 
**Description:** This parameter specifies if Single Sign on is enabled/disabled for the application.

**Sample:** 

```
EnableSAML2SSOLogin=true
```

### Single Logout

**Property Name:** `SAML2.EnableSLO`
 
**Description:** This parameter specifies if Single Logout is enabled/disabled for the application.

**Sample:** 

```
SAML2.EnableSLO=true
```
### Response Signing
**Property Name:** `SAML2.EnableResponseSigning`
 
**Description:** This parameter specifies if the SAML response should be signed.

**Sample:** 

```
SAML2.EnableResponseSigning=true
```
### Assertion Signing
**Property Name:** `SAML2.EnableAssertionSigning`
 
**Description:** This parameter specifies if the SAML assertion element should be signed.

**Sample:** 

```
SAML2.EnableAssertionSigning=true
```

### Assertion Encryption
**Property Name:** `SAML2.EnableAssertionEncryption`
 
**Description:** This parameter specifies if the SAML assertion element should be encrypted.

**Sample:** 

```
SAML2.EnableAssertionEncryption=true
```

### Request Signing
**Property Name:** `SAML2.EnableRequestSigning`
 
**Description:** This parameter specifies if the SAML Authentication requests and the Logout requests should be signed.

**Sample:** 

```
SAML2.EnableRequestSigning=true
```

### Passive Authentication
**Property Name:** `SAML2.IsPassiveAuthn`
 
**Description:** This parameter specifies if the SAML Authentication request is passive.

**Sample:** 

```
SAML2.IsPassiveAuthn=true
```

### Skip URIs

**Property Name:** `skipURIs`
 
**Description:** This parameter may include URIs that need not be secured. Multiple URIs can be set using comma separated
  values.

**Sample:** 

```
skipURIs=/my-app/page1.jsp,/my-app/page2.jsp
```

### Index Page

**Property Name:** `indexPage`
 
**Description:** This parameter may denote the URI for the landing page of the webapp.

**Sample:** 

```
indexPage=/my-app/index.html
```
### Error Page

**Property Name:** `errorPage`
 
**Description:** This parameter may denote the URI for the error page of the webapp.

**Sample:** 

```
errorPage=/error.jsp
```
