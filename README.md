# Packages

xinMODS adds simple package management to the interface allowing you to add functional modules to a bare-bones CMS deployment 
without redeployments or restarting. 

On your instance go to this URL: http://localhost:8080/site/packages/list.html

It shows you an interface to a set of endpoints described loosely below.

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
    
    # Only show the option sometimes
    types[] = xinmods:product 

```

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

# Running locally

```bash
mvn -DskipTests=true verify && mvn -Pcargo.run -Drepo.path=storage
```

Clean everything without content:

```bash
rm storage -Rf && mvn -DskipTests=true clean verify && mvn -Pcargo.run,without-content -Drepo.path=storage
```
