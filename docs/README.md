# General

Read more documentation here: https://xinsolutions.co.nz/bloomreach-hippo-cms-caas

If you're interested in implementing a NodeJS based client consider using the `xinmods` package found here: 

* https://www.npmjs.com/package/xinmods

# Packages

XIN mods adds simple package management to the interface allowing you to add functional modules to a bare-bones CMS deployment 
without redeployments or restarting. 

On your instance go to this URL: http://localhost:8080/site/packages/

It shows you an interface to a set of endpoints described loosely below.

To enable a number of standard features of the XIN mods go into the packages interface and install the
`Enable XIN Mods - Common.zip` package you can find in `docs/packages`.

## REST Endpoints

Export a package:

```bash
$ curl -X GET -H Content-Type: application/json -H Accept: application/json http://localhost:8080/site/custom-api/packages/blog-package/export -o package2.zip -v
```

Import a package:

```bash
$ curl -X PUT -F "file=@package2.zip;type=application/zip" http://localhost:8080/site/custom-api/packages/import -v
```

Create a package definition:

```bash
curl -X PUT -H "Content-Type: application/json" -d @test-package.json "http://localhost:8080/site/custom-api/packages/test-package2" -v
```

test-package.json

```json
{
     "filters" : ["/content/documents/"],
     "cnds": ["xinmods:basedocument", "xinmods:blogdocument"]
}
```

Change/Edit a package definition:

```bash
curl -X POST -H "Content-Type: application/json" -d @test-package.json "http://localhost:8080/site/custom-api/packages/test-package2" -v
```

Delete a package definition:

```bash
curl -X DELETE -H "Content-Type: application/json" "http://localhost:8080/site/custom-api/packages/test-package2" -v
```

List package definitions:

```bash
curl -X GET -H "Content-Type: application/json" "http://localhost:8080/site/custom-api/packages/list" -v
```


# XinMODS configuration 

Configure a module configuration here:

    /hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig
    
With allowed values:

    snsTopics : string[] -- a list of SNS topics to write hippo bus events to
    webhooks : string[] -- a list of webhooks to POST the JSON payload of the hippo bus event to.
    assetCacheLength: long -- the cache-control max-age=<d> value passed back to the requestee.
    
# Rigid Folders

Out of the box the `xinmods` namespace has a `xinmods:rigidfolder` mixin. If the common package has been installed
and a folder has been tagged with this mixin, authors will not be able to do any workflow actions on this folder. 

# Image Focus

After installing the XIN Mods Common package (from the docs/packages/ folder), your assets will get an additional
field that allows you to set the focal point of the image which will be stored on the image as additional properties. 

You could feed the additional focal values into the focalX and focalY cropping options shown below.

# Image Manipulation

XIN Mods enrich the standard CMS with a asset modification servlet found under `/assetmod`.

It allows you to scale, crop and apply filters to images in the DAM, and if configured properly from an external S3 source as well.
This functionality is commonly pushed behind a proper caching layer.

Example URLs:

* http://localhost:8080/site/binaries/content/gallery/images/image.jpg -- original binary
* http://localhost:8080/site/assetmod/binaries/content/gallery/images/image.jpg -- asset mod url without instructions
* http://localhost:8080/site/assetmod/crop=100,100/binaries/content/gallery/images/image.jpg -- crop to 100x100 on the center
* http://localhost:8080/site/assetmod/crop=100,100,<focalX>,<focalY>/binaries/content/gallery/images/image.jpg -- crop to 100x100 but focus on x,y in the image where x and y are numbers between -0.5 and 0.5
* http://localhost:8080/site/assetmod/scale=_,100/binaries/content/gallery/images/image.jpg -- scale image to 100 height
* http://localhost:8080/site/assetmod/scale=100,_/binaries/content/gallery/images/image.jpg -- scale image to 100 width
* http://localhost:8080/site/assetmod/scale=100,_/crop=50,50/binaries/content/gallery/images/image.jpg -- scale image to 100 width then crop to 50x50



# Integrating User Management with Auth0

To understand how to integrate Auth0 into your brXM instance, read through `docs/AUTH0_INTEGRATION.md`. It only takes
a few minutes to configure!

# Extension points

XINmods makes it easier to integrate external tools into the CMS by offering a number of easy to use integration points:

* Adding buttons to the document toolbar 
* Adding a plugin to the dashboard
* Adding an administrator panel
* Adding a reporting panel

What follows is a description of the configuration structures required to create these extension points. Once you have 
set them up, you can easily create a package to their paths that will allow you to reimport them when you desire.  

## Toolbar plugin

To the default workflow (here: `/hippo:configuration/hippo:workflows/default/handle/frontend:renderer`) add a node 
with the following structure.

```
toolbar-option1/
    jcr:primaryType = frontend:plugin
    wicket.id = ${item}
    plugin.class = nz.xinsolutions.extensions.XinInjectionToolbarPlugin
    
    # Where to go if the menu item is clicked
    action = http://url/to/go/to?path={path}
    
    # If specified, will show in submenu with that title
    submenu = SubMenuName
    
    title = Label on button
    
    icon = enumeration string from Icon class (default: GEAR)
    
    publishedOnly = true|false
    
    # Only show the option sometimes
    types[] = xinmods:product 

```

