/*
 * @author Xin Chen
 * Created on 2009-11-11
 * Updated on 2010-08-09
 * Email:  xchen@ir.hit.edu.cn
 * Blog:   http://hi.baidu.com/
 */
package oz.fetchcontent.analysis;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import oz.fetchcontent.main.Log;
import oz.fetchcontent.main.config;


public class TextExtract {
	
	private static List<String> lines;
	private final static int blocksWidth;
	private static int threshold;
	private static String html;
	private static boolean flag;
	private static int start;
	private static int end;
	private static StringBuilder text;
	private static ArrayList<Integer> indexDistribution;
	
	// the version with html tag.
	private static char Tagsign;
	//private static char TagEnd;
	
	private static StringBuffer htmlbuffer;
	private static StringBuffer tagbuffer;
	private static List<Htmltag> tags;
	private static int tagcounter;
	
	private final static int TAGCOUNTBASE = 0x4e00;
	//private final static String[] starttagfilter = {"<strong", "<p", "<img", "<a", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6","<br", "<br /", "<br/", "</br"};
	//private final static String[] endtagfilter   = {"</strong>", "</p>", "</a>", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>", "</h6>"};
	/*
	private final static String[] tagfilter = {"<strong", "<p", "<img", "<a", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6","<br", "<br /", "<br/", "</br",
												"</strong>", "</p>", "</a>", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>", "</h6>"};
	*/
	private final static String tagfilter = "strong p img a i h1 h2 h3 h4 h5 h6 br ul li em div /strong /p /a /i /h1 /h2 /h3 /h4 /h5 /h6 /ul /li /em /div";
	// end 
	
	static {
		lines = new ArrayList<String>();
		indexDistribution = new ArrayList<Integer>();
		tags = new ArrayList<Htmltag>();
		text = new StringBuilder();
		blocksWidth = 3;
		flag = false;
		threshold	= 86; 
		
		Tagsign = '\u2630'; // 乾挂
		//TagEnd   = '\u2637'; // 坤挂
		htmlbuffer = new StringBuffer();
		tagbuffer = new StringBuffer();
	}
	
	static class Htmltag {
		
		String tag = null;
		char mark;
		
		public Htmltag(String tag, char mark){
			this.tag = tag;
			this.mark = mark;
		}
		
		public Htmltag(char mark){
			this.mark = mark;
		}
	}
	
	static class Ahtmltag extends Htmltag {

		public Ahtmltag(String tagtext, String domain, char mark) {
			super(tagtext, mark);
		}
		
	}
	
	static class Imghtmltag extends Htmltag {
		public Imghtmltag(String tag, String domain, char mark){
			super(mark);
			if (tag.contains("src=\"")){
				if (!tag.contains("src=\"http") && !tag.contains("src=\"https")){
					this.tag = tag.replace("src=\"", "src=\""+domain);
				}
			}else if (tag.contains("src=\'")){
				if (!tag.contains("src=\'http") && !tag.contains("src=\'https")){
					this.tag = tag.replace("src=\'", "src=\'"+domain);
				}
			}else if (tag.contains("src=")){
				if (!tag.contains("src=http") && !tag.contains("src=https")){
					this.tag = tag.replace("src=", "src="+domain);
				}
			}
			this.tag = tag;
		}
	}
	
	public static void setthreshold(int value) {
		threshold = value;
	}

	
	public static String parse(String _html, String domain) 
			throws StringIndexOutOfBoundsException, IndexOutOfBoundsException{
		return parse(_html, domain, false);
	}
	
	
	public static String parse(String _html, String domain, boolean _flag) 
			throws StringIndexOutOfBoundsException, IndexOutOfBoundsException {
		flag = _flag;
		
		// TODO : debug extract
		html = _html;
		//Log.d(html,config.DEBUG_ORIHTML);
		
		// TODO : debug extract 
		replaceTag(domain);
		//Log.d(html, config.DEBUG_REPLACETAG);
		
		/*
		StringBuffer tagdbuffer = new StringBuffer();
		for (int i=0; i<tags.size(); ++i){
			tagdbuffer.append(tags.get(i).mark+"\n");
			tagdbuffer.append(tags.get(i).tag+"\n\n");
		}
		
		Log.d(tagdbuffer.toString(), config.DEBUG_TAGS);
		*/
		preProcess();
		//Log.d(html, config.DEBUG_REMOVETAG);
		
		html = getText();
		//Log.d(html, config.DEBUG_EXTRACT);
		
		html = restoreTag();
		//Log.d(html, config.DEBUG_RESTORETAG);
		
		return html;
	}
	
	private static void replaceTag (String domain) throws StringIndexOutOfBoundsException{
		// TODO : find out some tag you need
		
		// init all the data there.
		htmlbuffer.delete(0, htmlbuffer.length());
		tags.clear();
		tagcounter = TAGCOUNTBASE;
		// init end 
		
		Integer[] pos = new Integer[1];
		pos[0] = -1;
		final int htmlsize = html.length();
		
		char c;
		while(pos[0] < htmlsize-1){
			c = html.charAt(++pos[0]);
			
			if (c == '<'){
				
				tagbuffer.delete(0, tagbuffer.length());
				tagbuffer.append(c);
				
				boolean ret = punmpTag(tagbuffer, html, htmlsize, pos);
				
				if(ret){
					htmlbuffer.append(storeTag(tagbuffer, domain));
				}else{
					htmlbuffer.append(tagbuffer);
				}
			}else
				htmlbuffer.append(c);
		}
		html = htmlbuffer.toString();
	}
	
