package org.highmed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationProfile;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RegistrationProfileWithMailDomainCheck extends RegistrationProfile implements
    FormAction {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RegistrationProfileWithMailDomainCheck.class);

  public static final String PROVIDER_ID = "registration-mail-domain-validation";
  private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

  private static final String CLIENT_ID = "clientId";
  private static final String SECRET = "secret";
  private static final String NUM_PORTAL_URI = "numPortalUri";
  private static final String TOKEN_URI = "tokenUri";
  private static final String ERROR_MESSAGE = "errorMessage";

  private static final int EMAIL_MAX_LENGTH = 255;

  static {
    ProviderConfigProperty clientIdProperty = new ProviderConfigProperty();
    clientIdProperty.setName(CLIENT_ID);
    clientIdProperty.setLabel("Client id");
    clientIdProperty.setType(ProviderConfigProperty.STRING_TYPE);
    clientIdProperty.setHelpText("Client id");

    ProviderConfigProperty secretProperty = new ProviderConfigProperty();
    secretProperty.setName(SECRET);
    secretProperty.setLabel("Secret");
    secretProperty.setType(ProviderConfigProperty.STRING_TYPE);
    secretProperty.setHelpText("Secret");

    ProviderConfigProperty urlProperty = new ProviderConfigProperty();
    urlProperty.setName(NUM_PORTAL_URI);
    urlProperty.setLabel("Num portal uri");
    urlProperty.setType(ProviderConfigProperty.STRING_TYPE);
    urlProperty.setHelpText("Num portal uri");

    ProviderConfigProperty tokenProperty = new ProviderConfigProperty();
    tokenProperty.setName(TOKEN_URI);
    tokenProperty.setLabel("Token uri");
    tokenProperty.setType(ProviderConfigProperty.STRING_TYPE);
    tokenProperty.setHelpText("Keycloak token uri");

    ProviderConfigProperty errorMessageProperty = new ProviderConfigProperty();
    errorMessageProperty.setName(ERROR_MESSAGE);
    errorMessageProperty.setLabel("Error message");
    errorMessageProperty.setType(ProviderConfigProperty.TEXT_TYPE);
    errorMessageProperty.setHelpText(
        "Sample: <div>Invalid email address. Please contact us at: <a href=\"mailto:dorothea.brooke@vitagroup.com\">Dorothea Brooke</a></div>");

    CONFIG_PROPERTIES.add(tokenProperty);
    CONFIG_PROPERTIES.add(clientIdProperty);
    CONFIG_PROPERTIES.add(secretProperty);
    CONFIG_PROPERTIES.add(urlProperty);
    CONFIG_PROPERTIES.add(errorMessageProperty);
  }

  @Override
  public String getDisplayType() {
    return "Registration email domain validation";
  }


  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return CONFIG_PROPERTIES;
  }

  @Override
  public String getHelpText() {
    return "Adds validation of domain emails for registration";
  }

  @Override
  public void validate(ValidationContext context) {
    Map<String, String> config = context.getAuthenticatorConfig().getConfig();

    NumPortalClient client = new NumPortalClient(config.get(NUM_PORTAL_URI),
        config.get(TOKEN_URI), config.get(CLIENT_ID), config.get(SECRET));

    List<String> domains = List.of();

    try {
      domains = client.getDomainsWhitelist();
    } catch (SystemException e) {
      LOGGER.error("An error has occurred while retrieving whitelisted email domains: {}",
          e.getMessage(), e);
    }

    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

    List<FormMessage> errors = new ArrayList<>();
    String email = formData.getFirst(Validation.FIELD_EMAIL);

    if (email != null) {
      validateEmail(email, context, errors);
      validateDomain(email, domains, context, errors, config);
    }

    if (errors.size() > 0) {
      context.error(Errors.INVALID_REGISTRATION);
      context.validationError(formData, errors);
    } else {
      context.success();
    }
  }

  private void validateEmail(String email, ValidationContext context, List<FormMessage> errors) {
    if (Validation.isBlank(email)) {
      LOGGER.error("Registration email is blank");
      errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
    } else if (!Validation.isEmailValid(email) || email.length() > EMAIL_MAX_LENGTH) {
      LOGGER.error("Registration email is not valid: {}", email);
      context.getEvent().detail(Details.EMAIL, email).error(Messages.INVALID_EMAIL);
      errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
    }
  }

  private void validateDomain(String email, List<String> domains, ValidationContext context,
      List<FormMessage> errors, Map<String, String> config) {
    LOGGER.info("Whitelisted domains: {}", domains.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(",", "{", "}")));

    String emailDomain = email.substring(email.indexOf("@") + 1);

    for (String domain : domains) {
      if (Pattern.matches(domainToRegex(domain), emailDomain)) {
        LOGGER.info("Email address {} matches domain {}.", email, domain);
        return;
      }
    }

    LOGGER.error("Email address not whitelisted: {}", email);
    context.getEvent().detail(Details.EMAIL, email);
    errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, config.get(ERROR_MESSAGE)));
  }

  private String domainToRegex(String domain) {
    String newDomain = domain.replace(".", "\\.");
    return newDomain.replace("*", ".*");
  }
}
