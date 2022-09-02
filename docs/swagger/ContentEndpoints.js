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
     * Get Documents
     *
     * @id getDocuments
     * @tag Content
     * @summary Get documents
     *
     * @response 200 {object} The list of documents
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
     * List Documents
     *
     * @id listDocuments
     * @tag Content
     *
     * @summary List documents
     *
     * @response 200 {object} the list of documents and folders
     *
     * @param req
     * @param resp
     */
    listDocuments(req, resp) {},

    /**
     * @swagger
     *
     * UUID to path
     *
     * @id uuidToPath
     * @tag Content
     *
     * @summary UUID to path
     *
     * @response 200 {object} the list of documents and folders
     *
     * @param req
     * @param resp
     */
    uuidToPath(req, resp) {},

    /**
     * @swagger
     *
     * UUID to path
     *
     * @id pathToUuid
     * @tag Content
     *
     * @summary Path To UUID
     *
     * @response 200 {object} the list of documents and folders
     *
     * @param req
     * @param resp
     */
    pathToUuid(req, resp) {},

}
