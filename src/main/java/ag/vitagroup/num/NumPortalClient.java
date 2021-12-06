package ag.vitagroup.num;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumPortalClient {

  private final String numUri;
  private final String tokenUri;
  private final String clientId;
  private final String secret;

  private static final Logger LOGGER = LoggerFactory.getLogger(NumPortalClient.class);

  private TokenProvider tokenProvider = TokenProvider.getInstance();

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer";

  public NumPortalClient(String numUri, String tokenUri, String clientId, String secret) {
    this.numUri = numUri;
    this.tokenUri = tokenUri;
    this.clientId = clientId;
    this.secret = secret;
  }

  public List<String> getDomainsWhitelist() {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(numUri);

      request.addHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
      request.addHeader(AUTHORIZATION,
          String.format("%s %s", BEARER, tokenProvider.getToken(clientId, secret, tokenUri)));

      CloseableHttpResponse response = client.execute(request);

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity().getContent(), List.class);
      } else {
        LOGGER.error(
            "An error has occurred while communicating with the portal. Status code: {}, Reason: {}",
            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        throw new SystemException("An error has occurred while communicating with the portal");
      }
    } catch (Exception e) {
      throw new SystemException(e.getMessage(), e);
    }
  }
}