### Icon identifiers

For all possible values for the icons please read `docs/ICONS.md`.


## Content properties

The remaining extension points have a common approach to injecting content. Assume the different plugin types below
all share these properties. 

```
    html = 'HTML to render'
    js = 'Javascript to write on the page'
    headCSS[] = one or more urls to stylesheets (will be rendered in <head/>)
    headJS[] = one or more urls to javascripts (will be rendered in <head/>)

```


### Dashboard plugin

To add a widget to the dashboard add a node here: `/hippo:configuration/hippo:frontend/cms/cms-dashshortcuts`

Using the content properties described above create a node as follows:

```
    jcr:primaryType = frontend:plugin
    plugin.class = nz.xinsolutions.extensions.XinInjectionPlugin
```

### Admin panel

To add an administration panel add a node here: `/hippo:configuration/hippo:frontend/cms/cms-admin`

Using the content properties described above create a node as follows:

```
    jcr:primaryType = frontend:plugin
    plugin.class = nz.xinsolutions.extensions.XinInjectionAdminPanelDefinition
    
    title = Title of admin panel
    help = Description of admin panel
    icon = URL to icon for admin panel
```

### Reporting Panel

To add a reporting panel add two nodes here: `/hippo:configuration/hippo:frontend/cms/hippo-reports`

First of all a report definition node (eg. `my-new-report`) -- it does not need any of the injection properties:

```
    jcr:primaryType = frontend:plugin
    plugin.class = nz.xinsolutions.extensions.XinInjectionReportDefinition
    
    service.id = service.report.my-new-report
    
    title = Title of reporting panel
    help = Description of reporting panel
    icon = URL to icon for reporting panel  
```

Secondly a report panel plugin node (eg. `my-new-report-plugin`) -- this one contains the injection properties.

```
    jcr:primaryType = frontend:plugin
    plugin.class = nz.xinsolutions.extensions.nz.xinsolutions.extensions.XinInjectionReportPlugin
    wicket.id = service.report.my-new-report
    
    html = <something to inject>
    js = 
    headJS =
    headCSS = 
    
```

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

JWTs obtained through the `/cms/ws/jwt` endpoint can be sent through to the `/api` and `/custom-api` endpoints as
bearer tokens in the `Authorization` header.

    Authorization: Bearer <jwt>

The token will be checked against the `jwks.json` specified in the configuration. By default, if the JWT validation was
successful, a JCR session is opened as `admin`. If you do not want that, you can provide an alternative set of credentials 
in the deployment configuration. 

    ..
    "xin" : { 
        "cmsHost": "https://<your_cms_deployment_url>",
        "adminPass": "",
        "jwksUrl": "https://<your_cms_deployment_url>/cms/ws/jwks.json",
        "jwtRepoUser": "jwt_user", 
        "jwtRepoPassword": ""
    } 
    ..

As with normal Basic authentication for the API endpoints, only users either `admin` or contained within the
`restapi` group are allowed to access the endpoints.  

To successfully retrieve the JWT from the /cms/ws/jwt endpoint, one mustt be sure to include
the `?source=http://<yourdomain>` parameter to allow the `Access-Control-Allow-Origin` parameter
to be setup appropriately. Also, make sure the XHR has `withCredentials` enabled so that the session
cookies are being sent along to the JWT endpoint.

## Integration scenario

What follows is an example of how a 3rd party webapp can integrate into Bloomreach using JWTs: 

* Using the XINmods dashboard extensions create a new entry point into your external code;
* When a user visits the the dashboard button, the iframe will invoke your application;
* The application is aware of where the CMS is located and will invoke the `/cms/ws/jwt` endpoint to obtain a JWT;
* It can then use regular HTTP calls (preferably from a backend) or the `xinmods` NPM module (as an abstraction) to query the CMS's repository.
* Make sure to regularly fetch a new token so that it does not expire (the default expiration is set to 1 hour). 
* The claims of the JWT could be inspected to determine their level of access to the third party integration by restricting functionality based on group membership. 


# Collections

To understand how you can use XIN Collections to interact with the repository to store, retrieve and query small
documents read through `docs/COLLECTIONS.md`.

# Queries

To understand how to use the queries endpoints read `docs/QUERIES.md`.

## Other endpoints

Other useful REST endpoints that are available to you are:

* `/site/custom-api/content/path-to-uuid/?path=/jcr/path`: Outputs the UUID of a document on a specific path, its type 
and the UUIDs of its immediate children.
* `/site/api/documents/{uuid}`: Outputs a very complete interpretation of the bean at that UUID (commonly used to get
 more details about the query results' UUIDs), more information to be found here: 
 https://www.onehippo.org/library/concepts/rest/content-rest-api/document-detail-resource.html.
* `/site/api/documents`: Simple document filtering described here:
https://www.onehippo.org/library/concepts/rest/content-rest-api/document-collection-resource.html.



# Running locally

```bash
mvn -DskipTests=true verify && mvn -Pcargo.run -Drepo.path=storage
```

Clean everything without content:

```bash
rm storage -Rf && mvn -DskipTests=true clean verify && mvn -Pcargo.run,without-content -Drepo.path=storage
```
