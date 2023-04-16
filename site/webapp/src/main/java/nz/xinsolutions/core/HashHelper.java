package nz.xinsolutions.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;


public class HashHelper {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HashHelper.class);

    /**
     * Private constructor, because it's a helper.
     */
    private HashHelper() {}

    /**
     * Hash to MD5. Convenience function to hash input to MD5.
     *
     * @param input     the input to hash.
     * @return the output.
     */
    public static String hashToMD5(String input) {
        return hashToAlgorithm(input, "MD5");
    }

    /**
     * This function will grab some input and hash it using a given algorithm and then output
     * the hexadecimal pairs as a string returned by the function.
     * @param input     the thing to hash
     * @param algo      the algorithm to use
     *
     * @return a hash for this input using a specific algorithm.
     */
    public static String hashToAlgorithm(String input, String algo) {

        try {
            MessageDigest digest = MessageDigest.getInstance(algo);

            // hash the email
            digest.update(input.getBytes(UTF_8));

            // turn into hex representation
            return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();

        }
        catch (NoSuchAlgorithmException nsaEx) {
            LOG.error("No md5 algorithm found for hashing.");
        }

        // something went wrong, let's forget about this.
        return null;
    }

}
