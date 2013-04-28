package oz.fetchcontent.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.horrabin.horrorss.RssFeed;
import org.horrabin.horrorss.RssItemBean;
import org.horrabin.horrorss.RssParser;

import oz.fetchcontent.datax.Datax;
import oz.fetchcontent.datax.Sqlmaker;
import oz.fetchcontent.main.Log;
import oz.fetchcontent.main.Util;
import oz.fetchcontent.main.rs;

public class Newslist extends Thread{
	
	public Newslist(){
		
		mRss = new RssParser();
		mNewscache = new HashMap<String, Newsitem>();
		unprocessLinks = new ArrayList<RssItemBean>();
		addListener(new Newscacheupdate());
		this.start();
	}

	@Override
	public void run() {
		while(true){
			// 1 get the rss data and init some contianer;
			
			Log.e("Get up, keep working", 0);
			try {
				mFeed = mRss.load(NEWSURL);
				//InputStream ins = new InputStream();
				//mRss.load(input);
				List<RssItemBean> items = mFeed.getItems();
				unprocessLinks.clear();
				
				mRound++;
				
				for (int i=0; i<items.size(); ++i){
					RssItemBean bean = items.get(i);
					try {
						isprocessLink(bean, unprocessLinks);
					} catch (NoSuchAlgorithmException e) {
						Log.e(e.getMessage(), rs.EXCEPTIONCODE);
					}
				}
				
				// 2 process those unprocess link;
				processLink(unprocessLinks);
				
				Log.e("Jobs done time to sleep a while", 0);
				
				try {
					Newslist.sleep(TAKEABREAK);
				} catch (InterruptedException e) {
					Log.e("Newslist Thread die", rs.EXCEPTIONCODE);
				}
			} catch (Exception e1) {
				Log.e(e1.getMessage(), rs.EXCEPTIONCODE);
				try {
					Newslist.sleep(TAKEABREAK);
				} catch (InterruptedException e) {
					Log.e(e.getMessage(), rs.EXCEPTIONCODE);
				}
				mRound--;
			}
		}
	}

	public void isprocessLink(RssItemBean bean, List<RssItemBean> unprocesslink) 
			throws NoSuchAlgorithmException{
		
		String url = bean.getLink();
		String hashurl = Util.md5(url);
		
		Newsitem newsitem = mNewscache.get(hashurl);
		
		if (newsitem == null){
			unprocesslink.add(bean);
		}else{
			newsitem.hint();
		}
	}
	
	public int processLink(List<RssItemBean> unprocessLinks) {
		
		// 1 check this link from db;
		// 2 if it is exist; create the news item into the newscache();
		// else fresh new one. deal with fetch the content.
		Webcontent content = null;
		for (int i=0; i<unprocessLinks.size(); i++){
			
			RssItemBean bean = unprocessLinks.get(i);
			
			String url = bean.getLink();
			
			String hashurl = null;
			
			try {
				hashurl = Util.md5(url);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
				hashurl = "";
			}
			
			// 1 search it from db;
			String sql = Sqlmaker.queryNewsBylinkhash(hashurl);
			int count = 0;
			
			try {
				count = Datax.getInstance().checkRecExist(sql, true);
				if(count > 0){
					// it exist in the db;
					Newsitem item = new Newsitem(url);
					mNewscache.put(hashurl, item);
					
				}else{
					// it flash new
					if (content == null)
						content = new Webcontent(bean);
					else
						content.resetRssItembean(bean);
					
					content.processBean();
					content.saveContenttoDB();
				    String linkhash = content.getLinkhash();
				    mNewscache.put(linkhash, new Newsitem(bean.getLink()));
				}
			} catch (SQLException e) {
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			} catch (IOException e) {
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			} catch (NoSuchAlgorithmException e) {
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			} catch (URISyntaxException e) {
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			} catch (StringIndexOutOfBoundsException e){
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			}catch (IndexOutOfBoundsException e){
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			}catch (IllegalStateException e){
				//e.printStackTrace();
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
			}
		}
		
		onUpdatecache();
		return 0;
	}
	
	public void addListener(NewscacheListener listener){
		mCachelistener[0] = listener;
	}
	
	public void onUpdatecache(){

		mCachelistener[0].updateCache();
	}
	
	// data define
	interface NewscacheListener{
		void updateCache();
	}
	
	class Newscacheupdate implements NewscacheListener {

		@Override
		public void updateCache() {
			if (mNewscache.size() > CACHEITMESIZE){
				// clean some item with low hint;
				Log.e("start clear up cache, count is "+mNewscache.size(), 0);
				
				float unhitpoint = -1 * mRound * rs.UNHITPOINT;
				float totlehint = 0.0f;
				
				Iterator it = mNewscache.entrySet().iterator();
				List<String> removeitem = new ArrayList<String>();
				while (it.hasNext()){
					
					Map.Entry<String, Newsitem> entry = (Entry<String, Newsitem>) it.next();
					Newsitem item = entry.getValue();
					item.addHint(unhitpoint);
					totlehint += item.getHint();
				}
				
				float averagehit = totlehint / mNewscache.size();
				
				it = mNewscache.entrySet().iterator();
				while (it.hasNext()){
					Map.Entry<String, Newsitem> entry = (Entry<String, Newsitem>) it.next();
					Newsitem item = entry.getValue();
					String   key  = entry.getKey();
					if (item.getHint() <= averagehit){
						removeitem.add(key);
					}
				}
				// remove those the unhot news
				for (int i=0; i<removeitem.size(); ++i){
					mNewscache.remove(removeitem.get(i));
				}
				
				// rount reset
				mRound = 0;
				Log.e("finish clear up cache count is"+mNewscache.size(), 0);
			}
		}
		
	}
	
	RssParser mRss = null;
	RssFeed mFeed = null;
	Newsitem mItem = null;
	
	Map<String, Newsitem> mNewscache = null;
	List<RssItemBean> unprocessLinks = null;
	
	Integer mRound = 0;
	NewscacheListener[] mCachelistener = new NewscacheListener[LISTENERSIZE];
	
	final static String NEWSURL = "http://news.dbanotes.net/rss";
	final static int CACHEITMESIZE  = 1000;
	final static int TAKEABREAK = 1000 * 60 * 10;
	final static int LISTENERSIZE = 3;
}
