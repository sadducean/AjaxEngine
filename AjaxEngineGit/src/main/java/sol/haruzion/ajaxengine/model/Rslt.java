package sol.haruzion.ajaxengine.model;

public class Rslt {
	public String message="";
	public boolean success=true;
	public long count;
	public Object data;
	public static Rslt succ() {
		return new Rslt();
	}
	public static Rslt rslt(boolean succ) {
		Rslt rslt=new Rslt();
		rslt.success=succ;
		return rslt;
	}
	public static Rslt data(Object data) {
		Rslt rslt=new Rslt();
		rslt.data=data;
		return rslt;
	}
	public static Rslt data(String msg,Object data) {
		Rslt rslt=new Rslt();
		rslt.data=data;
		rslt.message=msg;
		return rslt;
	}
	public static Rslt succ(String msg) {
		Rslt rslt=new Rslt();
		rslt.message=msg;
		return rslt;
	}
	public static Rslt fail(Object msg) {
		Rslt rslt=new Rslt();
		rslt.message=msg+"";
		rslt.success=false;
		return rslt;
	}
}
