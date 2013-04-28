package oz.fetchcontent.datax;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oz.fetchcontent.main.rs;

public class Newsdx implements Datafetcher {

	@Override
	public int execute(ResultSet rest, List<List<kv> > kvz) throws SQLException {
		do {
			
			List<kv> kvx = new ArrayList<kv>();
			
			kv e =  new kv(rs.ID, rest.getInt(rs.ID));
			kvx.add(e);
			
			e = new kv(rs.ORILINK, rest.getString(rs.ORILINK));
			kvx.add(e);
			
			e = new kv(rs.LINKHASH, rest.getString(rs.LINKHASH));
			kvx.add(e);
			
			e = new kv(rs.CONTENT, rest.getString(rs.CONTENT));
			kvx.add(e);
			
			e = new kv(rs.TITLE, rest.getString(rs.TITLE));
			kvx.add(e);
			
			e = new kv(rs.AUTHOR, rest.getString(rs.AUTHOR));
			kvx.add(e);
			
			e = new kv(rs.PROCESSURL, rest.getString(rs.PROCESSURL));
			kvx.add(e);
			
			e = new kv(rs.UPDATED, rest.getString(rs.UPDATED));
			kvx.add(e);
			
			e = new kv(rs.STATUS, rest.getInt(rs.UPDATED));
			kvx.add(e);
			
			e = new kv(rs.ANALYSIS, rest.getString(rs.ANALYSIS));
			kvx.add(e);
			
			e = new kv(rs.ORIWEBSITE, rest.getString(rs.ORIWEBSITE));
			kvx.add(e);
			kvz.add(kvx);
			
		}while(rest.next());
		return 0;
	}
}

