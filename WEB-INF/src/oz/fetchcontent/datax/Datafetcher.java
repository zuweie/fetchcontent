package oz.fetchcontent.datax;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Datafetcher {
	public int execute(ResultSet rest, List<List<kv> > kvz) throws SQLException;
}
