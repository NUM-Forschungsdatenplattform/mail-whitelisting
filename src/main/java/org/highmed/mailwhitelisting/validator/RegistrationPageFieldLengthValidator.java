package org.highmed.mailwhitelisting.validator;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationProfile;
import org.keycloak.events.Errors;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegistrationPageFieldLengthValidator extends RegistrationProfile implements FormAction {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegistrationPageFieldLengthValidator.class);

    private static final String PROVIDER_ID = "num-field-length-validator";
    private static final String FIRSTNAME_MAX_LENGTH_KEY = "firstname_max_length";
    private static final String LASTNAME_MAX_LENGTH_KEY = "lastname_max_length";
    private static final String DEPARTMENT_MAX_LENGTH_KEY = "department_max_length";
    private static final String NOTES_MAX_LENGTH_KEY = "notes_max_length";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty firstNameMaxLength = setupConfig(FIRSTNAME_MAX_LENGTH_KEY, "Firstname maximum length", "Firstname maximum length");
        configProperties.add(firstNameMaxLength);

        ProviderConfigProperty lastNameMaxLength = setupConfig(LASTNAME_MAX_LENGTH_KEY, "Lastname maximum length", "Lastname maximum length");
        configProperties.add(lastNameMaxLength);

        ProviderConfigProperty departmentMaxLength = setupConfig(DEPARTMENT_MAX_LENGTH_KEY, "Department maximum length", "Department maximum length");
        configProperties.add(departmentMaxLength);

        ProviderConfigProperty notesMaxLength = setupConfig(NOTES_MAX_LENGTH_KEY, "Additional notes maximum length", "Additional notes maximum length");
        configProperties.add(notesMaxLength);

    }

    @Override
    public void validate(ValidationContext context) {
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        List<FormMessage> errors = new ArrayList<>();
        Optional<FormMessage> firstNameValidation = validateField(config, formData.getFirst(RegistrationPage.FIELD_FIRST_NAME), RegistrationPage.FIELD_FIRST_NAME, FIRSTNAME_MAX_LENGTH_KEY, "firstname-error-length-too-long");
        Optional<FormMessage> lastNameValidation = validateField(config, formData.getFirst(RegistrationPage.FIELD_LAST_NAME), RegistrationPage.FIELD_LAST_NAME, LASTNAME_MAX_LENGTH_KEY, "lastname-error-length-too-long");
        Optional<FormMessage> departmentValidation = validateField(config, formData.getFirst("user.attributes.department"), "user.attributes.department", DEPARTMENT_MAX_LENGTH_KEY, "department-error-length-too-long");
        Optional<FormMessage> notesValidation = validateField(config, formData.getFirst("user.attributes.notes"), "user.attributes.notes", NOTES_MAX_LENGTH_KEY, "notes-error-length-too-long");

        if (firstNameValidation.isPresent()) {
            errors.add(firstNameValidation.get());
        }
        if (lastNameValidation.isPresent()) {
            errors.add(lastNameValidation.get());
        }
        if (departmentValidation.isPresent()) {
            errors.add(departmentValidation.get());
        }
        if (notesValidation.isPresent()) {
            errors.add(notesValidation.get());
        }
        if (errors.size() > 0) {
            context.error(Errors.INVALID_REGISTRATION);
            context.validationError(formData, errors);
        } else {
            context.success();
        }
    }

    private Optional<FormMessage> validateField(Map<String, String> contextConfig, String fieldValue, String fieldName, String configKey, String errorKey) {
        Integer fieldMaxLength = Integer.valueOf(contextConfig.get(configKey));
        if (StringUtil.isNotBlank(fieldValue)) {
            if (contextConfig.containsKey(configKey) && fieldValue.length() > fieldMaxLength.intValue()) {
                LOGGER.warn("Registration field {} exceeded configured max length {}. Current length {} ", fieldName, fieldMaxLength, fieldValue.length());
                return Optional.of(new FormMessage(fieldName, errorKey, fieldMaxLength));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayType() {
        return "NUM Custom registration page field length validator";
    }

    @Override
    public String getHelpText() {
        return "Validates first name, last name, department and additional notes field length.";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private static ProviderConfigProperty setupConfig(String name, String label, String helpText) {
        ProviderConfigProperty configProperty = new ProviderConfigProperty();
        configProperty.setName(name);
        configProperty.setLabel(label);
        configProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperty.setHelpText(helpText);
        return configProperty;
    }
}
