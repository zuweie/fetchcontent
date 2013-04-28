package oz.fetchcontent.analysis;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.horrabin.horrorss.RssItemBean;

import oz.fetchcontent.datax.Datax;
import oz.fetchcontent.datax.Sqlmaker;
import oz.fetchcontent.datax.kv;
import oz.fetchcontent.main.Log;
import oz.fetchcontent.main.Util;
import oz.fetchcontent.main.rs;

public class Webcontent {
	
	public Webcontent(RssItemBean bean){
		this.bean = bean;
		process = false;
		content = new StringBuffer();
		
	}
	public void resetRssItembean(RssItemBean bean){
		this.bean = bean;
		process = false;
		content.delete(0, content.length());
		Author = null;
	}
	
	public boolean processBean() 
			throws IOException, URISyntaxException, StringIndexOutOfBoundsException, 
			IndexOutOfBoundsException, IllegalStateException{
		
		if (bean != null && process == false){
			// TODO : process the bean -- fetch the web content;
			String link = bean.getLink();
			// TODO : fix the tap problem;
			//String link = "http://jianshu.io/p/PNq2y9";
			/*
			if(link.indexOf("https") >=0){
				//link = link.replaceFirst("https", "http");
				return process;
			}
			*/
			String html = Apachehttpclient.getInstance().get(link);
			URI uri = new URI(link);
			String domain = uri.getScheme()+"://"+uri.getHost();
			content.delete(0, content.length());
			//content.append(TextExtract.parse(html).replace("\'", "\\\'"));
			// 都不知到这个sting类的替换干什么鸟，真操蛋，把他换成中文单引号算求啦。
			//content.append(TextExtract.parse(html, domain).replace("\'", "‘"));
			content.append(TextExtract.parse(html, domain).replace("\'", "\\\'"));
			process = true;
		}
		return process;
	}
	
	
	public void saveContenttoDB() throws SQLException, NoSuchAlgorithmException{
		if (process){
			List<kv> values = makeSavevalues();
			String sql = Sqlmaker.insertNewstoDB(values, rs.NEWSTAB);
			Datax.getInstance().updateDatatoDB(sql);
		}
	}
	
	public List<kv> makeSavevalues() 
			throws NoSuchAlgorithmException{
		if (process){
			List<kv> values = new ArrayList<kv>();
			kv v = new kv(rs.ORILINK, bean.getLink());
			values.add(v);
			
			String linkhash = Util.md5(bean.getLink());
			v = new kv(rs.LINKHASH, linkhash);
			values.add(v);
			
			if (content.length() > 0){
				v = new kv(rs.CONTENT, content.toString());
				values.add(v);
			}
			
			v = new kv(rs.TITLE, bean.getTitle().replaceAll("\'", "‘"));
			values.add(v);
			
			if (Author != null){
				v = new kv(rs.AUTHOR, Author);
				values.add(v);
			}
			
			int status = inspectContent();
			v = new kv(rs.STATUS, status);
			values.add(v);
			return values;
		}
		return null;
	}
	
	public int inspectContent(){
		if (content.length() == 0)
			return rs.FAIL;
		else
			return rs.PASS;
	}
	
	public String getLinkhash() throws NoSuchAlgorithmException{
		return Util.md5(bean.getLink());
	}
	
	public void reset(){
		process = false;
		content.delete(0,  content.length());
		Author = null;
		//bean = null;
	}
	
	RssItemBean bean = null;
	boolean process = false;
	StringBuffer content = null;
	String Author = null;
}
