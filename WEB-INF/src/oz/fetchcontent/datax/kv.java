package oz.fetchcontent.datax;

import java.util.List;

public class kv {
	
	String key;
	String sv = null;
	Integer iv = null;
	Float  fv = null;
	byte[] blob;
	
	public kv(String key, String value){
		this.key = key;
		this.sv = value;
	}
	
	public kv(String key, int value){
		this.key = key;
		this.iv = value;
	}
	
	public kv(String key, float value){
		this.key = key;
		this.fv = value;
	}
	
	public kv(String key, byte[] blob){
		this.key = key;
		this.blob = blob;
	}
	
	public String getKey(){
		return key;
	}
	
	public int getInt(){
		return this.iv;
	}
	
	public float getFloat(){
		return this.fv;
	}
	
	public String getString () {
		return this.sv;
	}
	
	public byte[] getByte () {
		return this.blob;
	}
	
	public String getValueString(){
		if (this.sv != null)
			return sv;
		if (this.iv != null)
			return String.valueOf(iv);
		if (this.fv != null)
			return String.valueOf(fv);
		if (this.blob != null)
			return new String (blob);
		else
			return null;
	}
	
	public static kv getkv(String key, List<kv> values){
		for (int i=0; i<values.size(); ++i){
			kv e = values.get(i);
			if (e.getKey().equals(key)){
				return e;
			}
		}
		return null;
	}
}
