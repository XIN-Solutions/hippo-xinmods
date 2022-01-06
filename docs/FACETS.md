# Faceted Navigation Retrieval endpoint

An endpoint was created to extract faceted navigation results from the brXM repository. 

To invoke the endpoint:

    http://localhost:8080/site/custom-api/facets/get

or

    https://deployment.com/api/xin/facets/get

The query parameters accepted by this endpoint are:

* `facetPath`; the path of the facet that we wish to query (required)
* `childPath`; the facet navigation element inside the selected facet
* `offset`; at which result to start returning elements (pagination) -- default: `0`
* `limit`; how many elements to return (pagination) -- default: `50`
* `sorted`; boolean that indicates whether to use the sort behaviour specified in the facet definition
* `fetch`; a list of xpath expressions that allow you to prefetch content. 

An example of the fetch command would be: `images/*/link` which would retrieve the referenced
image beans for something structured like this:

```
    "results": [
      {
        "id": "dcee47ea-db87-4d7b-a8de-e7722d015400",
        "name": "test-product",
        "displayName": "Test Product",
        "path": "/content/facets/specs/Carrier Signals/4G/Dual SIM?/hippo:resultset",
        "type": "xinmods:product",
        "locale": "document-type-locale",
        "pubState": "published",
        "pubwfCreationDate": "2021-12-05T00:23:37.078+13:00",
        "pubwfLastModificationDate": "2022-01-06T12:55:01.185+13:00",
        "pubwfPublicationDate": "2022-01-06T12:55:02.721+13:00",
        "items": {
          "xinmods:discountPrice": 599.99,
          "xinmods:name": "Samsung Galaxy A52s (2021)",
          "xinmods:summary": "Samsung Galaxy A52s (2021) 5G Dual SIM Smartphone",
          "xinmods:sellingPoints": [
            "Great value",
            "Great quality"
          ],
          "xinmods:price": 698.99,
          "xinmods:images": [
            {
              "type": "hippogallerypicker:imagelink",
              "link": {
                "type": "local",
                "id": "fcffc3e1-34ab-4f8a-a93a-bfe057dd0904",
                "url": "http://localhost:8080/site/custom-api/documents/fcffc3e1-34ab-4f8a-a93a-bfe057dd0904",
```

Example response:

```
{
  "success": true,
  "message": "Success.",
  "facet": {
    "sourceFacet": "/content/facets/specs",
    "facetPath": "Carrier Signals/4G/Dual SIM?",
    "displayName": "Dual SIM?",
    "childFacets": {
      "Yes": 1
    },
    "results": [
      ...
    ]
  }
}
      
```