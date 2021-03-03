package com.mindtree.transformer.factory;

import com.mindtree.bluenoid.business.BluenoidBusinessImpl;
import com.mindtree.holoxo.business.HoloxoBusinessImpl;
import com.mindtree.utils.business.IMigratorBusiness;
import com.mindtree.utils.constants.MigratorConstants;


/**
 * @author M1032046
 *
 */
public class MigratorBusinessFactory {

	public static IMigratorBusiness getMigratorBusiness(String brand) {

		if (brand == null) {
			return null;
		}
		if (brand.equalsIgnoreCase(MigratorConstants.BRAND_HOLOXO)) {
			return new HoloxoBusinessImpl();

		} else if (brand.equalsIgnoreCase(MigratorConstants.BRAND_BLUENOID)) {
			return new BluenoidBusinessImpl();

		} 

		return null;

	}

}
