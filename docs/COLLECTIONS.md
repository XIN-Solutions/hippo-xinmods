
# Collections

A common use-case for web applications is saving small bits of information (think of
user profile information, contact form values etc.), so a mechanism to push information
into the CMS would be very useful.

With that in mind, Collections have been introduced to XIN Mods. After installing the
XIN Mods Common package, you will notice a new `hippostd:folder` at `/content/collections`.
This is where new collections can be added.

A collection is container of folders, and `jcr:content` handle nodes with a single always-live
`jcr:content` node underneath of primary type: `xinmods:collectionitem`.

Collections cannot be created through the endpoints, you will have manually create a new
`hippostd:folder` node directly underneath the `collections` base folder.

Endpoints that now are available are:

* GET: `/site/custom-api/collections/list`; provides a list of collections currently available
* GET: `/site/custom-api/collections/{collectionName}/item?path=...`; retrieves the JSON representation
  of an item at path `...`
* DELETE: `/site/custom-api/collections/{collectionName}/item?path=...&forceDelete=true/false`; removes
  an item from the tree. If `forceDelete` is set to false, then only leaf items can be removed, if set
  to true, parts of the collection tree can be removed all at once.
* POST: `/site/custom-api/collections/{collectionName}/item?path=...` this endpoint allows you
  push new content items into the collection. If the folders of the path do not exist yet, they are
  created on the fly. The body of the POST request is a JSON object shaped as follows:


     {
         "saveMode":"Overwrite|Merge|IfNotExists",
         "values": {
             "story": {
                 "value" : "The story just goes on and on",
                 "type": "String"
             },
             
             "summary": {
                 "value" : "This is a summayr of the story that keeps going.",
                 "type": "String"
             },
             
             "age": {
                 "value": "10",
                 "type": "Long"
             },
             
             "date": {
                 "value": "2021-02-18T11:08:18.450+13:00",
                 "type": "Date"
             }
     
         }
     }

The following types are supported: `Boolean`, `String`, `Long`, `Double`, `Date`.

Savemodes come in handy when you wish to exert control over how information is
pushed into the repostiory.

* If SaveMode is set to `Overwrite`, all existing content at the path is replaced.
* If set to `Merge`, then existing content is retained; existing fields with new values
  are overwritten, and new fields are merged into the existing document;
* If set to `IfNotExists`, then only if no such item exists yet, the item content is added.

All these endpoints are available through an abstraction in the `xinmods` Javascript wrapper. 