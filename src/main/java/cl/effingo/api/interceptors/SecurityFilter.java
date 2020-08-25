package cl.effingo.api.interceptors;


import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import javax.ws.rs.ext.Provider;

import cl.effingo.services.WordPressAPI;
import cl.effingo.utils.Parametros;


@Provider
public class SecurityFilter implements ContainerRequestFilter{
	

	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void filter (ContainerRequestContext requestContext) throws IOException{
	
        //final SecurityContext securityContext = requestContext.getSecurityContext();
       /* String token = requestContext.getHeaderString("jwt");
        
        if (token==null || token.isEmpty()){
			Response unauthorizedStatus = Response.status(Response.Status.UNAUTHORIZED)
					  .entity("No Autorizado")
					  .build();

			requestContext.abortWith(unauthorizedStatus);          	
        }
        else {
            Parametros par = new Parametros();
            WordPressAPI api = new WordPressAPI();
            api.validate(token, par.getParameter("urlWPValidateToken"));
            if (!api.isTokenValid()){
    			Response unauthorizedStatus = Response.status(Response.Status.UNAUTHORIZED)
    					  .entity("No Autorizado")
    					  .build();

    			requestContext.abortWith(unauthorizedStatus);     
            }        	
        	
        	
        }*/
        


		System.out.println("INTERCEPTOR");
		
		

	}
	
}
