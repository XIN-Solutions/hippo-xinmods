package nz.xinsolutions.services;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageUrlAnalyser {
    
    public static final String PATH_BINARIES = "binaries";
    public static final String PATH_EXTERNAL = "external";
    public static final String PATH_ASSETMOD = "assetmod";
    
    /**
     * Mimetypes to write to response
     */
    private static Map<String, String> s_mimeTypes = new LinkedHashMap<String, String>() {{
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("gif", "image/gif");
        put("png", "image/png");
    }};
    
    
    /**
     * Determine whether it's an internal or external request
     * @param fullUrl the url that is being requested.
     * @return
     */
    public boolean isLocalRequest(String fullUrl) {
        int binariesPathIdx = fullUrl.indexOf("/" + PATH_BINARIES);
        return binariesPathIdx != -1;
    }
    
    /**
     * @return part of the url where the actual thing lives
     */
    public String getBinaryLocation(String fullUrl) {
        String binariesPathPrefix = "/" + PATH_BINARIES;
        int binariesPathIdx = fullUrl.indexOf(binariesPathPrefix);
        if (binariesPathIdx == -1) {
            return null;
        }
        return fullUrl.substring(binariesPathIdx + binariesPathPrefix.length());
    }
    
    
    /**
     * @return the extension from the url
     */
    public String getExtension(String fullUrl) {
        int lastPeriodIdx = fullUrl.lastIndexOf('.');
        if (lastPeriodIdx == -1) {
            return null;
        }
        return fullUrl.substring(lastPeriodIdx + 1);
    }
    
    /**
     * @return mime type for extension of full url
     */
    public String getMimeType(String fullUrl) {
        String ext = this.getExtension(fullUrl);
        return s_mimeTypes.getOrDefault(ext, "application/octet-stream");
    }
    
    /**
     * @return the instructions part of teh full url
     */
    public String getInstructionString(String fullUrl) {
        
        int
            startIdx = fullUrl.indexOf(PATH_ASSETMOD) + PATH_ASSETMOD.length(),
            endIdx = fullUrl.indexOf(PATH_BINARIES);
        
        // not using /binaries? perhaps using /external instead.
        if (endIdx == -1) {
            endIdx = fullUrl.indexOf(PATH_EXTERNAL);
        }
        
        return fullUrl.substring(startIdx, endIdx);
    }
    
    /**
     * @return a list of instruction instances based on the raw instructions.
     */
    public Instruction[] interpretInstruction(String raw) {
        return
            Arrays.stream(raw.split("/"))
                .filter(StringUtils::isNotBlank)
                .map(Instruction::new)
                .toArray(Instruction[]::new)
            ;
    }
    
    public boolean hasVersionParameter(String fullUrl) {
        String instructions = this.getInstructionString(fullUrl);
        if (StringUtils.isEmpty(instructions)) {
            return false;
        }
        return instructions.contains("v=");
    }
    
    // ------------------------------------------------------------------------------------------
    //		Instructions
    // ------------------------------------------------------------------------------------------
    
    /**
     * Instruction container
     */
    public class Instruction {
        
        /**
         * Name
         */
        private String name;
        
        /**
         * Parameters
         */
        private String[] params;
        
        /**
         * Initialise data-members
         *
         * @param raw
         */
        public Instruction(String raw) {
            int eqIndex = raw.indexOf("=");
            this.name = raw.substring(0, eqIndex);
            this.params = raw.substring(eqIndex + 1).split(",");
        }
        
        /**
         * @return true if the parameter is empty
         */
        public boolean isEmpty(int idx) {
            if (params.length <= idx) {
                return false;
            }
            return params[idx].equals("_");
        }
        
        /**
         * @return the parameters
         */
        public String[] getParams() {
            return params;
        }
        
        /**
         * @return a specific parameter
         */
        public String getParam(int idx) {
            return (
                params.length > idx
                    ? params[idx]
                    : null
            );
        }
        
        
        /**
         * @return a number parameter
         */
        public Float getFloatParam(int idx) {
            return (
                params.length > idx
                    ? (NumberUtils.isNumber(params[idx]) ? Float.parseFloat(params[idx]) : null)
                    : null
            );
        }
        
        /**
         * @return a number parameter
         */
        public Integer getIntParam(int idx) {
            return (
                params.length > idx
                    ? (NumberUtils.isDigits(params[idx]) ? Integer.parseInt(params[idx]) : null)
                    : null
            );
        }
        
        public String getName() {
            return name;
        }
        
    }
    

}
