module.exports = {


    /**
     * @swagger
     *
     * <p>Execute a query that returns a set of documents.</p>
     * <p>Can we do multiple lines?</p>
     *
     * @id executeQuery
     * @tag Complex Content
     * @summary Execute a query that returns a set of documents.
     *
     * @response 200 {object} The Collections API List response.
     *
     * @param req
     * @param resp
     */
    executeQuery(req, resp) {},


    /**
     * @swagger
     *
     * Get document is an endpoint built-in to Bloomreach XM, it allows you to query for
     * a single document by its UUID. While you're free to use this endpoint, it is recommended to
     * use the XIN Mods `Get document at path` endpoint as it allows prefetching.
     *
     * @id ootbGetDocument
     * @tag Content
     * @summary Get document by ID (legacy)
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param uuid {string} (path) the UUID of the document you want to retrieve.
     *
     * @response 200 {BloomreachDocument} The list of documents
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    getDocument(req, resp) {},


    /**
     * @swagger
     *
     * Get documents is an endpoint built-in to Bloomreach XM, it allows you to query for
     * documents in quite the unrefined manner.
     *
     * @id ootbGetDocuments
     * @tag Content
     * @summary Get filtered documents
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param _offset {?number} (query) pagination offset
     * @param _max {?number} (query) the maximum number of records to be retrieved
     * @param _orderBy {?string} (query) the field to order by (eg, hippostd:pubwfCreationDate)
     * @param _sortOrder {?string} (query) 'asc' or 'desc' for ascending and descending sort order respectively
     * @param _nodetype {?string} (query) the CND type to look for (like xinmods:blogdocument)
     * @param _query {?string} (query) the free text query.
     *
     * @response 200 {GetDocumentsResponse} The list of documents
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    getDocuments(req, resp) {},


    /**
     * @swagger
     *
     * Get facet at path
     *
     * @id getFacetAtPath
     * @tag Complex Content
     * @summary Get Facet at path
     *
     * @response 200 {object} The Facet information
     *
     * @param req
     * @param resp
     */
    getFacetAtPath(req, resp) {},


    /**
     * @swagger
     *
     * Get document with UUID
     *
     * @id getDocumentWithUuid
     * @tag Content
     *
     * @summary Get document with UUID
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param uuid {string} (query) the unique identifier of the document you wish to retrieve.
     * @param fetch {?string[]} (query) partial paths to items in the payload that must be <a href="https://marnixkok.nl/news/bloomreach-xm-tutorials/prefetching-content-from-bloomreach-xm-using-xin-mods" target="_blank">prefetched</a>.
     *
     * @response 200 {DocumentWithUuidResponse} the document with a particular UUID
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    getDocumentWithUuid(req, resp) {},


    /**
     * @swagger
     *
     * Get document at path
     *
     * @id getDocumentAtPath
     * @tag Content
     *
     * @summary Get document at path
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param path {string} (query) the path of the document you wish to retrieve.
     * @param fetch {?string[]} (query) partial paths to items in the payload that must be <a href="https://marnixkok.nl/news/bloomreach-xm-tutorials/prefetching-content-from-bloomreach-xm-using-xin-mods" target="_blank">prefetched</a>.
     *
     * @response 200 {DocumentWithUuidResponse} the document with a particular UUID
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    getDocumentAtPath(req, resp) {},


    /**
     * @swagger
     *
     * This endpoint enables you to retrieve a list of sub-folders and sub-documents
     * of a specific path in your content tree.
     *
     * @id listDocuments
     * @tag Content
     *
     * @summary List folders and documents
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param path {string} (query) the path to retrieve child document information for
     * @param fetch {?string[]} (query) partial paths to items in the payload that must be <a href="https://marnixkok.nl/news/bloomreach-xm-tutorials/prefetching-content-from-bloomreach-xm-using-xin-mods" target="_blank">prefetched</a>.
     *
     * @response 200 {ListDocumentsResponse} the list of documents and folders
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    listDocuments(req, resp) {},

    /**
     * @swagger
     *
     * This endpoint is able to convert a UUID to the Path at which the document
     * with that UUID lives in the JCR. It will also return other useful metadata.
     *
     * @id uuidToPath
     * @tag Content
     *
     * @summary UUID to path converter
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param uuid {string} (query) the UUID to convert to its path equivalent
     *
     * @response 200 {UuidToPathResponse} the list of documents and folders
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    uuidToPath(req, resp) {},

    /**
     * @swagger
     *
     * This endpoint is able to convert a document path to the UUID of the document.
     * It will also return other useful metadata.
     *
     * @id pathToUuid
     * @tag Content
     *
     * @summary Path To UUID Converter
     *
     * @param Authorization {string} (header) Basic authorization header for user in the `restapi` group or `admin` user
     * @param path {string} (query) the path of the document to retrieve a UUID for.
     *
     * @response 200 {PathToUuidResponse} the list of documents and folders
     * @response 403 {string} Sent when the credentials are invalid.
     *
     * @param req
     * @param resp
     */
    pathToUuid(req, resp) {},

}
