package oz.fetchcontent.analysis;

import oz.fetchcontent.main.rs;

public class Newsitem {
	String link;
	float hint = 1.0f;
	
	public Newsitem(String link){
		this.link = link;
	}
	
	public void hint(){
		hint = hint + rs.HINTPOINT;
	}
	
	public void addHint(float hintpoint){
		hint += hintpoint;
	}
	
	public float getHint(){
		return hint;
	}
	
}
