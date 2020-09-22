package cl.effingo.api.interceptors;


import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import javax.ws.rs.ext.Provider;

import cl.effingo.services.WordPressAPI;
import cl.effingo.utils.JSONUtil;
import cl.effingo.utils.Parametros;


@Provider
public class SecurityFilter implements ContainerRequestFilter{
	

	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void filter (ContainerRequestContext requestContext) throws IOException{
	
		
	
		String url = requestContext.getUriInfo().getPath().toString();
		System.out.println("URL:" +url);
		
		if (url.indexOf("/v1/authenticated/")>-1){
			JSONUtil jsonParser= new JSONUtil();
			String status="false";
			String token = requestContext.getHeaderString("jwt");
			System.out.println("Token:" + token);
			try {
	            Parametros par = new Parametros();
	            WordPressAPI api = new WordPressAPI();
	            api.validate(token, par.getParameter("urlWPValidateToken"));
	            if (!api.isTokenValid()){
	            	String msg=Response.Status.UNAUTHORIZED.toString();
	    			Response unauthorizedStatus = Response.status(Response.Status.UNAUTHORIZED)
	    					  .entity(jsonParser.JSONDataSingle("",status,msg))
	    					  .build();

	    			requestContext.abortWith(unauthorizedStatus); 

	            } 				
				
			}
			catch(Exception e){
    			Response unauthorizedStatusError = Response.status(Response.Status.UNAUTHORIZED)
  					  .entity(jsonParser.JSONDataSingle("",status,e.getMessage()))
  					  .build();
    			
    			requestContext.abortWith(unauthorizedStatusError); 
			}
		}


		System.out.println("INTERCEPTOR");
		
		

	}
	
}
