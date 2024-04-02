package org.highmed.mailwhitelisting;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public final class TokenProvider {

  Map<String, Token> tokenStore = new HashMap<>();

  private static final String GRANT_TYPE_KEY = "grant_type";
  private static final String CONTENT_TYPE_KEY = "Content-type";
  private static final String CLIENT_SECRET_KEY = "client_secret";
  private static final String GRANT_TYPE_VALUE = "client_credentials";

  private static TokenProvider INSTANCE;

  private final static Logger LOGGER = Logger.getLogger(TokenProvider.class.getName());

  private TokenProvider() {
  }

  public synchronized static TokenProvider getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TokenProvider();
    }
    return INSTANCE;
  }

  public String getToken(String clientId, String secret, String tokenUri)
      throws AuthenticationException, IOException {
    Token token = tokenStore.get(String.format("%s%s%s", clientId, secret, tokenUri));

    if (isTokenInvalid(token)) {
      try (CloseableHttpClient client = HttpClients.createDefault()) {
        HttpPost httpPost = new HttpPost(tokenUri);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
        params.add(new BasicNameValuePair(CLIENT_SECRET_KEY, secret));

        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpPost.setHeader(CONTENT_TYPE_KEY, ContentType.APPLICATION_FORM_URLENCODED.toString());

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(clientId,
            secret);
        httpPost.addHeader(new BasicScheme().authenticate(credentials, httpPost, null));

        CloseableHttpResponse response = client.execute(httpPost);

        ObjectMapper mapper = new ObjectMapper();

        token = mapper.readValue(response.getEntity().getContent(), Token.class);
        token.setExpiry(LocalDateTime.now().plusSeconds(token.getExpiresIn()));

        tokenStore.put(String.format("%s%s%s", clientId, secret, tokenUri), token);
      }
    }

    return token.getAccessToken();
  }

  private boolean isTokenInvalid(Token token) {
    return (token == null || token.getExpiry().minusSeconds(20).isBefore(LocalDateTime.now()));
  }
}


