package pl.kajeto.jobs.domain;

/**
 * The only way for the application to learn who the current tenant is. Services use it to scope
 * every query; nothing else reads the owner from the request.
 */
public interface TenantProvider {

  /**
   * @return the tenant of the current request
   * @throws TenantNotResolvedException when the request carries no usable identity
   */
  TenantId currentTenant();
}
