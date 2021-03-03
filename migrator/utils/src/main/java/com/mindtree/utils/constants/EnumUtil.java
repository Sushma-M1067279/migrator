package com.mindtree.utils.constants;

import java.util.EnumSet;

public class EnumUtil {
	
	private EnumUtil() {
	}
	
	public enum STATUS {
		NEW, PROCESSING, SUBMITTED, RETRY, COMPLETE, FAIL
	}
	
	public enum SOCIAL {
		FACEBOOK, INSTAGRAM, SNAPCHAT, PINTEREST, YOUTUBE
	}
	
	  public static <E extends Enum<E>> boolean contains(Class<E> enumClass,
	      String value) {
	    try {
	      return EnumSet.allOf(enumClass)
	          .contains(Enum.valueOf(enumClass, value));
	    } catch (Exception e) {
	      return false;
	    }
	  }
}
