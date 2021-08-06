package sol.haruzion.ajaxengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tbl {
	public String db,name,comment;
	public List<Col> cols=new ArrayList<Col>();
	public List<Map<String, Object>> values=new ArrayList<Map<String, Object>>();
	public String where;
	public String orderby;
	public String limit;
	public Integer page,rowsperpage,pagercount;
	public Pagination pagination;
}
