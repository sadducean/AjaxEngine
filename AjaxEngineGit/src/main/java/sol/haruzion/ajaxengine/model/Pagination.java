package sol.haruzion.ajaxengine.model;

import sol.haruzion.ajaxengine.Util;

public class Pagination {
    public boolean hasnext, hasprev;
    public long ttlrow, stidx, edidx, page, stpage, edpage, rows, pagecnt, ttlpage;
    public Object param;

    public Pagination(long ppage, long prows, long pcnt, long pttlrow) {
        this.page = ppage;
        this.rows = prows;
        this.pagecnt = pcnt;
        this.ttlrow = pttlrow;
        if (page < 1) page = 1;
        ttlpage = (long) (Math.ceil(Util.toDouble(ttlrow) / Util.toDouble(rows)));
        stidx = (page - 1) * rows;
        if (stidx < 0) stidx = 0;
        edidx = stidx + rows;
        if (edidx > ttlrow) edidx = (long) ttlrow;
        stpage = page - pagecnt / 2;
        if (stpage < 1) stpage = 1;
        edpage = page + pagecnt / 2;
        if(edpage<pcnt)edpage=pcnt;
        if (edpage > ttlpage) edpage = ttlpage;
        if (edpage < 1) edpage = 1;
        if (stpage > 1) hasprev = true;
        if (edpage < ttlpage) hasnext = true;
    }

    public Pagination(long ppage, long prows, long pcnt, long pttlrow, Object param) {
        this(ppage, prows, pcnt, pttlrow);
        this.param = param;
    }
}
