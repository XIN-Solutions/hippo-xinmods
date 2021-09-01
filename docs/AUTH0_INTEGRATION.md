# XINMods Auth0 Integration

When integrating the Bloomreach CMS into organisations it may be appropriate to hook into an existing
organisational component that provides authentication; such as Google Groups, a SAML provider, Social Logins and
other another IdP. Building a connector for each possible integration would be tiring. Instead, using the Auth0 
integration you can let Auth0 do the heavy lifting regarding these integrations while the CMS only integrates 
with Auth0.

Other reasons to use this is to outsource password management (forgot password etc.) and user group management 
to Auth0, which seems to be a slightly smoother experience overall. 

Follow the steps below to start using this integration. 

## Hooking up Auth0

To enable Auth0 support do the following:

* Make sure the clientId you are integrating with is a Machine to Machine type Application, this will ensure a 
  JWT is returned for us to verify.  

* Make sure to associate a Custom API with this application, it is where we will define our Permissions. 

* Modify the `loginPage` node and point it at the `Auth0LoginPlugin`: 


    /hippo:configuration/hippo:frontend/login/login/loginPage

        plugin.class = nz.xinsolutions.authentication.Auth0LoginPlugin

* Create or update your xinmods module configuration and annotate it with `auth0.*` configuration properties. If you
  don't have one yet you could create one yourself (just mirror the structure of other modules) or install the 
  `Enable XIN Mods - Common.zip` package from the `docs/packages/` folder.

    
    /hippo:modules/xinmods/hippo:moduleconfig

        auth0.appName = The Name used on the login screen "Click below to login to <appName>"
        auth0.clientId = The Auth0 Application Client Identifier you want to authenticate against
        auth0.audience = The audience entry that is required to be there during JWT verification 
        auth0.domain = The domain at which your auth0 account lives (eg, <tenant>.au.auth0.com)
        auth0.jwksUrl = The URL at which the key description is found (eg, https://<tenant>.au.auth0..com/.well-known/jwks.json) 
        auth0.afterLoginUrl = Where are we redirecting after login? (usually just back to https://<env>/cms)
        auth0.afterLogoutUrl = Where are we redirecting after logout? (usually just back to https://<env>/cms)
        

Restart your CMS.

It is advisable to use different client configurations on development and production environments. 

## Roles and Groups Management

When the plugin is in use, and a user logs in, their profile information is updated and new groups and roles are added
and/or removed from their account's representation in the Bloomreach Repository as if they were a local account. 

To manage which users will get what roles and groups you have to add Permissions to the API. 

Adding a permission that represents what would be a role (like `xm.cms.user`) in brXM we create a Permission
named `role:xm.cms.user`. Similarly, to create a permission that represents a group in brXM prefix it with
`group:`. So let's say there's a group called `tools-styling` that gives the user access to a specific tool,
the Permission's ID would be called `group:tools-styling`.

These permissions can be assigned to individual accounts, or as part of a role (which implies one or more permissions). 
Once the user logs in, they become part of the `permissions` array in the JWT, and will be synchronised into the CMS. 

**Be careful though!** Whichever groups you expose here, a user can become a part of when assigned the permissions, you 
might want to stay away from `admin` type permissions. 

## Using the plugin

When you've restarted the CMS you will be greeted with a slightly different login experience. Instead of the
usual username and password field, there now is a button the user must press that will trigger the login workflow. 

In some cases you want to login with a local user (like `admin`), to do so, add `#system` to the URL. The old
username and password fields will reappear. 

In some situations, especially while configuring the plugin you might get stuck with a logged in user. To logout, open
a console in your browser and type: `logoutFromAuth0()`; this will manually trigger the logout in the Auth0 SDK. 

