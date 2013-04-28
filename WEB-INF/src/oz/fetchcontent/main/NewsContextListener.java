package oz.fetchcontent.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import oz.fetchcontent.analysis.Newslist;
import oz.fetchcontent.datax.Datax;

public class NewsContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		Datax.getInstance().destoryConnector();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		new Newslist();
		//System.out.println("start the Newslist!!");
	}

}
