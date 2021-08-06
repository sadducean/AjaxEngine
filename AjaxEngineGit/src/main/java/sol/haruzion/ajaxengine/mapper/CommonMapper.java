package sol.haruzion.ajaxengine.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface CommonMapper {
	boolean existsdb(@Param("param") Map<String,Object> param);
	boolean existstbl(@Param("param") Map<String,Object> param);
	boolean createdb(@Param("param") Map<String,Object> param);
	boolean update(@Param("param") Map<String,Object> param);
	Long delete(@Param("param") Map<String,Object> param);
	long insert(@Param("param") Map<String,Object> param);
	List<Map<String, Object>> select(@Param("param") Map<String,Object> param);
	List<Map<String, String>> columns(@Param("param") Map<String,Object> param);
	long count(@Param("param") Map<String,Object> param);
}
