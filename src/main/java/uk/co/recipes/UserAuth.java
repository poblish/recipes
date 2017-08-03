/**
 *
 */
package uk.co.recipes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import uk.co.recipes.api.IUserAuth;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @author andrewregan
 */
public class UserAuth implements IUserAuth {

    private final String id;
    private final String provider;

    /**
     */
    @JsonCreator
    public UserAuth(@JsonProperty("authProvider") final String inProvider, @JsonProperty("authId") final String inId) {
        this.provider = checkNotNull(inProvider);
        this.id = checkNotNull(inId);
    }

    public String getAuthId() {
        return id;
    }

    public String getAuthProvider() {
        return provider;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(provider, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserAuth)) {
            return false;
        }
        final UserAuth other = (UserAuth) obj;
        return Objects.equal(id, other.id) && Objects.equal(provider, other.provider);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("id", id)
                .add("provider", provider)
                .toString();
    }
}
