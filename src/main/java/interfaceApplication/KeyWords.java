package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.jGrapeFW_Message;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.string.StringHelper;

public class KeyWords {
	private JSONObject _obj;
	private GrapeTreeDBModel sensitiveWords;
	private String pkString;
	
	public KeyWords() {
		
		_obj = new JSONObject();
		sensitiveWords = new GrapeTreeDBModel();//数据库对象
        //数据模型
        GrapeDBDescriptionModel gdbField = new GrapeDBDescriptionModel();
        gdbField.importDescription(appsProxy.tableConfig("SensitiveWords"));
        sensitiveWords.descriptionModel(gdbField);
        
        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel(appsProxy.tableConfig("SensitiveWords"));
        sensitiveWords.permissionsModel(gperm);
  		
        sensitiveWords.checkMode();

        pkString = sensitiveWords.getPk();
	}

	/**
	 * 新增敏感词
	 * 
	 * @param info
	 * @return
	 */
	public String AddKeyWords(String info) {
		int code = 99;
		JSONObject object = JSONObject.toJSON(info);
		if (object != null) {
			try {
				String content = object.get("content").toString();
				JSONObject obj = findByContent(content); // 表中已存在该敏感词
				if (obj != null) {
					return resultMessage(2);
				}
				code = sensitiveWords.data(object).insertOnce() != null ? 0 : 99;
//				redis.set("KeyWords", sensitiveWords.field("content").select()); // 新增成功，修改缓存中的数据
			} catch (Exception e) {
				nlogger.logout(e);
				code = 99;
			}
		}
		return resultMessage(code, "新增敏感词成功");
	}

	/**
	 * 敏感词内容修改
	 * 
	 * @param id
	 *            唯一标识符
	 * @param info
	 *            修改数据
	 * @return
	 */
	public String UpdateKeyWords(String id, String info) {
		int code = 99;
		try {
			code = sensitiveWords.eq(pkString, new ObjectId(id)).data(info).updateEx() ? 0 : 99;
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return resultMessage(code, "敏感词内容修改成功");
	}

	/**
	 * 敏感词内容删除
	 * 
	 * @project GrapeSensitive
	 * @package interfaceApplication
	 * @file KeyWords.java
	 * 
	 * @param id
	 * @return
	 *
	 */
	public String DeleteKeyWords(String id) {
		int code = 99;
		try {
			code = sensitiveWords.eq(pkString, new ObjectId(id)).delete() != null ? 0 : 99;
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return resultMessage(code, "敏感词内容删除成功");
	}

	/**
	 * 敏感词内容删除
	 * 
	 * @project GrapeSensitive
	 * @package interfaceApplication
	 * @file KeyWords.java
	 * 
	 * @param id
	 * @return
	 *
	 */
	public String DeleteBatchKeyWords(String id) {
		int code = 99;
		if(StringHelper.InvaildString(id)){
			code = 99;
			return resultMessage(code, "关键词不存在");
		}
		String[] value = id.split(",");
		try {
			sensitiveWords.or();
			for (String string : value) {
				sensitiveWords.eq(pkString, new ObjectId(string));
			}
			code = sensitiveWords.deleteAll() == value.length ? 0 : 99;
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return resultMessage(code, "敏感词内容删除成功");
	}

	/**
	 * 分页显示敏感词数据
	 * 
	 * @param ids
	 *            当前页码
	 * @param pageSize
	 *            每页数据量
	 * @return String
	 *         JSONObject，包含totalsize总页数，currentPage当前页，pageSize每页数据量，data每页数据详细
	 */
	@SuppressWarnings("unchecked")
	public String page(int ids, int pageSize) {
		JSONObject object = new JSONObject();
		JSONArray array = null;
		try {
			array = new JSONArray();
			array = sensitiveWords.page(ids, pageSize);
		} catch (Exception e) {
			nlogger.logout(e);
			array = new JSONArray();
		} finally {
			object.put("totalsize", (int) Math.ceil((double) array.size() / pageSize));
			object.put("pageSize", pageSize);
			object.put("currentPage", ids);
			object.put("data", array);
		}
		return resultMessage(object);
	}

	/**
	 * 敏感词检测
	 * 
	 * @param content
	 *            待检测内容
	 * @return String
	 *         {"message":"包含敏感词"，"errorcode":3},{"message":"不含有敏感词"，"errorcode"
	 *         :0}
	 * 
	 */
	public String CheckKeyWords(String content) {
		int code = 0;
		JSONArray array = sensitiveWords.field("content").select();
		JSONObject object = null;
		String contents = "";
		if (array != null) {
			try {
				for (Object obj : array) {
					object = (JSONObject) obj;
					contents = object.get("content").toString();
					if (content.contains(contents)) {
						code = 3;
						break;
					}
				}
			} catch (Exception e) {
				nlogger.logout(e);
				code = 3;
			}
		}
		return resultMessage(code, "不含有敏感词");
	}

	/**
	 * 根据敏感词内容，判断表中是否已存在该敏感词
	 * 
	 * @param content
	 *            敏感词内容
	 * @return 表中已存在，返回该条数据信息，表中不存在，返回null
	 */
	private JSONObject findByContent(String content) {
		JSONObject obj = null;
		try {
			obj = new JSONObject();
			obj = sensitiveWords.eq("content", content).find();
		} catch (Exception e) {
			nlogger.logout(e);
			obj = null;
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	private String resultMessage(int num) {
		return resultMessage(num, "");
	}

	private String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填字段为空";
			break;
		case 2:
			msg = "该敏感词已存在";
			break;
		case 3:
			msg = "待检测内容存在敏感词";
			break;
		default:
			msg = "其他操作异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
