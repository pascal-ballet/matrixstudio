package matrixstudio.model;

import java.util.ArrayList;
import java.util.List;

import org.jocl.cl_platform_id;

import matrixstudio.kernel.CLUtil;

/**
 * <b>Classe Platform<b>
 * <p>La classe Platform permet de récupérer l'id de la platform
 * 
 * @author lesech
 */

/**
 * 
 */

public class Platform {
	
	/**
	 * Id de platform
	 * 
	 * @see Platform#getPlatformId()
	 * @see Platform#setPlatformId(cl_platform_id)
	 * @see Platform#setPlatformId(int)
	 */
	private cl_platform_id platformId;
	
	//List<cl_platform_id> listPlatform;
	
	/**
	 * 
	 * @param platformId constructeur de la classe Platform avec en paramètre une variable de type cl_platform
	 */

	public Platform(cl_platform_id platformId) {
		//this.listPlatform = new ArrayList<>();
		//listPlatform.add(platformId);
		this.platformId = platformId;
	}
	
	/**
	 * Constructeur par défaut de la classe platform
	 * 
	 * <p>Constructeur par défaut de la classe platforme<p>
	 */
	public Platform() {
		//this.listPlatform = new ArrayList<>();
		this.platformId = null;
	}
	
	/**
	 * 
	 * @return Getter de l'attribut platformId
	 */
	public cl_platform_id getPlatformId() {
		return this.platformId;
	}
	
	/**
	 * 
	 * @param platformId mofifier la valeur de platformID à partir du paramètre 
	 */
	
	public void setPlatformId(cl_platform_id platformId) {
		this.platformId = platformId;
	}

	/**
	 * 
	 * @param index modifier platformId à partir d'un index 
	 */
	
	public void setPlatformId(int index) {
		List<cl_platform_id> listPlatform = CLUtil.getListPlatform();
		cl_platform_id res = listPlatform.get(index);
		
		this.platformId = res;
		
	}
	
}
