# JWT credential provider

To enable 3rd party web applications to integrate with the Bloomreach CMS a JWT provider is part of XIN Mods.

Once a user has been logged in to the CMS, a call to `/cms/ws/jwt` will provide you with a JSON string containing a
JWT. It contains the following additional claims:

* `username`: the name of the user that was logged in with
* `usergroups`: the groups this user belongs to in the CMS.

To validate the signature of this token, you can point at a JWKS found here `/cms/ws/jwks.json`. The private key
used to generate the tokens is found in `webapps/keys/*.pem|der` in the CMS project.

Before using this facility, make sure to generate new keys using the script in `./bin/jwt/generate_key.sh` by calling
it as follows:

    $ cd bin/jwt/ 
    $ ./generate_key.sh jwt

This will generate four new files, that will replace the existing key files. Ideally you do not store private keys in
the repo, instead you could make bundling it from an external source part of your build process.

As with normal Basic authentication for the API endpoints, only users either `admin` or contained within the
`restapi` group are allowed to access the endpoints.

To successfully retrieve the JWT from the /cms/ws/jwt endpoint, one must be sure to include
the `?source=http://<yourdomain>` parameter to allow the `Access-Control-Allow-Origin` parameter
to be setup appropriately. Also, make sure the XHR has `withCredentials` enabled so that the session
cookies are being sent along to the JWT endpoint.

Sources that are valid to be used as a `source` parameter must be whitelisted in the xinmods module config at:

* `/hippo:configuration/xinmods/hippo:moduleconfig`

Add a new property called: `jwt.whitelist` of  type String multiple. A good default value could be `http://localhost:8090`
if the application requesting the JWT is running on `localhost` at port `8090`.

