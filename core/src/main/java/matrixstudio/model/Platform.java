package matrixstudio.model;

import java.util.ArrayList;
import java.util.List;

import org.jocl.cl_platform_id;

import matrixstudio.kernel.CLUtil;

/**
 * <b>Classe Platform<b>
 * <p>The Platform class is used to retrieve the platform id<p>
 * 
 * @author lesech
 */

/**
 * 
 */

public class Platform {
	
	/**
	 * platform id
	 * 
	 * @see Platform#getPlatformId()
	 * @see Platform#setPlatformId(cl_platform_id)
	 * @see Platform#setPlatformId(int)
	 */
	private cl_platform_id platformId;
	
	/**
	 * Constructor of the Platform class with a variable of type cl_platform as parameter
	 * @param platformId 
	 */

	public Platform(cl_platform_id platformId) {
		this.platformId = platformId;
	}
	
	/**
	 * 
	 * <p>Default constructor of the platform class<p>
	 */
	public Platform() {
		this.platformId = null;
	}
	
	/**
	 * get platform id's
	 * @return
	 */
	public cl_platform_id getPlatformId() {
		return this.platformId;
	}
	
	/**
	 * set platform Id
	 * @param platformId
	 */
	
	public void setPlatformId(cl_platform_id platformId) {
		this.platformId = platformId;
	}

	/**
	 * set platformId with Index
	 * @param index 
	 */
	
	public void setPlatformId(int index) {
		List<cl_platform_id> listPlatform = CLUtil.getListPlatform();
		cl_platform_id res = listPlatform.get(index);
		
		this.platformId = res;
		
	}
	
}
