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
    
    publishedOnly = true|false
    
    # Only show the option sometimes
    types[] = xinmods:product 

```

### Icon identifiers

* ARROW_DOWN
* ARROW_FAT_DOWN_CIRCLE
* ARROW_UP
* ARROW_RIGHT_SQUARE
* BELL
* BULLET
* BULLHORN
* CALENDAR_DAY
* CALENDAR_MONTH
* CARET_DOWN
* CARET_DOWN_CIRCLE
* CARET_RIGHT
* CARET_UP_CIRCLE
* CHECK_CIRCLE
* CHECK_CIRCLE_CLOCK
* CHECK_SQUARE
* CHEVRON_DOWN_CIRCLE
* CHEVRON_DOWN
* CHEVRON_LEFT_CIRCLE
* CHEVRON_LEFT
* CHEVRON_RIGHT_CIRCLE
* CHEVRON_RIGHT
* CHEVRON_UP_CIRCLE
* CHEVRON_UP
* CODE
* COMPONENT
* COMPRESS
* CROP
* EMPTY
* EXCLAMATION_CIRCLE
* EXCLAMATION
* EXCLAMATION_TRIANGLE
* EXPAND
* FILE_COMPOUND
* FILE_IMAGE
* FILE
* FILE_NEWS
* FILE_PENCIL
* FILE_TEXT
* FILES
* FLASK
* FLOPPY
* FOLDER
* FOLDER_OPEN
* FONT
* FORWARD
* GEAR
* GLOBE_ABSTRACT
* GLOBE
* INFO_CIRCLE
* INFO
* LINK
* LIST_UL
* LOCKED
* MIMETYPE_AUDIO
* MIMETYPE_BINARY
* MIMETYPE_DOC
* MIMETYPE_DOCX
* MIMETYPE_FLASH
* MIMETYPE_IMAGE
* MIMETYPE_ODP
* MIMETYPE_ODS
* MIMETYPE_ODT
* MIMETYPE_PDF
* MIMETYPE_PPT
* MIMETYPE_PPTX
* MIMETYPE_RTF
* MIMETYPE_SXC
* MIMETYPE_SXI
* MIMETYPE_SXW
* MIMETYPE_TEXT
* MIMETYPE_VIDEO
* MIMETYPE_XLS
* MIMETYPE_XLSX
* MIMETYPE_ZIP
* MINUS_CIRCLE
* MINUS_CIRCLE_CLOCK
* MOVE_INTO
* PENCIL_SQUARE
* PIE_CHART
* PLUS
* PLUS_SQUARE
* REFRESH
* RESTORE
* SEARCH
* SORT
* STEP_BACKWARD
* STEP_FORWARD
* THUMBNAILS
* TIMES
* TIMES_CIRCLE
* TRANSLATE
* TYPE
* UNLINK
* UNLOCKED
* USER_CIRCLE

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

# Queries

Part of the CaaS aspect of the XinMod additions are the ability to query the content by using the custom interpreter
built for the HST query variants used in Hippo. A typical query could look something like this:

```
    (query
        (type with-subtypes 'xinmods:page')
        (offset 100)
        (limit 10)
        (scopes
            (include '/content/documents/xin')
            (include '/content/documents/configuration')
            (exclude '/content/documents/xin/secret')
        )
        (where
            (and
                (= [mods:onsale] true)
                (or
                    (> [mods:price] 100)
                    (<= [mods:price] 1000)
                )
            )
        )
        (sortby [xinmods:publishedDate] desc)
    )
```

And is sent to the following GET endpoint `/site/custom-api/content/query?query=...` where `query` request parameter contains
an encoded version of the query description above. 

It returns a list of UUIDs of hippo documents that adhere to the query as per its parameters. These UUIDs are to be fed
into the `/site/api/documents/{uuid}` GET endpoint.

## Operators

Binary operators:

* `contains`: property contains value X (contains 'X')
* `!contains`: does not contain
* `>`: greater than
* `<`: smaller than
* `>=`: greater than or equal to
* `<=`: smaller than or equal to
* `=`: equals
* `!=`: does not equal
* `i=`: equals (case insensitive)
* `i!=`: does not equal (case insensitive)

Unary operators:

* `null`: property is null
* `notnull`: property is not null

Compound operators:

* `(and X Y)`: X and Y must both be true
* `(or X Y)`: X or Y must be true

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
