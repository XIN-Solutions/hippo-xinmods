package nz.xinsolutions.beans;

import nz.xinsolutions.core.security.BasicAuthUtility;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple bean that is able to read some environment related variables
 */
public class ContextVariablesBean {

    public static final String KEY_API_URL = "apiUrl";
    public static final String KEY_XIN_API_URL = "xinApiUrl";
    /**
     * Request
     */
    private HttpServletRequest request;

    /**
     * Initialise data-members
     */
    public ContextVariablesBean(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * @return the api url
     */
    public String getApiUrl() {
        return getParamOrDefault(KEY_API_URL, "/site/api");
    }

    /**
     * @return the xin custom api url
     */
    public String getXinApiUrl() {
        return getParamOrDefault(KEY_XIN_API_URL, "/site/custom-api");
    }

    /**
     * @return the authentication header so we can ship it along to the API
     */
    public String getApiAuthenticationHeader() {
        return BasicAuthUtility.getAuthorizationHeader(request);
    }

    /**
     * @return a context initialisation parameter is returned or defaultValue when not found.
     */
    protected String getParamOrDefault(String key, String defaultValue) {

        String value = request.getServletContext().getInitParameter(key);

        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }
}
