package cl.effingo.api.controller;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import cl.effingo.api.model.AnswersDTO;
import cl.effingo.api.model.AnwsersHabitDTO;
import cl.effingo.api.model.EvaluationDTO;
import cl.effingo.api.model.FechaIniDTO;
import cl.effingo.api.model.FileUploadForm;
import cl.effingo.api.model.ProgresoDTO;
import cl.effingo.api.model.ProgressDTO;
import cl.effingo.api.model.Subscription;
import cl.effingo.dao.*;
import cl.effingo.model.AuthDTO;
import cl.effingo.model.Emotion;
import cl.effingo.model.Habito;
import cl.effingo.model.StepDTO;
import cl.effingo.model.TbCapsulaLiberada;
import cl.effingo.model.TbCapsulas;
import cl.effingo.model.TbCiclo;
import cl.effingo.model.TbCicloCapsulaUsuario;
import cl.effingo.model.TbEvaluacionInicial;
import cl.effingo.model.TbImagen;
import cl.effingo.model.TbPasosIniciales;
import cl.effingo.model.TbPreguntasPushRespuestas;
import cl.effingo.model.TbProgresoCiclo;
import cl.effingo.model.TbRegistroEmocion;
import cl.effingo.model.TbRespuestaQuizz;
import cl.effingo.model.UserWP;
import cl.effingo.services.FaceAPI;
import cl.effingo.services.PushAPI;
import cl.effingo.services.WordPressAPI;
import cl.effingo.utils.*;



@Path("/")
public class WpUsersController {

	private File fileUploadPath;
	
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
    	
