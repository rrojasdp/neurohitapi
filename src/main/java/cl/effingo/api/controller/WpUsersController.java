package cl.effingo.api.controller;
import java.io.UnsupportedEncodingException;

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
import cl.effingo.model.AuthDTO;
import cl.effingo.model.UserWP;
import cl.effingo.services.WordPressAPI;
import cl.effingo.utils.*;



@Path("/")
public class WpUsersController {

	
	Parametros par = new Parametros();

	@Path("/v1/user/regcode")
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
	
	@Path("/v1/user/register")
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
	
	
	@Path("/v1/user/auth")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authUser(AuthDTO user,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
    	DataService ds = new DataService(par.getEnviroment());
    	JSONUtil jsonParser= new JSONUtil();
    	cl.effingo.model.WpUsers users=new cl.effingo.model.WpUsers();
    	WordPressAPI api = new WordPressAPI();
    	try {
    		System.out.println("email:" + user.getEmail()+ " password:" + user.getPassword());
			api.auth(user.getEmail(), user.getPassword(), par.getParameter("uriWPAuth"));
			status="true";
			msg="OK";
		} catch (UnsupportedEncodingException e) {
    		status="false";
    		msg=e.getMessage();   		
    	}
    	if (api.isAuthenticated()){
    		users = ds.getUserById(api.getUserWP().getUserid());
    		users.setToken(api.getToken());
    	}
    	else {
    		status="false";
    		msg=api.messageError;   		
    	}
    	
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(users,status,msg));
        return rb.build();
    }	
	 

}
