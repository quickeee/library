/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.security.auth;

import java.util.Iterator;
import java.util.List;

import org.beangle.commons.bean.Initializing;
import org.beangle.commons.collection.CollectUtils;
import org.beangle.security.core.Authentication;
import org.beangle.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderManager extends AbstractAuthenticationManager implements Initializing {

  protected final Logger logger = LoggerFactory.getLogger(ProviderManager.class);

  protected List<AuthenticationProvider> providers = CollectUtils.newArrayList();

  public void init() throws Exception {
    if (providers.isEmpty()) { throw new RuntimeException("authentication provider list is empty"); }
    logger.info("providers:" + providers);
  }

  public Authentication doAuthentication(Authentication auth) throws AuthenticationException {
    Iterator<AuthenticationProvider> iter = getProviders().iterator();
    Class<? extends Authentication> toTest = auth.getClass();
    AuthenticationException lastException = null;

    while (iter.hasNext()) {
      AuthenticationProvider provider = iter.next();
      if (!provider.supports(toTest)) continue;

      Authentication result;
      try {
        result = provider.authenticate(auth);
        if (result != null) copyDetails(auth, result);
      } catch (AuthenticationException ae) {
        lastException = ae;
        result = null;
      }
      if (lastException instanceof AccountStatusException) break;
      if (null != result) return result;
    }

    if (lastException == null) lastException = new ProviderNotFoundException("Provider not found!");
    throw lastException;
  }

  private void copyDetails(Authentication source, Authentication dest) {
    if ((dest instanceof AbstractAuthentication) && (dest.getDetails() == null)) {
      AbstractAuthentication token = (AbstractAuthentication) dest;
      token.setDetails(source.getDetails());
    }
  }

  public List<AuthenticationProvider> getProviders() {
    return providers;
  }

  public void setProviders(List<AuthenticationProvider> providers) {
    this.providers = providers;
  }

}
