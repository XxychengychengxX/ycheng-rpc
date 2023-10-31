/**
 * @author Valar Morghulis
 * @Date 2023/10/4
 */
package com.ychengycheng.core.protection;

public interface RateLimiter {
    boolean allowRequest();

}
