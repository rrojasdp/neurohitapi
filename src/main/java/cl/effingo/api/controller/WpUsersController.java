package cl.effingo.api.controller;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.json.JSONObject;

import com.google.gson.Gson;

import javax.ws.rs.core.MediaType;




import cl.effingo.dao.*;
import cl.effingo.model.UserWP;
import cl.effingo.api.model.WpUsers;
import cl.effingo.utils.*;



@Path("/v1/user")
public class WpUsersController {

	
	Parametros par = new Parametros();
	


	
	@Path("/regcode")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRegCode(String uuid) {
        String status="false";
        String msg="Error";
    	DataService ds = new DataService(par.getEnviroment());
    	JSONObject json = new JSONObject(uuid);
    	JSONUtil jsonParser= new JSONUtil();
    	
    	String uid = json.getString("uuid");
        boolean result = ds.generateAuthCode(uid);
        ds.commit();
        ds.close();
        if (result) {
        	System.out.println("CREADO REGCODE");
            //return Response.ok().status(Response.Status.CREATED).build();
    		status="true";
    		msg="OK";
            Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(json,status,msg));
            return rb.build();
            
        } else {
        	System.out.println("NO CREADO REGCODE");
            return Response.notModified().build();
        }
    }
	
	@Path("/register")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(UserWP user,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
    	DataService ds = new DataService(par.getEnviroment());
    	JSONUtil jsonParser= new JSONUtil();
    	String autCode = req.getHttpHeaders().getHeaderString("uuid");
    	System.out.println("autCode:"+autCode);
    	cl.effingo.model.WpUsers users = ds.doRegister(autCode, user, par);
    	
        ds.commit();
        ds.close();
        if (users!=null) {
        	System.out.println("USUARIO REGISTRADO");
    		status="true";
    		msg="OK";
            
        } else {
    		status="false";
    		msg=ds.errorMessage;
    		System.out.println("USUARIO NO REGISTRADO:" + msg);
        }
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(users,status,msg));
        //Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(user,status,msg));
        return rb.build();
    }
	 
    
	/*
	@POST
    @Path("/register")
    @Produces("application/json")
   

    public Response register (@RequestBody WpUsers wpusers , @Context HttpRequest req){
 		


	    TokenKC kc = null;
		try {
			kc = new TokenKC(req);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return SERVER_ERROR;
		}
	    
	    
	    
        System.out.println("SRV(email):"+kc.getParameter("email"));
        System.out.println("SRV(User):"+ kc.getParameter("preferred_username"));
        System.out.println("SRV(resource_access):"+kc.getParameter("resource_access"));  	
    	
    	
    	
    	
    	
    	
		System.out.println("ENTRANDO AL SERVICIO:");
        DataService ds = new DataService(par.getEnviroment()); 
        String status="false";
        String msg="Error";
        JSONUtil json= new JSONUtil();
        TbTaVehiculo v = null;
        try {
        	v = ds.getCarById(vehiculeId);
        	if (v==null){
        		msg="Vehiculo no Existe";
        	}
        	else {
        		status="true";
        		msg="OK";
        	}
     	
        }
        catch (Exception e){
        	msg="Error en Clase:" + this.getClass().getName() +", Metodo:" + this.getClass().getEnclosingMethod().getName();
        }
        finally {
        	ds.close();
        }
        System.out.println("JSON:" +json.JSONDataSingle(v,status,msg));
        Response.ResponseBuilder rb = Response.ok(json.JSONDataSingle(v,status,msg));
        return rb.build();
	} 
		*/
	
}