	private static String restoreTag() throws IndexOutOfBoundsException{
		htmlbuffer.delete(0, htmlbuffer.length());
		
		Integer[] pos = new Integer[1];
		pos[0] = -1;
		final int htmlsize = html.length();
		char c;
		while(pos[0] < htmlsize-1){
			c = html.charAt(++pos[0]);
			if (c == Tagsign){
				char tagmark = html.charAt(++pos[0]);
				
				Htmltag tag = findTag(tagmark);
				if (tag != null){
					htmlbuffer.append(tag.tag);
					continue;
				}
				
				htmlbuffer.append(c);
				htmlbuffer.append(tagmark);
				
			}else
				htmlbuffer.append(c);
		}
		return htmlbuffer.toString();
	}
	
	private static String storeTag(StringBuffer tagbuffer, String domain) {
		
		String tagtext = tagbuffer.toString();
		
		tagbuffer.deleteCharAt(0);
		tagbuffer.deleteCharAt(tagbuffer.length()-1);
		String[] tagcontent = tagbuffer.toString().split(" ");
		
		if (tagcontent!= null && tagcontent.length >0 && retainedTag(tagcontent[0])){
			int tagmark = createTag(tagcontent[0], tagtext, domain);
			return "" + Tagsign + (char)tagmark;
		}else
			return tagtext;
	}
	
	private static boolean retainedTag(String tag){
		return tagfilter.contains(tag);
	}
	
	private static int createTag(String whattag, String tagtext, String domain){
		
		if (whattag.equals("img")){
			
			Htmltag newtag = new Imghtmltag(tagtext, null, (char)(tagcounter++));
			tags.add(newtag);
			return newtag.mark;
			
		}else if (whattag.equals("a")){
			
			Htmltag newtag = new Ahtmltag(tagtext, domain, (char)(tagcounter++));
			tags.add(newtag);
			return newtag.mark;
			//return tags.size();
			
		}else if (whattag.contains("br")){
			Htmltag newtag = new Htmltag("<br />", (char)(tagcounter++));
			tags.add(newtag);
			return newtag.mark;
		}else{
			Htmltag newtag = new Htmltag(tagtext, (char)(tagcounter++));
			tags.add(newtag);
			return newtag.mark;
		}
	}
	
	private static Htmltag findTag(char mark){
		for (int i=0; i<tags.size(); ++i){
			if (tags.get(i).mark == mark)
				return tags.get(i);
		}
		return null;
	}
	
	private static boolean punmpTag(StringBuffer tag, String html, int htmlsize, Integer[] pos){
		
		while(pos[0] < htmlsize-1){
			char c = html.charAt(++pos[0]);
			if (c == '<'){
				--pos[0];
				return false;
			}else if (c == '>'){
				tag.append(c);
				return true;
			}else
				tag.append(c);
		}
		return false;
	}
	
	private static void preProcess() {
		html = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
		html = html.replaceAll("(?is)<!--.*?-->", "");				// remove html comment
		html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		html = html.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
		html = html.replaceAll("&.{2,5};|&#.{2,5};", "");			// remove special char
		html = html.replaceAll("(?is)<.*?>", "");
		//<!--[if !IE]>|xGv00|9900d21eb16fa4350a3001b3974a9415<![endif]--> 
	}
	
	private static String getText() {
		lines = Arrays.asList(html.split("\n"));
		indexDistribution.clear();
		
		for (int i = 0; i < lines.size() - blocksWidth; i++) {
			int wordsNum = 0;
			for (int j = i; j < i + blocksWidth; j++) { 
				lines.set(j, lines.get(j).replaceAll("\\s+", ""));
				wordsNum += lines.get(j).length();
			}
			indexDistribution.add(wordsNum);
		}
		
		start = -1; end = -1;
		boolean boolstart = false, boolend = false;
		text.setLength(0);
		
		for (int i = 0; i < indexDistribution.size() - 1; i++) {
			if (indexDistribution.get(i) > threshold && ! boolstart) {
				if (indexDistribution.get(i+1).intValue() != 0 
					|| indexDistribution.get(i+2).intValue() != 0
					|| indexDistribution.get(i+3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}
			if (boolstart) {
				if (indexDistribution.get(i).intValue() == 0 
					|| indexDistribution.get(i+1).intValue() == 0) {
					end = i;
					boolend = true;
				}
			}
			StringBuilder tmp = new StringBuilder();
			if (boolend) {
				//System.out.println(start+1 + "\t\t" + end+1);
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < 5) continue;
					tmp.append(lines.get(ii) + "\n");
				}
				String str = tmp.toString();
				//System.out.println(str);
				if (str.contains("Copyright")  || str.contains("版权") ) continue; 
				text.append(str);
				boolstart = boolend = false;
			}
		}
		return text.toString();
	}
}

