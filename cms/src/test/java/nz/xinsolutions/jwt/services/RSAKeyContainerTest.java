package nz.xinsolutions.jwt.services;


import junit.framework.TestCase;
import nz.xinsolutions.jwt.JwtServiceConfig;
import org.junit.Test;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 * <p>
 * Purpose:
 */
public class RSAKeyContainerTest  {

    @Test
    public void testLoadPublicKey() {
        JwtServiceConfig jwtCfg = JwtServiceConfig.fromWebContext(null);
        RSAKeyContainer rsaKey = new RSAKeyContainer(jwtCfg);
        rsaKey.initialiseKeys();
    }

}