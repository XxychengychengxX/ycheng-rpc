/**
 * @author Valar Morghulis
 * @Date 2023/10/4
 */
package com.ychengycheng.core.protection;

public interface CircuitBreaker {

    void recordRequest();
    void recordErrorRequest();

    boolean isBreak();

    void reset();
}
