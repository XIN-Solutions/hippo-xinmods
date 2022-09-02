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
