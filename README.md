

# mail-whitelisting

### Deployment

1. Copy jar to ```/keycloak-<version>/providers```
2. Run the following command to complete the installation:
```
${kc.home.dir}/bin/kc.sh build
```

### Configuration

Configuration is done per realm in the keycloak administration console

Steps:

1. Select realm to be configured
2. Go to *Authentication* tab on the left and under *Flows* select *Registration* 
3. On the right hand of the screen click "Duplicate" button and create a copy of the *Registration* flow
4. Name the new flow
5. In the *Flows* tab, select the newly created registration flow in order to configure it
6. Under *Add* (+ icon), add a new step 
7. In the providers list, select the plugin name *Registration email domain validation*
8. Save
9. Move the newly added execution flow to be just below *Profile validation*; this is important such that all the form validation is done sequentially
10. Enable the new execution 
11. On the right there is a config button (gear settings) where the plugin is to be configured
12. Under *Flows* tab, the newly created registration flow needs to be selected instead of the default *Registration* 
13. Click on settings (right corner with 3 verical dots) and choose bind flow
14. Select registration flow
15. Default *Registration* flow should appear as 'Not in use' now

Sample config values

* Num portal uri: http://host.docker.internal:8090/organization/domains
* Token uri: https://keycloak.dev.num-codex.de/auth/realms/crr/protocol/openid-connect/token
* Client id: num-portal
* Client secret: <num-portal-client-secret>
* Error message: 

```<span class="message-text" style = "display:block">Your email-address is not allowed. Please contact our support at:<a href="mailto:num-support@gwdg.de" style="color: white;font-weight: bold;padding-left: 10px;">num-support@gwdg.de</a> and inform about this message.</span>``` 


### License

Copyright 2021 vitagroup AG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
