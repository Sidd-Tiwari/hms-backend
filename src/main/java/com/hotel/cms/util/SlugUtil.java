package com.hotel.cms.util;
public final class SlugUtil {
 private SlugUtil(){}
 public static String toSlug(String v){ return v==null?null:v.trim().toLowerCase().replaceAll("[^a-z0-9\\s-]","").replaceAll("\\s+","-").replaceAll("-+","-"); }
}
