# mail-whitelisting

### Deployment

Copy jar to /opt/jboss/keycloak/standalone/deployments

### Cofiguration

Configuration is done per ream in the keycloak administration console

Steps:

1. Select realm to be configured
1. Go to *Authentication* tab on the left and under *Flows* select *Registration* 
1. On the right hand of the screen click copy button and creat a copy of the *Registration* flow
1. Name the new flow
1. In the *Flows* tab, select the newly created registration flow in order to configure it
1. Under *Actions*, under the root execution, add a new execution
1. In the providers list, select the plugin name *Registration email domain validation*
1. Save
1. Move the newly added execution flow to be just below *Profile validation*; this is important such that all the form validation is done sequentially
1. Enable the new execution
1. On the right there is a config button where the plugin is to be configured
1. Under *Bindings* tab next to the *Flows* tab, the newly created registration flow needs to be selected instead of the default *Registration* 

Sample config values

* Num portal uri: http://host.docker.internal:8090/organization/domains
* Token uri: https://keycloak.dev.num-codex.de/auth/realms/crr/protocol/openid-connect/token
* Client id: 89dddc8f-0f25-4faf-a58d-6cda681f6ed3
* Secret: num-portal
* Error message: 

```<div>Invalid email address. Please contact us at: <a href="mailto:dorothea.brooke@vitagroup.com">Dorothea Brooke</a></div>``` 
