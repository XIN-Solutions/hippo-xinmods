const fs = require('fs');
const express = require('express');
const SwaggerScrape = require("swagger-scrape");
const DEFAULT_PORT = process.env.APP_PORT || 8080;

const pkgInfo = require('./package.json');

const appInfo = {
    version: pkgInfo.version,
    title: pkgInfo.name,
    description: fs.readFileSync('description.html').toString(),

    common: [ "Models.js" ]
};

const app = express();


app.get("/api/xin/content/document-with-uuid", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::getDocumentWithUuid; */
});

app.get("/api/xin/content/document-at-path", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::getDocumentAtPath; */
});

app.get("/api/xin/content/uuid-to-path", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::uuidToPath; */
});

app.get("/api/xin/content/path-to-uuid", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::pathToUuid; */
})


app.get("/api/xin/content/documents-list", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::listDocuments; */
});


app.get("/api/documents", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::getDocuments; */
});


app.get("/api/xin/facets/get", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::getFacetAtPath; */
});


app.get("/api/xin/query", (req, resp) => {
    /** @fileHint: ContentEndpoints.js::executeQuery; */
});


app.get("/api/xin/collections/list", (req, resp) => {
    /** @fileHint: CollectionEndpoints.js::listCollections; */
});


app.get("/api/xin/collections/:name/item", (req, resp) => {
    /** @fileHint: CollectionEndpoints.js::collectionGet; */
});

app.delete("/api/xin/collections/:name/item", (req, resp) => {
    /** @fileHint: CollectionEndpoints.js::collectionDelete; */
});


app.post("/api/xin/collections/:name/item", (req, resp) => {
    /** @fileHint: CollectionEndpoints.js::collectionPut; */
});

app.get("/assetmod/:url", (req, resp) => {
    /** @fileHint: AssetModEndpoints.js::assetMod; */
});



const swaggerHost = process.env.SWAGGER_HOST || ('localhost:' + DEFAULT_PORT);
const swaggerEndpoints = SwaggerScrape.scrapeEndpoints("./", app);
const swaggerDoc = SwaggerScrape.toSwaggerJson(appInfo, `${swaggerHost}`, "/", swaggerEndpoints);

console.log(JSON.stringify(swaggerDoc, null, 4));
