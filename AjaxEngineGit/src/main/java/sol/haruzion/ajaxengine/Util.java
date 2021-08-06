package sol.haruzion.ajaxengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;

import com.google.common.io.Files;

import sol.haruzion.ajaxengine.model.OsKind;

public class Util {
	public static String renameDuplicates(String dir,String fn) {
		String fnnoext=Files.getNameWithoutExtension(fn);
		String ext=Files.getFileExtension(fn);
	    int i=1;
	    File file = new File(dir + "/"+fn);
	    while (file.exists() && !file.isDirectory()) {
	    	file = new File(dir + "/"+fnnoext+"_"+i+"."+ext);
	        i++;
	        if(i>Integer.MAX_VALUE-100)return null;
	    }
	    return file.getName();
	}
	public static Map<String,Object> toMap(Object...objects){
		HashMap<String,Object> map=new HashMap<String, Object>();
		String lastkey=null;
		for (int i = 0; i < objects.length; i++) {
			Object obj=objects[i];
			int idx=i+1;
			if(idx%2==1) {
				lastkey=obj.toString();
			}else {
				map.put(lastkey, obj);
			}
		}
		return map;
	}
	public static String nvl(Object obj) {
		if(obj==null)return "";
		return obj.toString();
	}
	private static StandardPBEStringEncryptor encrypter() {
		String salt="AjaxEngine";
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(salt);
		encryptor.setAlgorithm("PBEWithMD5AndDES");
		encryptor.setSaltGenerator(new StringFixedSaltGenerator(salt));
		return encryptor;
	}
	public static String encrypt(String txt) {
		if(txt==null)return null;
		return encrypter().encrypt(txt);
	}
	public static String decrypt(String txt) {
		if(txt==null)return null;
		return encrypter().decrypt(txt);
	}
	public static String toStr(Object obj) {
		if(obj==null)return "";
		return obj.toString();
	}
	public static OsKind getOS() {
		String osname=System.getProperty("os.name").toLowerCase();
		if(osname.contains("win"))return OsKind.Windows;
		else if(osname.contains("mac"))return OsKind.Mac;
		else if(osname.contains("nix")&&osname.contains("aix")&&osname.contains("nux"))return OsKind.Unix;
		else if(osname.contains("sunos"))return OsKind.Solaris;
		else return OsKind.Linux;
	}
	public static double toDouble(Object obj) {
		return Double.parseDouble(obj+"");
	}
	public static String capitalize(String str) {
	    if(str == null || str.isEmpty()) {
	        return str;
	    }
	    return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	public static Map<String,Map<String,Object>> listToMap(String key,List<Map<String,Object>> list){
		Map<String,Map<String,Object>> rslt=new HashMap<String, Map<String,Object>>();
		for (Map<String, Object> map : list) {
			rslt.put((String) map.get(key),map);
		}
		return rslt;
	}
	public static Map<String, Map<String, String>> clone(Map<String, Map<String, String>> origin) {
		Map<String, Map<String, String>> map=new HashMap<>();
		for (Map.Entry<String, Map<String, String>> entry : origin.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
	public static List<Map<String, Object>> filterList(List<Map<String, Object>> list){
		final ArrayList<String> cols=new ArrayList<String>();
		list.forEach(map->{
			if(map!=null) {
				Iterator<Entry<String, Object>> ents = map.entrySet().iterator();
				ents.forEachRemaining(ent->{
					if(!cols.contains(ent.getKey()))cols.add(ent.getKey());
					if(ent.getValue()==null)map.put(ent.getKey(), "");
				});
			}
		});
		Map<String, Object>[] arr=list.toArray(new Map[0]);
		for (int i = 0; i < arr.length; i++) {
			if(arr[i]==null)arr[i]=new HashMap<>();
			for (String col : cols) {
				if(!arr[i].containsKey(col))arr[i].put(col, "");
			}
		}
		list=Arrays.asList(arr);
		return list;
	}
}
