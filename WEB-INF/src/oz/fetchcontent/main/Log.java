package oz.fetchcontent.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


public final class Log {
	// e for echo or error
	public static void e(String errmsg, int code){
		String logf = System.getenv(config.CATALINA_HOME) + "/webapps/fetchcontent.log";
		//String logf = "/home/tomcat/tomcat/webapps/fetchcontent.log"; 
		File logfile = new File(logf);
		FileOutputStream logs;
		try {
			logs = new FileOutputStream(logfile, true);
			Date date = new Date();
			String log = date.toString()+"["+code+"]:"+errmsg+"\n\n";
			logs.write(log.getBytes());
			logs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void d(String debugmessage, String output){
		File df = new File(output);
		FileOutputStream debug;
		try{
			debug = new FileOutputStream(df, false);
			debug.write(debugmessage.getBytes());
			debug.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
