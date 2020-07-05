/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.webserver;

import com.alibaba.fastjson.JSONArray;
import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgBodyProto;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgBodyProto.CabinetMsgBody;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.directwebremoting.Browser;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ScriptSessionFilter;

/**
 * @program: cabinet
 *
 * @description: 工具类
 *
 * @author: xrds
 *
 * @create: 2020-07-05 13:18
 **/
public class DwrUtil {
  /**
   * 发送给全部
   */
  public static void sedMessageToAll(CabinetMsgProto.CabinetMsg msgModel){


    try{
      CabinetMsgBodyProto.CabinetMsgBody  content = CabinetMsgBodyProto.CabinetMsgBody.parseFrom(msgModel.getContent());
      Map<String,Object> map = new HashMap<String,Object>();
      map.put("user", Constants.DWRConfig.JSONFORMAT.printToString(msgModel));
      map.put("content", Constants.DWRConfig.JSONFORMAT.printToString(content));
      final Object msg = JSONArray.toJSON(map);
      Browser.withAllSessions(new Runnable(){
        private ScriptBuffer script = new ScriptBuffer();
        public void run(){
          script.appendCall(Constants.DWRConfig.DWR_SCRIPT_FUNCTIONNAME, msg);
          Collection<ScriptSession> sessions = Browser.getTargetSessions();
          for (ScriptSession scriptSession : sessions){
            scriptSession.addScript(script);
          }
        }
      });
    }catch(Exception e){

    }
  }
  /**
   * 发送给个人
   */
  public static void sendMessageToUser(String userid, String message) {
    final String sessionid = userid;
    final String msg = message;
    final String attributeName = Constants.SessionConfig.SESSION_KEY;
    Browser.withAllSessionsFiltered(new ScriptSessionFilter() {
      public boolean match(ScriptSession session) {
        if (session.getAttribute(attributeName) == null)
          return false;
        else {
          boolean f = session.getAttribute(attributeName).equals(sessionid);
          return f;
        }
      }
    }, new Runnable() {
      private ScriptBuffer script = new ScriptBuffer();
      public void run() {
        script.appendCall(Constants.DWRConfig.DWR_SCRIPT_FUNCTIONNAME, msg);
        Collection<ScriptSession> sessions = Browser.getTargetSessions();
        for (ScriptSession scriptSession : sessions) {
          scriptSession.addScript(script);
        }
      }
    });

  }


}
