package cl.effingo.api.controller;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import cl.effingo.dao.*;
import cl.effingo.model.TbImagen;
import cl.effingo.model.TbRegistroEmocion;

import cl.effingo.utils.*;



@Path("/")
public class ImageController {
	@GET
	@Path("/image/{uuid}")
	@Produces("image/jpeg")
    public byte[] get(@PathParam("uuid") String uuid) {
        BufferedImage bImage = null;
        Parametros par = new Parametros();
    	DataService ds = new DataService(par.getEnviroment());
    	TbImagen emo = ds.getEmotionRegisterByUUID(uuid);
    	File file = new File(emo.getPATH_IMAGEN());
    	
    	
        ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
        
        try {
        	bImage = ImageIO.read(file);
            ImageIO.write(bImage,"jpg",bo);
        } catch (IOException ex) {
            return null;
        }
        ds.close();
        return bo.toByteArray();    	
    }
	
}
