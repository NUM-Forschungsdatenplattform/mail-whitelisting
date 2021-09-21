package ag.vitagroup.num;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("expires_in")
  private long expiresIn;

  private LocalDateTime expiry;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
    this.expiry = LocalDateTime.now().plusSeconds(expiresIn);
  }

  public LocalDateTime getExpiry() {
    return expiry;
  }

}