        if (users!=null) {
        	System.out.println("USUARIO REGISTRADO");
    		status="true";
    		msg="OK";
			users.setFirst_name(user.getFirt_name());
			users.setLast_name(user.getLast_name());
            
        } else {
    		status="false";
    		msg=ds.errorMessage;
    		System.out.println("USUARIO NO REGISTRADO:" + msg);
        }
        ds.commit();
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
			String autCode = req.getHttpHeaders().getHeaderString("uuid");
			if (api.isAuthenticated()){
				
				
				
				users = ds.geCheckUserById(api.getUserWP().getUserid(),autCode);
				users.setToken(api.getToken());

				status="true";
				msg="OK";

			}
			else {
				msg=api.messageError;
				status="false";
			}
			

		} catch (UnsupportedEncodingException e) {
    		status="false";
    		msg=e.getMessage();   		
    	}
    	
    	/*if (api.isAuthenticated()){
    		users = ds.getUserById(api.getUserWP().getUserid());
    		users.setToken(api.getToken());
    	}
    	else {
    		status="false";
    		msg=api.messageError;   		
    	} */
    	
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(users,status,msg));
        return rb.build();
    }	
	 

	
	@Path("/v1/authenticated/step")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStep(StepDTO paso,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        paso.setId(user.getUserid());
        
        
        
    	DataService ds = new DataService(par.getEnviroment());
    	TbPasosIniciales pi = new TbPasosIniciales();
    	if (paso.getNombre_paso_inicial().equals("VIDEO_INICIAL")){
    		pi.setVIDEO_INICIAL("S");
    	}
    	
    	if (paso.getNombre_paso_inicial().equals("FOTO_INICIAL")){
    		pi.setFOTO_INICIAL("S");
    	} 
    	if (paso.getNombre_paso_inicial().equals("PREGUNTAS_INICIALES")){
    		pi.setPREGUNTAS_INICIALES("S");
    	}    	
    	pi.setID(paso.getId());    	
    	pi.setID_PASO_INICIAL(paso.getId_paso_inicial());
    	
    	int s= ds.UpdateStep(pi);
    	if (s>0){
			msg="OK"; 
			status="true";
    	}
    	else {
    		msg=ds.errorMessage;
    		status="false";
    	
    	}
    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(paso,status,msg));
        return rb.build();
    }	
	
	
	@Path("/v1/authenticated/emotion")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes("multipart/form-data")
    public Response updatePicture(@MultipartForm FileUploadForm  form,@Context HttpRequest req) {
        String status="false";
        String msg="Ha ocurrido un error";
        WordPressAPI api = new WordPressAPI();
        FaceAPI emocionesApi = new FaceAPI(par);
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        JSONUtil jsonParser= new JSONUtil();
        Response.ResponseBuilder rb=null;
        if (ds.canTakePicture(user.getUserid(), par)){
        	
        	String ruta = par.getParameter("RutaFS");
        	FS fs = new FS(ruta);
        	fileUploadPath = new File(fs.getFolderPath());
        	String nombreArchivo = String.valueOf(UUID.randomUUID());
            try
            {
                File file = new File(fileUploadPath, nombreArchivo);  
                if (!file.exists()) 
                {
                    file.createNewFile();
                }
                
                TbImagen imagen= new TbImagen();
                imagen.setUUID(nombreArchivo);
                imagen.setPATH_IMAGEN(file.getAbsolutePath());
                ds.insertImagen(imagen);
                ds.commit();

               
                
                InputStream in = new ByteArrayInputStream(form.getFileData());
                
                
                BufferedImage bImageFromConvert = ImageIO.read(in);
                ImageIO.write(bImageFromConvert, "jpg", file);
                in.close();
                
                String url=par.getParameter("URLHost")+nombreArchivo;

                System.out.println("FILE:" + file.getAbsolutePath());
                System.out.println("URL:" + url);
               // System.out.println("EMOCION-1:"  );
                //Emotion emocion1 = emocionesApi.getEmotionsByImageURL(url);
                
                
                //System.out.println("1:" + emocion1.toString());
                System.out.println("EMOCION-2:"  );
                Emotion emocion2 = emocionesApi.getEmotionsByImage(file.getAbsolutePath());
                System.out.println("2:" + emocion2.toString());
                //Emotion emocion = emocionesApi.getEmotionsByImage(form.getFileData());

                
                if (emocion2!=null){
	                TbProgresoCiclo progreso = ds.getUserById(user.getUserid()).getProgresoCiclo();
	                TbRegistroEmocion emo = new TbRegistroEmocion();
	                emo.setANGER(emocion2.getAnger());
	                emo.setCONTEMPT(emocion2.getContempt());
	                emo.setDISGUST(emocion2.getDisgust());
	                emo.setFEAR(emocion2.getFear());
	                emo.setNEUTRAL(emocion2.getNeutral());
	                emo.setSADNESS(emocion2.getSadness());
	                emo.setHAPPINESS(emocion2.getHappiness());
	                emo.setSURPRISE(emocion2.getSurprise());
	                emo.setID(user.getUserid());
	                emo.setID_CICLO(progreso.getID_CICLO());
	                emo.setPATH_IMAGEN(file.getAbsolutePath());
	                
	                ds.registerEmotion(emo, par);
	                TbProgresoCiclo progresoParaActualizar = new TbProgresoCiclo();
	                progresoParaActualizar.setFOTOS_DIARIAS(1f);
	                progresoParaActualizar.setID_CICLO(progreso.getID_CICLO());
	                progresoParaActualizar.setID(progreso.getID());
	                ds.UpdateProgresoWithoutDate(progresoParaActualizar);
	                /*TbPasosIniciales pi = ds.getPasosInicialesByUserId(progreso.getID());
	                pi.setFOTO_INICIAL("S");
	                int s= ds.UpdateStep(pi);*/
	                msg="OK";
	                status="true";	                
	                
	                rb = Response.ok(jsonParser.JSONDataSingle(emo,status,msg));

	                
                }
                else {
                	msg="No se ha podido determinar la emocion de la foto";
                    rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
                   // return rb.build();	                	
                }
                
            } 
            catch (IOException e)
            {
            	System.out.println(e.getMessage());
                rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
                //return rb.build();
            }          	

        }
        else {
        	msg=ds.errorMessage;;
            rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));

        }
        ds.commit();
    	ds.close();
        return rb.build();
    }		
	
	
	@Path("/v1/user/validateSession")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateSession(@Context HttpRequest req) {
       
		JSONUtil jsonParser= new JSONUtil();
        String status="false";
        String msg="Error";
		String token = req.getHttpHeaders().getHeaderString("jwt");
		WordPressAPI api = new WordPressAPI();
		try {
            Parametros par = new Parametros();
            api.validate(token, par.getParameter("urlWPValidateToken"));
            if (api.isTokenValid()){
                status="true";
                msg="OK";

            } 				
			
		}
		catch(Exception e){
			status="false";
			msg=api.messageError;
		}


    	jsonParser= new JSONUtil();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }	
	
	@Path("/v1/authenticated/habit")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response habit(@Context HttpRequest req) {
       
		JSONUtil jsonParser= new JSONUtil();
        String status="false";
        String msg="Error";
        Habito habito = null;
		String jwt = req.getHttpHeaders().getHeaderString("jwt");
		
		WordPressAPI api = new WordPressAPI();
		UserWP user = api.getWPUserByToken(jwt);
		DataService ds = new DataService(par.getEnviroment());
		try {
            status="true";
            msg="OK";
            habito = ds.getHabit(user.getUserid());
		}
		catch(Exception e){
			status="false";
			msg=ds.errorMessage;
		}


    	jsonParser= new JSONUtil();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(habito,status,msg));
        return rb.build();
    }		
	
	
	
	@Path("/v1/authenticated/evaluation")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response evaluation(EvaluationDTO eva,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        TbProgresoCiclo progreso = ds.getUserById(user.getUserid()).getProgresoCiclo();
        Long id_ciclo = progreso.getID_CICLO();
   
        
        String[] preguntas =eva.getPreguntas().split(",");
        String[] valores =eva.getValores().split(",");
        boolean ok=false;
        try {
            for (int i=0;i< preguntas.length;i++){
            	TbEvaluacionInicial record = new TbEvaluacionInicial();
            	record.setID_CICLO(id_ciclo);
            	record.setID_PREGUNTA(Integer.valueOf(preguntas[i]));
            	record.setVALOR_RESPUESTA(Integer.valueOf(valores[i]));
            	record.setID(user.getUserid());
            	ds.registerEvaluation(record);
            }	
            ok=true;
        	if (ok){
    			msg="OK"; 
    			status="true";
        	}
        	else {
        		msg=ds.errorMessage;
        		status="false";
        	
        	}        	
            
        }
        catch(Exception e){
    		msg=ds.errorMessage;
    		status="false";
        }


    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }	
	
	
	@Path("/v1/authenticated/progress")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response progress(ProgressDTO eva,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        try {
            TbProgresoCiclo progreso = ds.getUserById(user.getUserid()).getProgresoCiclo();
            TbProgresoCiclo progresoItem = new TbProgresoCiclo();
        	progresoItem.setID(user.getUserid());

        	progresoItem.setID_CICLO(progreso.getID_CICLO());
        	progresoItem.setID_CICLO_ACTIVO(progreso.getID_CICLO_ACTIVO());
            String item = eva.getItemtocomplete();
            
            if ("CAPSULA1".equals(item)){
            	progresoItem.setCAPSULA1("S");
            }
            if ("CAPSULA2".equals(item)){
            	progresoItem.setCAPSULA2("S");
            }  
            if ("CAPSULA3".equals(item)){
            	progresoItem.setCAPSULA3("S");
            } 
            if ("CAPSULA4".equals(item)){
            	progresoItem.setCAPSULA4("S");
            } 
            if ("FOTOS_DIARIAS".equals(item)){
            	progresoItem.setFOTOS_DIARIAS(1f);
            }   
            if ("PREGUNTAS_DIARIAS".equals(item)){
            	progresoItem.setPREGUNTAS_DIARIAS(1f);
            }   
            if ("PREGUNTAS_SEMANALES".equals(item)){
            	progresoItem.setPREGUNTAS_SEMANALES(1f);
            }         
            
            ds.UpdateProgresoWithoutDate(progresoItem); 
            status="true";
            msg="OK";
        }
        catch(Exception e){
            status="false";
            msg="Error";
        }



    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }	
		
	
	@Path("/v1/authenticated/answers")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response answers(AnswersDTO resp,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
   
        TbProgresoCiclo progreso = ds.getUserById(user.getUserid()).getProgresoCiclo();
        
        
        /*Para eviar sumar el anterio*/
        progreso.setPREGUNTAS_DIARIAS(0f);
        progreso.setPREGUNTAS_SEMANALES(0f);
        progreso.setFOTOS_DIARIAS(0f);
        
        
        Long id_ciclo = progreso.getID_CICLO();

        
        // Progreso 
        
        
        
        String[] alternativas=resp.getAlternativas().split("\\|");
        String[] correctas =resp.getCorrectas().split("\\|");
        String capsulas = resp.getCapsulas();
        String quizzes = resp.getQuizzes();
        String[] puntajes = resp.getPuntajes().split("\\|");
        String[] preguntas = resp.getPreguntas().split("\\|");
       
        TbCapsulas capsula = ds.getCapsulaById(Long.valueOf(capsulas));
        
        Long idCapsula =Long.valueOf(capsulas);
        TbRespuestaQuizz forIteracion = new TbRespuestaQuizz();
        forIteracion.setID(user.getUserid());
        forIteracion.setID_CAPSULA(idCapsula);
        forIteracion.setID_QUIZZ_WP(Long.valueOf(quizzes));
        
		Long iteracion = ds.getIterationAnswersQuiz(forIteracion);
		
		Float calculoPuntaje=0f;
		try {
	        for (int i=0;i<preguntas.length;i++){
	        	TbRespuestaQuizz record = new TbRespuestaQuizz();
	        	record.setID(user.getUserid());
	        	record.setID_CAPSULA(Long.valueOf(capsulas));
	        	record.setID_QUIZZ_WP(Long.valueOf(quizzes));
	        	record.setALTERNATIVA(alternativas[i]);
	        	
	        	short c = 0;
	        	if (correctas[i].equals("true")){
	        		c=1;
	        	}
	        	record.setCORRECTO(c);
	        	
	        	record.setID_PREGUNTA_WP(Long.valueOf(preguntas[i]));
	        	record.setITERACION(iteracion);
	        	record.setPUNTAJE(Float.valueOf(puntajes[i]));
	        	calculoPuntaje += Float.valueOf(puntajes[i]);
	        	
	        	ds.registerAnswersQuiz(record);
        	
	        	
	        }
	        msg="NOOK";
	        
	        if (Float.compare(capsula.getMINIMO_APROBACION(),calculoPuntaje)<=0){
	        	// Capsula Aprobada
	        	// Habilitar segunda Capsula
	        	
	        	msg="OK";
	        	
	        	try {
		        	TbCicloCapsulaUsuario capsulaSiguiente = ds.getNextCapsulaById(Long.valueOf(capsulas), id_ciclo, user.getUserid());
	 	        	capsulaSiguiente.setESTADO("A");
		        	ds.activateNexCapsula(capsulaSiguiente);	        		
	        	}
	        	catch(Exception e){
	        		;;
	        	}


	        	// Liberar capsula
	        	TbCapsulaLiberada capLiberada = new TbCapsulaLiberada();
	        	capLiberada.setID(user.getUserid());
	        	capLiberada.setID_CAPSULA(idCapsula);
	        	ds.releaseCapsula(capLiberada);

	        	
	        	// Validar progreso ciclo para terminar el modulo
	        }
	        
	        if (resp.getNombreQuizz().equals("QUIZZ1")){ 
	        	progreso.setQUIZZ1("S");
	        	if (Float.compare(capsula.getMINIMO_APROBACION(),progreso.getPUNTAJE_QUIZZ1())>0){
	        		progreso.setPUNTAJE_QUIZZ1(calculoPuntaje);
	        	}
	        	
	        	
	        }
	        if (resp.getNombreQuizz().equals("QUIZZ2")){ 
	        	progreso.setQUIZZ2("S");
	        	if (Float.compare(capsula.getMINIMO_APROBACION(),progreso.getPUNTAJE_QUIZZ2())>0){
	        		progreso.setPUNTAJE_QUIZZ2(calculoPuntaje);
	        	}	        	
	        	
	        }
	        if (resp.getNombreQuizz().equals("QUIZZ3")){ 
	        	progreso.setQUIZZ3("S");
	        	if (Float.compare(capsula.getMINIMO_APROBACION(),progreso.getPUNTAJE_QUIZZ3())>0){
	        		progreso.setPUNTAJE_QUIZZ3(calculoPuntaje);
	        	}		        	
	        }
	        if (resp.getNombreQuizz().equals("QUIZZ4")){ 
	        	progreso.setQUIZZ4("S");
	        	if (Float.compare(capsula.getMINIMO_APROBACION(),progreso.getPUNTAJE_QUIZZ4())>0){
	        		progreso.setPUNTAJE_QUIZZ4(calculoPuntaje);
	        	}	        	
	        }	        
	        
	        
	        // Preguntar que si se cumplen las condiciones para terminar el ciclo
	        
	        
	        ds.UpdateProgresoWithoutDate(progreso);
  			status="true";
			
		}
		catch(Exception e){
			e.printStackTrace();
    		msg=e.getMessage();
    		status="false";
			ds.rollback();
		}

    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }		
	
	@Path("/v1/authenticated/habittest")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response habittest(AnwsersHabitDTO resp,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        Long id = user.getUserid();
        TbProgresoCiclo progreso = ds.getUserById(id).getProgresoCiclo();
        
        
        /*Para eviar sumar el anterior*/
        progreso.setFOTOS_DIARIAS(0f);
        progreso.setPREGUNTAS_DIARIAS(0f);
        progreso.setPREGUNTAS_SEMANALES(0f);
     
        
   
        Long id_ciclo = progreso.getID_CICLO();

        
        try {
	        String[] alternativas=resp.getAlternativas().split("\\|");
	        String capsulas= resp.getCapsulas();
	        String[] puntajes = resp.getPuntajes().split("\\|");
	        String[] preguntas = resp.getPreguntas().split("\\|");
	        String[] repuestas = resp.getRespuestas().split("\\|");
	        String frecuencia =resp.getFrecuencia();
	
			for (int i=0;i<preguntas.length;i++){
				TbPreguntasPushRespuestas record = new TbPreguntasPushRespuestas();
				record.setID(id);
				record.setID_ALTERNATIVA(Integer.valueOf(alternativas[i]));
				record.setID_CAPSULA(Long.valueOf(capsulas));
				record.setID_CICLO(id_ciclo);
				record.setID_PREGUNTA_PUSH(Integer.valueOf(preguntas[i]));
				record.setID_RESPUESTA_PUSH(Long.valueOf(repuestas[i]));
				record.setVALOR_RESPUESTA(Integer.valueOf(puntajes[i]));
				
				ds.UpdateAnswerQuestion(record);
			}
			
			if (ds.hasMoreQuestions(id,frecuencia)==0){
				if (frecuencia.equals("DIARIA")){
			        progreso.setPREGUNTAS_DIARIAS(1f);
				}
				if (frecuencia.equals("SEMANAL")){
					progreso.setPREGUNTAS_SEMANALES(1f);
				}			
			}
			ds.UpdateProgresoWithoutDate(progreso);
  			status="true";
			
		}
		catch(Exception e){
			e.printStackTrace();
    		msg="Error al Enviar las Respuestas";
    		status="false";
			ds.rollback();
		}

    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }	
	
	@Path("/v1/authenticated/initiate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initiate(FechaIniDTO fec,@Context HttpRequest req) {
        String status="false";
        String msg="Error";
        
        /*Extraer el ID del Usuario apartir del JWT*/
        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        try {
            TbProgresoCiclo progreso = ds.getUserById(user.getUserid()).getProgresoCiclo();
            TbProgresoCiclo progresoItem = new TbProgresoCiclo();
        	progresoItem.setID(user.getUserid());

        	progresoItem.setID_CICLO(progreso.getID_CICLO());
        	progresoItem.setID_CICLO_ACTIVO(progreso.getID_CICLO_ACTIVO());
        	if (fec.getFechaIni()!=null){
        		progresoItem.setFECHA_INICIO(fec.getFechaIni());
        	}

            ds.UpdateProgreso(progresoItem); 
            status="true";
            msg="OK";
        }
        catch(Exception e){
            status="false";
            msg="Error";
        }



    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle("",status,msg));
        return rb.build();
    }	
	
	@Path("/v1/authenticated/emotion/lastrecord")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response lastrecord(@Context HttpRequest req) {
        String status="false";
        String msg="Error";

        WordPressAPI api = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = api.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        TbRegistroEmocion record=null;
        try {
        	record= ds.getLastEmotionRecordByUser(user.getUserid());
            status="true";
            msg="OK";
        }
        catch(Exception e){
            status="false";
            msg="Error";
        }



    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(record,status,msg));
        return rb.build();
    }	
	
	
	@Path("/v1/authenticated/key")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response key(@Context HttpRequest req) {
        PushAPI api = new PushAPI(par.getParameter("push-service-key"));
        System.out.println("parametro:" +par.getParameter("push-service-key"));
        System.out.println("key:" +api.getKey());
        Response.ResponseBuilder rb = Response.ok(api.getKey());
        return rb.build();
    }
	
	
	
	@Path("/v1/authenticated/subscribe")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response subscribe(Subscription subscription,@Context HttpRequest req) {
		
		String autCode = req.getHttpHeaders().getHeaderString("uuid");
        String status="false";
        String msg="Error";
        WordPressAPI apiwp = new WordPressAPI();
        String jwt = req.getHttpHeaders().getHeaderString("jwt");
        UserWP user = apiwp.getWPUserByToken(jwt);
        DataService ds = new DataService(par.getEnviroment());
        try {
        	ds.updateAuthCode(autCode, user.getUserid(), subscription.toString());
            status="true";
            msg="OK";
        }
        catch(Exception e){
            status="false";
            msg="Error";
        }        

    	JSONUtil jsonParser= new JSONUtil();
    	ds.commit();
    	ds.close();
        Response.ResponseBuilder rb = Response.ok(jsonParser.JSONDataSingle(subscription,status,msg));
        return rb.build();
    }		

	
	
	
	
	
	
	/**@RequestBody Subscription */
}
