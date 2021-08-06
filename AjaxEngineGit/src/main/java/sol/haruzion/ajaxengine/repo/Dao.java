package sol.haruzion.ajaxengine.repo;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class Dao {
	@Autowired SqlSession session;
	public <T> T getMapper(Class<T> cls){
		return session.getMapper(cls);
	}
}
