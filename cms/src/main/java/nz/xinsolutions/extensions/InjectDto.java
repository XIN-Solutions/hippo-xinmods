package nz.xinsolutions.extensions;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.CssUrlReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptUrlReferenceHeaderItem;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * Date: 4/02/18
 */
public class InjectDto {
    
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(InjectDto.class);
    
    /**
     * Constants
     */
    
    public static final String KEY_JS = "js";
    public static final String KEY_HTML = "html";
    public static final String KEY_HEAD_JS = "headJS";
    public static final String KEY_HEAD_CSS = "headCSS";
    
    /**
     * HTML
     */
    private String html;
    
    /**
     * Javascript to write
     */
    private String js;
    
    /**
     * Adding this to the Header
     */
    private String[] headJS = new String[0];
    
    /**
     * Adding this to the css
     */
    private String[] headCSS = new String[0];
    
    /**
     * Create a dto from a plugin configuration instance
     *
     * @param config    configuration to use
     * @return dto
     */
    public static InjectDto fromConfig(IPluginConfig config) {
    
        InjectDto dto = new InjectDto();
        
        if (isStringConfig(config, KEY_HTML)) {
            dto.html = (String) config.get(KEY_HTML);
        }
    
        if (isStringConfig(config, KEY_JS)) {
            dto.js = (String) config.get(KEY_JS);
        }
    
        if (isStringArrayConfig(config, KEY_HEAD_JS)) {
            dto.headJS = (String[]) config.get(KEY_HEAD_JS);
        }
    
        if (isStringArrayConfig(config, KEY_HEAD_CSS)) {
            dto.headCSS = (String[]) config.get(KEY_HEAD_CSS);
        }
        
        return dto;
    }
    
    /**
     * @return true if configuration contains a string value at <code>key</code>.
     */
    protected static boolean isStringConfig(IPluginConfig config, String key) {
        return config.containsKey(key) && config.get(key) instanceof String;
    }
    
    /**
     * @return true if configuration contains string array value at <code>key</code>.
     */
    protected static boolean isStringArrayConfig(IPluginConfig config, String key) {
        return config.containsKey(key) && config.get(key) instanceof String[];
    }
    
    
    // ------------------------------------------------------------------------------------------
    //      Accessors
    // ------------------------------------------------------------------------------------------
    
    public String getHtml() {
        return html;
    }
    
    public String getJs() {
        return js;
    }
    
    public String[] getHeadJS() {
        return headJS;
    }
    
    public String[] getHeadCSS() {
        return headCSS;
    }
    
    /**
     * @return the content string
     */
    public String getContentsString() {
        String complete = "";
        
        if (!StringUtils.isEmpty(this.html)) {
            complete += this.html;
        }
        
        if (!StringUtils.isEmpty(this.js)) {
            complete += "<script type='text/javascript'>" + this.js + "</script>";
        }
        
        return complete;
    }
    
    
    public void renderHead(IHeaderResponse response) {
        for (String css : this.getHeadCSS()) {
            response.render(
                new CssUrlReferenceHeaderItem(
                    css,
                    "screen",
                    null
                )
            );
        }
    
        // add javascripts to the header
        for (String js : this.getHeadJS()) {
            response.render(
                new JavaScriptUrlReferenceHeaderItem(
                    js,
                    "id" + new Date().getTime()
                )
            );
        }
    }
}
