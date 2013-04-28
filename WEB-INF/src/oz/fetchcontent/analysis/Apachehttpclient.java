package oz.fetchcontent.analysis;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class Apachehttpclient {
	static Apachehttpclient getInstance(){
		if (client == null){
			client = new Apachehttpclient();
		}
		return client;
	}
	
	public String get(String url) throws IOException{
		DefaultHttpClient client = getHttpclient();
		HttpGet request = new HttpGet(url);
		HttpResponse response;
		
		synchronized(client){
			response = client.execute(request);
		}
		
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity resEntity =  response.getEntity();
			return (resEntity == null) ? null : EntityUtils.toString(resEntity, HTTP.UTF_8);
		}
		return "fail!";
	}
	
	public String post(String url){
		return null;
	}
	
	DefaultHttpClient getHttpclient(){
		if (HttpClient == null){
			HttpParams params = new BasicHttpParams();
			
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			
			ConnManagerParams.setTimeout(params, 2000);
			HttpConnectionParams.setConnectionTimeout(params, 8000);
			HttpConnectionParams.setSoTimeout(params, 8000);
			
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));	
			
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			HttpClient =  new DefaultHttpClient(conMgr, params);
		}
		return HttpClient;
	}
	
	static Apachehttpclient client;
	DefaultHttpClient HttpClient;
}
