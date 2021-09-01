package nz.xinsolutions.authentication;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.xinsolutions.jwt.services.UserInfoGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.repository.PasswordHelper;
import org.hippoecm.repository.api.HippoSession;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To manipulate users and groups in the BloomReach JCR to synchronise the
 *      permissions as described by the incoming token as well as possible. This
 *      class is also able to check the validity of the tokens passed into the application.
 *
 */
public class Auth0Service {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Auth0Service.class);

    /**
     * Property constants
     */
    public static final String HIPPOSYS_PASSWORD = "hipposys:password";
    public static final String HIPPOSYS_EMAIL = "hipposys:email";
    public static final String HIPPOSYS_FIRSTNAME = "hipposys:firstname";
    public static final String HIPPOSYS_ACTIVE = "hipposys:active";
    public static final String HIPPOSYS_SECURITYPROVIDER = "hipposys:securityprovider";
    public static final String HIPPOSYS_USERROLES = "hipposys:userroles";

    public static final String USER_BASE_PATH = "/hippo:configuration/hippo:users";
    public static final String GROUP_BASE_PATH = "/hippo:configuration/hippo:groups";

    public static final String KEY_PERMISSIONS = "permissions";
    public static final String HIPPOSYS_MEMBERS = "hipposys:members";

    private UrlJwkProvider jwkProvider;

    /**
     * Get the configuration for the auth0 bridge.
     *
     * @param session   the jcr session to use
     * @return the auth0 configuration instance or null if something went wrong.
     * @throws RepositoryException
     */
    public Auth0Configuration retrieveConfiguration(Session session) throws RepositoryException {

        String cfgPath = getConfigurationPath();

        if (!session.nodeExists(cfgPath)) {
            LOG.error("Could not find a xinmods module configuration, skipping.");
            return null;
        }

        Node cfgNode = session.getNode(cfgPath);
        return Auth0Configuration.newFromNode(cfgNode);
    }

    /**
     * Make sure the JWT is valid.
     *
     *
     * @param authConfig
     * @param encodedJwt   the jwt to check for validity.
     * @return true if it's a valid token.
     */
    public DecodedJWT parseValidToken(Auth0Configuration authConfig, String encodedJwt) {

        try {
            UrlJwkProvider jwkProvider = getJwkProvider(authConfig);

            // decode without verifying and get the key we are using from JWKS
            DecodedJWT jwt = JWT.decode(encodedJwt);
            Jwk jwk = jwkProvider.get(jwt.getKeyId());

            // setup the algorithm
            Algorithm algorithm =
                Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            // build verifier
            JWTVerifier verifier =
                JWT.require(algorithm)
                    .withAudience(authConfig.getRequiredAudience())
                    .build()
            ;

            // validate the decoded jwt
            DecodedJWT validJwt = verifier.verify(encodedJwt);
            return validJwt;
        }
        catch (Exception ex) {
            LOG.error("Exception validating token, caused by: ", ex);
        }

        return null;
    }


    /**
     * @return a lazy-loaded UrlJwkProvider that will help find public keys for our JWT verification
     * @throws MalformedURLException
     */
    protected UrlJwkProvider getJwkProvider(Auth0Configuration config) throws MalformedURLException {
        if (this.jwkProvider == null) {
            return this.jwkProvider = new UrlJwkProvider(new URL(config.getJwksUrl()));
        }
        return this.jwkProvider;

    }

    /**
     * Create a new user in the Bloomreach repository.
     *
     * @param session   the jcr admin session to use create a new user
     * @param model     the model with information to be reflected
     * @return true if the node was created
     */
    public boolean createUser(Session session, BridgeTokenModel model) {
        try {
            Node baseNode = session.getNode(USER_BASE_PATH);
            Node userNode = baseNode.addNode(model.getEmail(), "hipposys:user");

            userNode.setProperty(HIPPOSYS_ACTIVE, true);
            userNode.setProperty(HIPPOSYS_EMAIL, model.getEmail());
            userNode.setProperty(HIPPOSYS_SECURITYPROVIDER, "internal");
            return true;
        }
        catch (RepositoryException rEx) {
            LOG.error("Something went wrong trying to create a user, caused by: ", rEx);
        }
        return false;
    }

    /**
     * For a user update the password and return it.
     *
     * @param session the admin sesssion to use.
     * @param token the email address to update the password for
     * @param jwt
     */
    public String refreshUserAndSetPassword(Session session, BridgeTokenModel token, DecodedJWT jwt) {
        try {

            // gather user information
            String userName = token.getEmail();
            String newPassword = UUID.randomUUID().toString();
            String hashedPassword = PasswordHelper.getHash(newPassword.toCharArray());

            // add roles and groups
            List<String> permissions = this.getPermissionListFromToken(jwt);
            List<String> roles = filterPermissionsStartingWith(permissions, "role:");
            List<String> groups = filterPermissionsStartingWith(permissions, "group:");

            UserInfoGenerator infoGenerator = newInfoGeneratorInstance();
            List<String> existingGroups = infoGenerator.queryUserGroups(session, userName);

            // existing groups minus groups we require results in a list of groups that are to be removed
            List<String> removedGroups = (List<String>) ListUtils.subtract(existingGroups, groups);

            // required groups minus existing groups will return a list of groups to add
            List<String> newGroups = (List<String>) ListUtils.subtract(groups, existingGroups);

            // get node
            Node userNode = session.getNode(getPathForUsername(userName));

            // set new information
            userNode.setProperty(HIPPOSYS_PASSWORD, hashedPassword);
            userNode.setProperty(HIPPOSYS_EMAIL, userName);
            userNode.setProperty(HIPPOSYS_FIRSTNAME, token.getName());
            userNode.setProperty(HIPPOSYS_USERROLES, roles.toArray(new String[0]));

            // remove group memberships
            if (!CollectionUtils.isEmpty(removedGroups)) {
                for (String group : removedGroups) {
                    this.removeUserFromGroup(session, userName, group);
                }
            }

            // add new groups
            if (!CollectionUtils.isEmpty(newGroups)) {
                for (String group : newGroups) {
                    this.addUserToGroup(session, userName, group);
                }
            }

            session.save();
            return newPassword;
        }
        catch (Exception ex) {
            LOG.error("New password could not be set for {}, caused by: ", token, ex);
        }

        return null;
    }

    /**
     * Remove a user from a group
     *
     * @param session
     * @param userName
     * @param group
     */
    protected void removeUserFromGroup(Session session, String userName, String group) throws RepositoryException {
        String groupPath = this.getPathForGroup(group);
        if (!session.nodeExists(groupPath)) {
            LOG.error("This group does not exist, will not remove user from group: {}", group);
            return;
        }

        Node groupNode = session.getNode(groupPath);
        if (!groupNode.hasProperty(HIPPOSYS_MEMBERS)) {
            LOG.error("Nothing to remove from group: {}", group);
            return;
        }

        List<String> newList = new ArrayList<>();
        Value[] values = groupNode.getProperty(HIPPOSYS_MEMBERS).getValues();
        for (Value val : values) {
            String memberName = val.getString();

            // is this is name to remove? skip adding it.
            if (memberName.equals(userName)) {
                continue;
            }

            newList.add(memberName);
        }

        groupNode.setProperty(HIPPOSYS_MEMBERS, newList.toArray(new String[0]));
   }

    /**
     * Add a user to group membership in the repository.
     *
     * @param session   the session to use
     * @param userName  the username to check
     * @param group     the group to add
     */
    protected void addUserToGroup(Session session, String userName, String group) throws RepositoryException {
        String groupPath = this.getPathForGroup(group);
        if (!session.nodeExists(groupPath)) {
            LOG.error("This group does not exist, will not add user to group: {}", group);
            return;
        }

        Node groupNode = session.getNode(groupPath);
        if (!groupNode.hasProperty(HIPPOSYS_MEMBERS)) {
            groupNode.setProperty(HIPPOSYS_MEMBERS, new String[] { userName });
            return;
        }

        // add a new member to the list of members
        Value[] values = groupNode.getProperty(HIPPOSYS_MEMBERS).getValues();
        List<String> newList = new ArrayList<>();
        for (Value val : values) {
            String memberName = val.getString();
            newList.add(memberName);
        }
        newList.add(userName);

        // set on property.
        groupNode.setProperty(HIPPOSYS_MEMBERS, newList.toArray(new String[0]));
    }

    /**
     * @return filtered permissions that start with `prefix`
     */
    protected List<String> filterPermissionsStartingWith(List<String> permissions, String prefix) {
        return permissions
            .stream()
            .filter(s -> s.startsWith(prefix))
            .map(roleName -> roleName.split(":")[1])
            .collect(Collectors.toList());
    }


    /**
     * Create a new administrative session based on the live user credentials ability
     * to impersonate the 'admin' user. Make sure to close the session.
     *
     * @return an administrator session
     * @throws RepositoryException
     */
    protected Session loginAdministrative() throws RepositoryException {
        final PluginUserSession userSession = PluginUserSession.get();
        HippoSession session = userSession.getJcrSession();
        Session adminSession = session.impersonate(new SimpleCredentials("admin", "".toCharArray()));
        return adminSession;
    }


    /**
     * Get a list of permissions that will translate to roles in the user.
     * @param jwt the JWT to decode
     * @return the list of permissions
     */
    protected List<String> getPermissionListFromToken(DecodedJWT jwt) {
        ObjectMapper objMap = new ObjectMapper();

        try {
            String payloadStr = new String(Base64.getDecoder().decode(jwt.getPayload().getBytes("UTF-8")), "UTF-8");
            Map<String, Object> payloadObj = objMap.readValue(payloadStr, Map.class);

            if (!payloadObj.containsKey(KEY_PERMISSIONS)) {
                return Collections.emptyList();
            }

            List<String> permissions = (List<String>) payloadObj.get(KEY_PERMISSIONS);
            return permissions;
        }
        catch (Exception ex) {
            LOG.error("Could not extract permissions list from token, caused by: ", ex);
            return Collections.emptyList();
        }
    }


    /**
     * Check that user exists.
     *
     * @param session
     * @param email
     * @return
     */
    protected boolean userExists(Session session, String email) {
        try {
            return session.nodeExists(getPathForUsername(email));
        }
        catch (RepositoryException rEx) {
            LOG.error("Could not check availability of user with name: {}, caused by: ", email, rEx);
            return false;
        }
    }

    /**
     * @return the path for this username
     */
    protected String getPathForUsername(String email) {
        return USER_BASE_PATH + "/" + Text.escapeIllegalJcrChars(email);
    }


    /**
     * @return the path for this username
     */
    protected String getPathForGroup(String group) {
        return GROUP_BASE_PATH + "/" + Text.escapeIllegalJcrChars(group);
    }

    /**
     * @return the configuration path
     */
    protected String getConfigurationPath() {
        return "/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig";
    }

    /**
     * @return the instance used for getting group information
     */
    protected UserInfoGenerator newInfoGeneratorInstance() {
        return new UserInfoGenerator();
    }

}
