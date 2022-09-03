/**
 * @typedef ApiResponse
 *
 * @property {boolean} success - `true` indicates the operation was successful.
 * @property {string} message - the message describing the state of the response.
 */

/**
 * @typedef {ApiResponse} DocumentWithUuidResponse
 *
 * @property {?BloomreachDocument} document - the document that is present at the UUID
 */

/**
 * @typedef {ApiResponse} UuidToPathResponse
 *
 * @property {string} uuid - the UUID that was originally requested
 * @property {string} type - the CND type of the document this UUID belongs to
 * @property {string} path - the path at which the document with this UUID lives.
 */

/**
 * @typedef {UuidToPathResponse} PathToUuidResponse
 */

/**
 * @typedef {ApiResponse} ListDocumentsResponse
 *
 * @property {string} uuid - the uuid of the node at the path requested
 * @property {string} path - the path as requested in the request
 * @property {string} name - the name of the node
 * @property {string} label - the display name of the node
 * @property {BloomreachFolder[]} folders - a list of folders that are available at this path
 * @property {BloomreachDocument[]} documents - a list of child documents available at the requested path
 */

/**
 * @typedef GetDocumentsResponse
 *
 * @property {number} offset - the offset for the result set
 * @property {number} max - the limit to the number of records to be returned
 * @property {number} count - the number of items returned
 * @property {number} total - total number of results (includes results outside the current offset/max viewport)
 * @property {boolean} more - if true there are more results to be retrieved in a next page.
 * @property {BloomreachGetDocumentItem} items - a list of result items.
 */

/**
 * @typedef GetDocumentResponse
 *
 */


/**
 * @typedef BloomreachDocument
 *
 * @property {string} id - the UUID of the document
 * @property {string} name - the node name of the document
 * @property {string} displayName - the human-readable label of the document
 * @property {string} path - the resolved path at which the document lives in the JCR
 * @property {string} type - the CND type of the document that has been retrieved
 * @property {string} locale - the document locale
 * @property {string} pubState - 'published' (unpublished documents are not exposed in the API)
 * @property {string} pubwfCreationDate - publication workflow creation ISO date string indicating when the document was created.
 * @property {string} pubwfLastModificationDate - publication workflow modification date formatted as an ISO date string.
 * @property {string} pubwfPublicationDate - publication workflow publication ISO date string indicating when the document was first published
 * @property {object} items - object describing the values stored in the document, this structure will be different for every document type.
 */

/**
 * @typedef BloomreachFolder
 *
 * @property {string} uuid - the UUID of the folder in the repository
 * @property {string} path - the absolute path of the folder in the repository
 * @property {string} name - the node name of the folder in the repository
 * @property {string} label - the display name of the folder in the repository
 */

/**
 * @typedef BloomreachGetDocumentItem
 *
 * @property {string} name - the name of the node
 * @property {string} id - the uuid of the node
 * @property {BloomreachLink} link - the link to the document
 * @property {string} type - the CND type of this node
 *
 */

/**
 * @typedef BloomreachLink
 * @property {string} type - 'local' to signify the document is in the repository
 * @property {string} id - the uuid of the node
 * @property {string} url - a URL that can retrieve this specific document
 *
 */
