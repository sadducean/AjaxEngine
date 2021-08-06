package sol.haruzion.ajaxengine.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.record.pivottable.PageItemRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import sol.haruzion.ajaxengine.Util;
import sol.haruzion.ajaxengine.mapper.CommonMapper;
import sol.haruzion.ajaxengine.model.Col;
import sol.haruzion.ajaxengine.model.Pagination;
import sol.haruzion.ajaxengine.model.Rslt;
import sol.haruzion.ajaxengine.model.Tbl;
import sol.haruzion.ajaxengine.repo.Dao;

@RestController
@RequestMapping("ajax")
public class RestAjaxController {
	@Autowired PlatformTransactionManager transactionManager;
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;
	@Autowired Dao dao;
	public CommonMapper getCm() {
		return dao.getMapper(CommonMapper.class);
	}
	@RequestMapping("mappings")
	public List<String> mappings() {
		List<String> result = new ArrayList<String>();
		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		for (Entry<RequestMappingInfo, HandlerMethod> elem : map.entrySet()) {
			RequestMappingInfo key = elem.getKey();
			HandlerMethod method = elem.getValue();
			result.add(key.toString());
		}
		return result;
	}
	@RequestMapping("test")
	public Rslt test(@RequestBody Map<String,Object> param) {
		return Rslt.data(param);
	}
	@RequestMapping("ctxpath")
	public String ctxpath(HttpServletRequest req) {
		return req.getServletContext().getContextPath();
	}
	@RequestMapping("existdb")
	public Rslt existdb(@RequestBody Map<String,Object> param) {
		return Rslt.data(Util.toMap("exist",getCm().existsdb(param)));
	}
	@RequestMapping("createdb")
	public Rslt createdb(@RequestBody Map<String,Object> param) {
		try {
			return Rslt.rslt(getCm().createdb(param));
		} catch (Exception e) {
			return Rslt.fail(e);
		}
	}
	@RequestMapping("existstbl")
	public Rslt existstbl(@RequestBody Map<String,Object> param) {
		try {
			return Rslt.data(Util.toMap("exist",getCm().existstbl(param)));
		} catch (Exception e) {
			return Rslt.fail(e); 
		}
	}
	@RequestMapping("now")
	public Rslt now() {
		return Rslt.data(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()));
	}
	@RequestMapping("createtbl")
	public Rslt createtbl(@RequestBody Tbl tbl) {
		Rslt rslt=new Rslt();
		try {
			String sql="create table "+tbl.db+"."+tbl.name+"(`seq` BIGINT(20) PRIMARY key NOT NULL AUTO_INCREMENT,";
			for (int i = 0; i < tbl.cols.size(); i++) {
				Col col=tbl.cols.get(i);
				col.name=col.name.trim();
				col.type=col.type.trim();
				sql+=col.name+" "+col.type;
				if(!col.type.equalsIgnoreCase("datetime")
						&&!col.type.equalsIgnoreCase("bit")
						&&!col.type.equalsIgnoreCase("int")
						&&!col.type.equalsIgnoreCase("bigint")
						&&!col.type.equalsIgnoreCase("double")
						&&!col.type.toLowerCase().contains("text")) {
					sql+="("+col.len+")";
				}
				sql+=" null";
				if(col.comment!=null)sql+=" COMMENT '"+col.comment+"'";
				if(i<tbl.cols.size()-1) {
					sql+=",";
				}
			}
			sql+=")";
			if(tbl.comment!=null)sql+=" COMMENT '"+tbl.comment+"'";
			Map<String,Object> dr=Util.toMap("sql",sql);
			getCm().update(dr);
			rslt.success=true;
		} catch (Exception e) {
			e.printStackTrace();
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	@RequestMapping("droptbl")
	public Rslt droptbl(@RequestBody Tbl tbl) {
		Rslt rslt=new Rslt();
		try {
			String sql="drop table "+tbl.db+"."+tbl.name;
			Map<String,Object> dr=Util.toMap("sql",sql);
			getCm().update(dr);
			rslt.success=true;
		} catch (Exception e) {
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	
	@RequestMapping("addcol")
	public Rslt addcol(@RequestBody Tbl tbl) {
		Rslt rslt=new Rslt();
		TransactionStatus txStatus =
				transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			String sql="";
			for (int i = 0; i < tbl.cols.size(); i++) {
				Col col=tbl.cols.get(i);
				col.name=col.name.trim();
				col.type=col.type.trim();
				sql="ALTER TABLE "+tbl.db+"."+tbl.name+" ADD COLUMN "+col.name+" "+col.type;
				if(!col.type.equalsIgnoreCase("datetime")
						&&!col.type.equalsIgnoreCase("bit")
						&&!col.type.equalsIgnoreCase("int")
						&&!col.type.equalsIgnoreCase("bigint")
						&&!col.type.equalsIgnoreCase("double")
						&&!col.type.toLowerCase().contains("text")) {
					sql+="("+col.len+")";
				}
				sql+=" null";
				if(col.comment!=null)sql+=" COMMENT '"+col.comment+"'";
				Map<String,Object> dr=Util.toMap("sql",sql);
				getCm().update(dr);
			}
			rslt.success=true;
		} catch (Exception e) {
			e.printStackTrace();
			 transactionManager.rollback(txStatus);
			rslt.success=false;
			rslt.message=e+"";
		}
		transactionManager.commit(txStatus);
		return rslt;
	}
	@RequestMapping("search")
	public Rslt search(@RequestBody Tbl tbl) {
		List<Map<String, String>> cols=getCm().columns(Util.toMap("db",tbl.db,"tbl",tbl.name));
		ArrayList<String> dates=new ArrayList<String>();
		for (Map<String, String> map : cols) {
			if(map.get("COLUMN_TYPE").toLowerCase().contains("datetime")) {
				dates.add(map.get("COLUMN_NAME"));
			}
		}
		String colnames="*";
		if(tbl.cols!=null&&!tbl.cols.isEmpty()) {
			colnames=Strings.join(tbl.cols.stream().map(a->{
				if(dates.contains(a.name)) {
					return "date_format("+a.name+", '%Y-%m-%d %H:%i:%s') "+a.name;
				}
				return a.name;
			}).collect(Collectors.toList()),',');
		}
		if(colnames.equals("*")) {
			colnames=Strings.join(cols.stream().map(a->{
				String colname=a.get("COLUMN_NAME");
				if(a.get("COLUMN_TYPE").toLowerCase().contains("datetime")) {
					return "date_format("+colname+", '%Y-%m-%d %H:%i:%s') "+colname;
				}
				return colname;
			}).collect(Collectors.toList()),',');
		}
		String sql="select "+colnames+" from "+tbl.db+"."+tbl.name;
		if(!StringUtils.isEmpty(tbl.where))sql+=" where "+tbl.where;
		if(!StringUtils.isEmpty(tbl.orderby))sql+=" order by "+tbl.orderby;
		if(!StringUtils.isEmpty(tbl.limit))sql+=" limit "+tbl.limit;
		Rslt rslt=new Rslt();
		try {
			Map<String,Object> dr=Util.toMap("sql",sql);
			tbl.values=getCm().select(dr);
			tbl.values=Util.filterList(tbl.values);
			rslt.data=tbl;
		} catch (Exception e) {
			e.printStackTrace();
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	@RequestMapping("searchpage")
	public Rslt searchpage(@RequestBody Tbl tbl) {
		List<Map<String, String>> cols=getCm().columns(Util.toMap("db",tbl.db,"tbl",tbl.name));
		ArrayList<String> dates=new ArrayList<String>();
		for (Map<String, String> map : cols) {
			if(map.get("COLUMN_TYPE").toLowerCase().contains("datetime")) {
				dates.add(map.get("COLUMN_NAME"));
			}
		}
		String colnames="*";
		if(tbl.cols!=null&&!tbl.cols.isEmpty()) {
			colnames=Strings.join(tbl.cols.stream().map(a->{
				if(dates.contains(a.name)) {
					return "date_format("+a.name+", '%Y-%m-%d %H:%i:%s') "+a.name;
				}
				return a.name;
			}).collect(Collectors.toList()),',');
		}
		if(colnames.equals("*")) {
			colnames=Strings.join(cols.stream().map(a->{
				String colname=a.get("COLUMN_NAME");
				if(a.get("COLUMN_TYPE").toLowerCase().contains("datetime")) {
					return "date_format("+colname+", '%Y-%m-%d %H:%i:%s') "+colname;
				}
				return colname;
			}).collect(Collectors.toList()),',');
		}
		String sql="select "+colnames+" from "+tbl.db+"."+tbl.name;
		if(!StringUtils.isEmpty(tbl.where))sql+=" where "+tbl.where;
		if(!StringUtils.isEmpty(tbl.orderby))sql+=" order by "+tbl.orderby;
		if(tbl.page==null)tbl.page=1;
		if(tbl.rowsperpage==null)tbl.rowsperpage=10;
		if(tbl.pagercount==null)tbl.pagercount=10;
		long ttl=count(tbl).count;
		tbl.pagination = new Pagination(tbl.page, tbl.rowsperpage, tbl.pagercount, ttl);
		sql+=" limit "+tbl.pagination.stidx+" , "+tbl.pagination.rows;
		Rslt rslt=new Rslt();
		try {
			Map<String,Object> dr=Util.toMap("sql",sql);
			tbl.values=getCm().select(dr);
			tbl.values=Util.filterList(tbl.values);
			rslt.data=tbl;
		} catch (Exception e) {
			e.printStackTrace();
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	@RequestMapping("count")
	public Rslt count(@RequestBody Tbl tbl) {
		String sql="select count(*) from "+tbl.db+"."+tbl.name;
		if(!StringUtils.isEmpty(tbl.where))sql+=" where "+tbl.where;
		Rslt rslt=new Rslt();
		try {
			Map<String,Object> dr=Util.toMap("sql",sql);
			rslt.count=getCm().count(dr);
			rslt.data=tbl;
		} catch (Exception e) {
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	@RequestMapping("del")
	public Rslt del(@RequestBody Tbl tbl) {
		if(Strings.isBlank(tbl.where)) {
			return Rslt.fail("where 필수");
		}
		Rslt rslt=new Rslt();
		String sql="delete from "+tbl.db+"."+tbl.name+" where "+tbl.where;
		try {
			Map<String,Object> dr=Util.toMap("sql",sql);
			rslt.count=getCm().delete(dr);
			rslt.data=tbl;
		} catch (Exception e) {
			rslt.success=false;
			rslt.message=e+"";
		}
		return rslt;
	}
	@RequestMapping("insert")
	public Rslt insert(@RequestBody Tbl tbl) {
		TransactionStatus txStatus =
				transactionManager.getTransaction(new DefaultTransactionDefinition());
		Rslt rslt=new Rslt();
		List<String> colnamelist=tbl.cols.stream().map(a->a.name).collect(Collectors.toList());
		String colnames=Strings.join(colnamelist,',');
		String psql="insert into "+tbl.db+"."+tbl.name+" ("+colnames+")values(";
		try {
			for (Map<String, Object> tval : tbl.values) {
				ArrayList<String> vals=new ArrayList<String>();
				for (String colname : colnamelist) {
					Object dval=tval.get(colname);
					if(dval!=null) {
						if(dval instanceof Boolean) {
							vals.add(dval.toString());
						}else {
							vals.add("'"+dval+"'");
						}
					}
					else vals.add("null");
				}
				String sql=psql+Strings.join(vals, ',')+")";
				Map<String,Object> dr=Util.toMap("sql",sql);
				dr.put("seq",null);
				long inserted=getCm().insert(dr);
				rslt.count+=inserted;
				if(inserted>0) {
					tval.put("seq",dr.get("seq"));
				}
			}
			transactionManager.commit(txStatus);
		} catch (Exception e) {
			transactionManager.rollback(txStatus);
			rslt.success=false;
			rslt.message=e+"";
		}
		rslt.data=tbl;
		return rslt;
	}
	@RequestMapping("update")
	public Rslt update(@RequestBody Tbl tbl) {
		TransactionStatus txStatus =
				transactionManager.getTransaction(new DefaultTransactionDefinition());
		Rslt rslt=new Rslt();
		try {
			for (Map<String, Object> tval : tbl.values) {
				Iterator<Entry<String, Object>> ents =tval.entrySet().iterator();
				ArrayList<String> upentities=new ArrayList<String>();
				while(ents.hasNext()) {
					Entry<String, Object> ent=ents.next();
					if(!ent.getKey().equals("seq")) {
						if(ent.getValue()!=null) {
							if(ent.getValue() instanceof Boolean) {
								upentities.add(ent.getKey()+"="+ent.getValue());
							}else {
								upentities.add(ent.getKey()+"='"+ent.getValue()+"'");
							}
						}else {
							upentities.add(ent.getKey()+"=null");
						}
						
					}
				}
				String sql="update "+tbl.db+"."+tbl.name+" set "+Strings.join(upentities, ',')+" where seq="+tval.get("seq");
				Map<String,Object> dr=Util.toMap("sql",sql);
				boolean updated=getCm().update(dr);
				if(updated) {
					rslt.count++;
				}
				tval.put("updated", updated);
			}
			transactionManager.commit(txStatus);
		} catch (Exception e) {
			transactionManager.rollback(txStatus);
			rslt.success=false;
			rslt.message=e+"";
		}
		rslt.data=tbl;
		return rslt;
	}
	@RequestMapping("rawsql")
	public Object rawsql(@RequestBody Map<String, Object> param) {
		if(param.get("act").equals("select")) {
			return getCm().select(param);
		}else {
			return getCm().update(param);
		}
	}
	@RequestMapping("encrypt")
	public Rslt encrypt(@RequestBody List<String> param) {
		try {
			ArrayList<String> encs=new ArrayList<String>();
			for (String str : param) {
				encs.add(Util.encrypt(str));
			}
			return Rslt.data(encs);
		} catch (Exception e) {
			return Rslt.fail(e);
		}
	}
	@RequestMapping("decrypt")
	public Rslt decrypt(@RequestBody List<String> param) {
		try {
			ArrayList<String> encs=new ArrayList<String>();
			for (String str : param) {
				encs.add(Util.decrypt(str));
			}
			return Rslt.data(encs);
		} catch (Exception e) {
			return Rslt.fail(e);
		}
	}
	@RequestMapping("session")
	public Rslt manageSession(@RequestBody Map<String, String> param,HttpSession session) {
		String act=param.get("act");
		String key=param.get("key");
		String val=param.get("val");
		try {
			if(act.equals("set")) {
				session.setAttribute(key, val);
			}else if(act.equals("get")) {
				return Rslt.data(Util.nvl(session.getAttribute(key)));
			}else if(act.equals("del")) {
				session.removeAttribute(key);
			}else if(act.equals("destroy")) {
				session.invalidate();
			}
			return Rslt.succ();
		} catch (Exception e) {
			return Rslt.fail(e+"");
		}
	}
	@RequestMapping("file")
	public Rslt manageFile(MultipartHttpServletRequest mreq) {
		final Rslt rslt=Rslt.data(new ArrayList<String>());
		try {
			String upath=mreq.getServletContext().getRealPath("/upload/");
			String act=mreq.getParameter("act");
			if(act.equals("upload")) {
				Map<String, MultipartFile> files=mreq.getFileMap();
				files.forEach((name,file)->{
					String fn=file.getOriginalFilename();
					if(fn==null)fn=name;
					String nm=Util.renameDuplicates(upath,fn);
					try {
						File dest=new File(upath,nm);
						file.transferTo(dest);
						((List<String>)rslt.data).add("upload/"+dest.getName());
						rslt.count++;
					} catch (Exception e) {
						rslt.success=false;
						rslt.message+=e+"\n";
					}
				});
			}else if(act.equals("delete")) {
				String[] dpaths=mreq.getParameter("paths").split(",");
				for (String dpath : dpaths) {
					boolean drslt=new File(dpath).delete();
					if(drslt)rslt.count++;
					if(!drslt)rslt.success=false;
				}
			}
		} catch (Exception e2) {
			return Rslt.fail(e2);
		}
		return rslt;
	}
	@RequestMapping("deletefile")
	public Rslt deleteFile(@RequestBody Map<String,Object> param,HttpServletRequest req) {
		try {
			Rslt rslt=new Rslt();
			List<String> filenames=(List<String>) param.get("filenames");
			for (String fn : filenames) {
				if(new File(req.getServletContext().getRealPath("upload"),fn).delete()) {
					rslt.count++;
				}
			}
			return rslt;
		} catch (Exception e) {
			e.printStackTrace();
			return Rslt.fail(e);
		}
	}
	@RequestMapping(value="tableinfo",produces = "text/html;charset=UTF-8")
	public String tableInfo(String db,String tablename) {
		String sql="SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='"+db+"' AND TABLE_NAME='"+tablename+"'";
		String tablecomment=getCm().select(Util.toMap("sql",sql)).get(0).get("TABLE_COMMENT")+"";
		if(tablecomment.equals("null"))tablecomment="";
		sql="SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='"+db+"' AND TABLE_NAME='"+tablename+"'";
		List<Map<String, Object>> colinfos=getCm().select(Util.toMap("sql",sql));
		colinfos=Util.filterList(colinfos);
		String rslt="<!DOCTYPE html>" + 
				"<html>" + 
				"<head>" + 
				"<meta charset=\"utf-8\"></head><body><table border=1 style='border-collapse: collapse;'>";
		rslt+="<tr style='background-color:orange;'>";
		rslt+="<td>db name</td><td>table name</td><td colspan='2'>table comment</td>";
		rslt+="</tr><tr>";
		rslt+="<td>"+db+"</td><td>"+tablename+"</td><td colspan='2'>"+tablecomment+"</td>";
		rslt+="</tr><tr><td colspan='4'></td></tr>";
		rslt+="<tr style='background-color:orange;'>";
		rslt+="<td>name</td><td>len</td><td>type</td><td>comment</td>";
		rslt+="</tr>";
		for (int i = 0; i < colinfos.size(); i++) {
			rslt+="<tr>";
			Map<String, Object> map=colinfos.get(i);
			rslt+="<td>"+map.get("COLUMN_NAME")+"</td>";
			String type=map.get("COLUMN_TYPE").toString().trim().replace(")","");
			String[] temp=type.split("\\(");
			String dty=temp[0];
			String len="";
			try {
				len=temp[1];
			} catch (Exception e) {}
			rslt+="<td>"+len+"</td>";
			rslt+="<td>"+dty+"</td>";
			rslt+="<td>"+map.get("COLUMN_COMMENT")+"</td>";
			rslt+="</tr>";
		}
		rslt+="</body></table>";
		return rslt;
	}
	@RequestMapping("ip")
	public String getIp(HttpServletRequest req) {
		return req.getRemoteAddr();
	}
	@RequestMapping("join")
	public Object join(String db,String cols,String tbls,String where,String orderby,String rowsperpage,String pagenumber) {
		List<Map<String,String>> tables=new Gson().fromJson(tbls,new TypeToken<List<Map<String,String>>>(){}.getType());
		String sql="select "+cols+" from "+db+"."+tables.get(0).get("name")+" ";
		for (int i = 1; i < tables.size(); i++) {
			Map<String,String> tbl=tables.get(i);
			String jointype="";
			if(!StringUtils.isEmpty(tbl.get("jointype"))) {
				jointype=tbl.get("jointype");
			}
			sql+=" left "+jointype+" join "+db+"."+tables.get(i).get("name")+" on "
					+tbl.get("on");
		}
		if(!StringUtils.isEmpty(where)) {
			sql+=" where "+where;
		}
		if(!StringUtils.isEmpty(orderby)) {
			sql+=" order by "+orderby;
		}
		HashMap<String,Object> sp=new HashMap<String, Object>();
		sp.put("sql",sql);
		List<Map<String,Object>> rslt = getCm().select(sp);
		return rslt;
	}
	@RequestMapping("wait")
	public String wait(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "{waited:"+millis+"}";
	}
}
