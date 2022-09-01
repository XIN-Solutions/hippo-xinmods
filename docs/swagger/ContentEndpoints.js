module.exports = {


    /**
     * @swagger
     *
     * <p>Execute a query that returns a set of documents.</p>
     * <p>Can we do multiple lines?</p>
     *
     * @id executeQuery
     * @tag Content
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
     * @tag Content
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
     * @response 200 {object} the document with a particular UUID
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
     * @summary Get document at Path
     *
     * @response 200 {object} the document at that path
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
