package com.rhcloud.igorbotian.rsskit.rest.facebook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.rhcloud.igorbotian.rsskit.db.RsskitDataSource;
import com.rhcloud.igorbotian.rsskit.db.facebook.FacebookEntityManager;
import com.rhcloud.igorbotian.rsskit.db.facebook.FacebookEntityManagerImpl;
import com.rhcloud.igorbotian.rsskit.rest.facebook.FacebookException;
import com.rhcloud.igorbotian.rsskit.rest.facebook.FacebookNewsFeed;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/**
 * @author Igor Botian <igor.botyan@alcatel-lucent.com>
 */
public class FacebookAPIImpl implements FacebookAPI {

    private static final String API_VERSION = "2.3";

    private final OAuthEndpoint oAuth = new OAuthEndpoint();
    private final HomeEndpoint home;
    private final ObjectEndpoint objects;
    private final FacebookEntityManager entityManager;

    public FacebookAPIImpl(RsskitDataSource source) throws FacebookException {
        Objects.requireNonNull(source);

        try {
            this.entityManager = new FacebookEntityManagerImpl(source);
        } catch (SQLException e) {
            throw new FacebookException("Failed to initialize Facebook entity manager");
        }

        this.home = new HomeEndpoint(this);
        this.objects = new ObjectEndpoint(this);
    }

    @Override
    public String requestAccessToken(String clientID, String clientSecret, String code, URL callbackURL)
            throws FacebookException {

        Objects.requireNonNull(clientID);
        Objects.requireNonNull(clientSecret);
        Objects.requireNonNull(code);
        Objects.requireNonNull(callbackURL);

        AccessToken token = oAuth.requestAccessToken(clientID, clientSecret, code, callbackURL);
        return entityManager.registerAccessToken(token.token, token.expires);
    }

    @Override
    public URL getAuthorizationURL(String clientID, Set<String> permissions, URL callbackURL) throws FacebookException {
        Objects.requireNonNull(clientID);
        Objects.requireNonNull(permissions);
        Objects.requireNonNull(callbackURL);

        return oAuth.getAuthorizationURL(clientID, permissions, callbackURL);
    }

    @Override
    public String version() {
        return API_VERSION;
    }

    @Override
    public boolean isAccessTokenExpired(String token) throws FacebookException {
        Objects.requireNonNull(token);
        return entityManager.isAcessTokenExpired(token);
    }

    @Override
    public FacebookNewsFeed getNewsFeed(String token) throws FacebookException {
        Objects.requireNonNull(token);

        String accessToken = entityManager.getAccessToken(token);

        if (accessToken == null) {
            throw new FacebookException("Access token is not registered: " + token);
        }

        return home.getNewsFeed(accessToken);
    }

    @Override
    public JsonNode getObject(String id, String accessToken) throws FacebookException {
        Objects.requireNonNull(id);
        return objects.get(id, accessToken);
    }
}
