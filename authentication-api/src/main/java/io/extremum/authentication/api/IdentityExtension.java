package io.extremum.authentication.api;

/**
 * Represent extension for {@link io.extremum.authentication.common.models.Identity}.
 * Implement it on your authentication project scope.
 *
 * @implNote to avoid name collisions - add prefix of your project to implementation class name.
 * <p>
 * <b>Examples: </b>
 * </p>
 *
 * <ul>
 * <li>{@code TestIdentityExtensionImpl}</li>
 * <li>{@code MyProjectNameIdentityExtensionImpl}</li>
 * </ul>
 *
 * @implSpec to correctly save implementations to database - you need to annotate it with @TypeAlias.
 * Use your implementation class name as value for annotation.
 */
public interface IdentityExtension {
}
