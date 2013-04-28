package oz.fetchcontent.datax;

import java.util.List;
import oz.fetchcontent.main.rs;

public class Sqlmaker {
	public static String queryNewsBylinkhash(String[] urlhashes){
		return null;
	}
	
	public static String queryNewsBylinkhash(String urlhash){
		String sql = " select * from " + rs.NEWSTAB + " where " + rs.LINKHASH + " = \'" + urlhash + "\'";
		return sql;
	}
	
	public static String insertNewstoDB(List<kv> values, String tab){
		if (values.size() > 0){
			StringBuffer colname = new StringBuffer();
			StringBuffer colvalue = new StringBuffer();
			
			for (int i=0; i<values.size(); ++i){
				
				colname.append(values.get(i).getKey() + ",");
				colvalue.append("\'"+values.get(i).getValueString()+"\',");	
			}
			
			colname.deleteCharAt(colname.length()-1);
			colvalue.deleteCharAt(colvalue.length()-1);
			
			String sql = " insert into " + tab + "("+colname+") value ("+colvalue+")";
			return sql;
		}
		return null;
	}
}
