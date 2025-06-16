package pl.niebieskieaparaty.imageuploader.security.adapter.primary.rest;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/logout")
@RequiredArgsConstructor
class LogoutResource {

    private final CurrentIdentityAssociation identity;

    @POST
    public Response logout() {
        if (identity.getIdentity().isAnonymous()) {
            throw new UnauthorizedException("Not authenticated");
        }

        FormAuthenticationMechanism.logout(identity.getIdentity());
        return Response.noContent().build(); // JS handles redirect
    }
}
