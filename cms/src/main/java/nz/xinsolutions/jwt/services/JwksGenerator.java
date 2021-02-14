package nz.xinsolutions.jwt.services;

import net.minidev.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Is able to generate a JWKS map based on the RSA key loader
 */
public class JwksGenerator {

    public static final String KEY_KEYS = "keys";

    /**
     * Key loader
     */
    private RSAKeyContainer keyContainer;

    /**
     * Initialise data-members
     *
     * @param keyContainer the RSAKeyLoader instance
     */
    public JwksGenerator(RSAKeyContainer keyContainer) {
        this.keyContainer = keyContainer;
    }

    /**
     * @return the JWKS map that contains the currently active key
     */
    public Map<String, List<JSONObject>> createJwksMap() {

        Map<String, List<JSONObject>> jwksMap = new LinkedHashMap<>();

        // push the keys into a list of json objects
        List<JSONObject> keyList =
            keyContainer.getKeys()
                .stream()
                .map(holder -> holder.getRsaKey().toJSONObject() )
                .collect(Collectors.toList())
            ;

        jwksMap.put(KEY_KEYS, keyList);

        return jwksMap;
    }


}
