package nz.xinsolutions.rest;

import nz.xinsolutions.beans.ContextVariablesBean;
import nz.xinsolutions.core.Rest;
import nz.xinsolutions.queries.QueryParser;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This controller contains a number of REST actions that allow dynamic queries of the JCR.
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/content/")
public class ContentQueryResource extends BaseRestResource implements Rest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentQueryResource.class);

    /**
     * Keys for JSON output
     */

    public static final String KEY_UUID = "uuid";
    public static final String KEY_PATH = "path";
    public static final String KEY_URL = "url";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TYPE = "type";
    public static final String KEY_FOLDERS = "folders";
    public static final String KEY_UUIDS = "uuids";
    public static final String KEY_TOTAL_SIZE = "totalSize";
    public static final String KEY_NAME = "name";
    public static final String KEY_LABEL = "label";
    public static final String KEY_DOCUMENTS = "documents";

    /**
     * This action ingests a query. The query structure is outlined in {@link docs/QUERIES.md}. It outputs a response
     * that contains, uuids, paths and the API url to go looking for more details.
     *
     * @param uriInfo   query parameters for variable ingestion are in here
     * @param request   is the http servlet request
     * @param query     is the query parameter
     * @return a JSON object with the output of the query
     */
    @GET
    @Path("/query/")
    public Response performQuery(@Context UriInfo uriInfo, @Context HttpServletRequest request, @QueryParam(value = "query") String query) {
    
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    
        RestContext restCtx = newRestContext(this, request);
        ContextVariablesBean ctxVars = newContextVariablesInstance(request);

        try {
            if (StringUtils.isEmpty(query)) {
                throw new IllegalArgumentException("`query` parameter is empty");
            }

            LOG.info("Received Query Request: " + query);

            HstQueryManager qMgr = restCtx.getRequestContext().getQueryManager();
            HstQuery hstQuery = getQueryParserInstance().createFromString(qMgr, query, queryParams);
            
            HstQueryResult queryResult = hstQuery.execute();
            int totalItems = queryResult.getTotalSize();

            // uuid lists
            List<HippoBean> beans = new ArrayList<>();
            HippoBeanIterator it = queryResult.getHippoBeans();
            while (it.hasNext()) {
                HippoBean bean = it.nextHippoBean();
                beans.add(bean);
            }
            
            // respond
            return
                Response.status(200)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put(KEY_SUCCESS, true);
                            put(KEY_MESSAGE, beans.size() > 0 ? "Result found" : "No result found");
                            put(KEY_UUIDS, convertToResponseObject(beans, ctxVars.getApiUrl()));
                            put(KEY_TOTAL_SIZE, totalItems);
                        }}
                    )
                    .build()
                ;
        }
        catch (Exception ex) {
        
            LOG.error("Could not parse a query properly, caused by: ", ex);

            return
                Response.status(501)
                    .entity(
                        new LinkedHashMap<String, Object>() {{
                            put(KEY_SUCCESS, false);
                            put(KEY_MESSAGE, "Error: " + ex.getMessage());
                        }}
                    ).build()
            ;
        }
        
    }

    @GET
    @Path("/documents-list/")
    public Response foldersAtPath(@Context HttpServletRequest request, @QueryParam(value = KEY_PATH) String path) {

        RestContext ctx = newRestContext(this, request);
        ContextVariablesBean ctxVars = newContextVariablesInstance(request);

        try {
            if (StringUtils.isEmpty(path)) {
                LOG.info("The path is empty");
                return null;
            }

            HippoBean bean = (HippoBean) ctx.getRequestContext().getObjectBeanManager().getObject(path);

            if (bean == null) {
                LOG.error("Cannot find bean at folder of `{}`", path);
                return notFoundResponse();
            }

            if (!(bean instanceof HippoFolder)) {
                LOG.error("Not a proper folder path");
                return notFoundResponse();
            }

            HippoFolder folder = (HippoFolder) bean;

            List<Map> childFolders = (
                    folder.getFolders()
                        .stream()
                        .map(childFolder -> new LinkedHashMap<String, String>() {{
                            put(KEY_UUID, childFolder.getCanonicalUUID());
                            put(KEY_PATH, childFolder.getPath());
                            put(KEY_NAME, childFolder.getName());
                            put(KEY_LABEL, childFolder.getDisplayName());
                        }})
                        .collect(Collectors.toList())
                    );


            List<Map> childDocuments =
                    folder.getDocuments()
                        .stream()
                        .map(childDoc -> new LinkedHashMap<String, String>() {{
                            put(KEY_UUID, childDoc.getCanonicalHandleUUID());
                            put(KEY_PATH, childDoc.getPath());
                            put(KEY_NAME, childDoc.getName());
                        }})
                        .collect(Collectors.toList())
                    ;

            Map<String, Object> result = new LinkedHashMap<String, Object>() {{

                put(KEY_SUCCESS, true);
                put(KEY_MESSAGE, "Found.");

                put(KEY_UUID, bean.getCanonicalUUID());
                put(KEY_PATH, path);
                put(KEY_FOLDERS, childFolders);
                put(KEY_DOCUMENTS, childDocuments);

            }};

            return (
                Response
                    .status(200)
                    .entity(result)
                    .build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something happened while retrieving paths at `" + path + "`, caused by: ", ex);
            return null;
        }

    }


    /**
     * Convert a node path to a uuid for use in the documents API.
     *
     * @param request   is the incoming request
     * @param uuid      is the path we're interested in converting.
     * @return a response object with relevant status messages
     */
    @GET
    @Path("/uuid-to-path/")
    public Response uuidToPath(@Context HttpServletRequest request, @QueryParam(value = KEY_UUID) String uuid) {

        RestContext ctx = newRestContext(this, request);
        ContextVariablesBean ctxVars = newContextVariablesInstance(request);

        try {
            if (StringUtils.isEmpty(uuid)) {
                LOG.info("The UUID is empty");
                return null;
            }

            HippoBean bean = (HippoBean) ctx.getRequestContext().getObjectBeanManager().getObjectByUuid(uuid);

            if (bean == null) {
                return notFoundResponse();
            }

            Map<String, Object> result = new LinkedHashMap<String, Object>() {{

                put(KEY_SUCCESS, true);
                put(KEY_MESSAGE, "Found.");

                put(KEY_UUID, uuid);
                put(KEY_TYPE, bean.getNode().getPrimaryNodeType().getName());

                if (bean instanceof HippoDocument) {
                    put(KEY_PATH, ((HippoDocument) bean).getCanonicalHandlePath());
                } else {
                    put(KEY_PATH, bean.getPath());
                }
            }};

            return (
                Response
                    .status(200)
                    .entity(result)
                    .build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something happened while retrieving object at `" + uuid + "`, caused by: ", ex);
            return null;
        }
    }


    /**
     * Convert a node path to a uuid for use in the documents API.
     *
     * @param request   is the incoming request
     * @param path      is the path we're interested in converting.
     * @return a response object with relevant status messages
     */
    @GET
    @Path("/path-to-uuid/")
    public Response pathToUuid(@Context HttpServletRequest request, @QueryParam(value = KEY_PATH) String path) {
    
        RestContext ctx = newRestContext(this, request);
        ContextVariablesBean ctxVars = newContextVariablesInstance(request);

        try {
            if (StringUtils.isEmpty(path)) {
                LOG.info("The path is empty");
                return null;
            }

            HippoBean bean = (HippoBean) ctx.getRequestContext().getObjectBeanManager().getObject(path);

            if (bean == null) {
                return notFoundResponse();
            }

            Map<String, Object> result = new LinkedHashMap<String, Object>() {{

                put(KEY_SUCCESS, true);
                put(KEY_MESSAGE, "Found.");

                put(KEY_PATH, path);
                put(KEY_TYPE, bean.getNode().getPrimaryNodeType().getName());

                if (bean instanceof HippoDocument) {
                    put(KEY_UUID, ((HippoDocument) bean).getCanonicalHandleUUID());
                } else {
                    put(KEY_UUID, bean.getCanonicalUUID());
                }
            }};

            return (
                Response
                    .status(200)
                    .entity(result)
                    .build()
            );
        }
        catch (Exception ex) {
            LOG.error("Something happened while retrieving object at `" + path + "`, caused by: ", ex);
            return null;
        }
    }

    /**
     * @return a new instance of the context variables bean
     */
    public ContextVariablesBean newContextVariablesInstance(HttpServletRequest request) {
        return new ContextVariablesBean(request);
    }

    /**
     * Convert a list of hippo beans to JSON
     *
     * @param childBeans beans to convert.
     * @param apiUrl the base url
     * @return a json-ready list of maps from the childbeans passed in
     */
    public List<Map<String, String>> convertToResponseObject(List<HippoBean> childBeans, String apiUrl) {
        return childBeans
                .stream()
                .map(it -> beanToJson(apiUrl, it))
                .collect(Collectors.toList());
    }

    /**
     * @return a structure describing a bean
     */
    protected Map<String, String> beanToJson(String urlBase, HippoBean bean) {
        try {
            return new LinkedHashMap<String, String>() {{
                if (bean instanceof HippoDocument) {
                    HippoDocument doc = (HippoDocument) bean;
                    put(KEY_UUID, doc.getCanonicalHandleUUID());
                    put(KEY_PATH, doc.getCanonicalHandlePath());
                    put(KEY_URL, getApiPath(urlBase, doc.getCanonicalHandleUUID()));
                } else {
                    put(KEY_UUID, bean.getCanonicalUUID());
                    put(KEY_PATH, bean.getCanonicalPath());
                    put(KEY_URL, getApiPath(urlBase, bean.getCanonicalUUID()));
                }
                put(KEY_TYPE, bean.getNode().getPrimaryNodeType().getName());
            }};
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not get information from " + bean.getPath());
        }
        return null;
    }

    /**
     * @return the api path for a specific uuid
     */
    protected String getApiPath(String urlBase, String uuid) {
        return String.format("%s/documents/%s", urlBase, uuid);
    }


    /**
     * @return the not found response
     */
    protected Response notFoundResponse() {
        return (
            Response
                .status(200)
                .entity(new LinkedHashMap<String, Object>() {{
                    put("success", false);
                    put(KEY_MESSAGE, "Not found");
                }})
                .build()
        );
    }


    /**
     * @return the query parser instance
     */
    protected QueryParser getQueryParserInstance() {
        return new QueryParser();
    }

}
