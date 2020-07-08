# Query Endpoint

Part of the CaaS aspect of the XinMod additions are the ability to query the content by using the custom interpreter
built for the HST query variants used in Hippo. A typical query could look something like this:

This document describes how to call the query endpoint.

## Example queries


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
                    (> [mods:price] $minPrice)
                    (<= [mods:price] $maxPrice)
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

Queries can also contain variables `$varName` notation. When encountered, the value of the query parameter with that same 
name will be inserted at that location.

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
