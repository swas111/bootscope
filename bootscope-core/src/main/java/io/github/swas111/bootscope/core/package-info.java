/**
 * Bootscope core: plain Java 17 diagnostics logic, independent of any Spring Boot generation.
 *
 * <p>Holds the tool logic, {@code Redactor}, {@code OutputBudget}, in-memory error buffer, audit
 * log and the data-source interfaces that the Boot 3 and Boot 4 adapters implement. Nothing here
 * depends on Spring; most tests live in this module.
 */
package io.github.swas111.bootscope.core;
