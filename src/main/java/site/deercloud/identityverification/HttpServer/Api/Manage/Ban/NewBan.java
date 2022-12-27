package site.deercloud.identityverification.HttpServer.Api.Manage.Ban;

import com.alibaba.fastjson.*;
import com.sun.net.httpserver.*;
import site.deercloud.identityverification.HttpServer.model.BanRecord;
import site.deercloud.identityverification.HttpServer.model.Profile;
import site.deercloud.identityverification.HttpServer.model.User;
import site.deercloud.identityverification.HttpServer.model.WebToken;
import site.deercloud.identityverification.SQLite.BanListDAO;
import site.deercloud.identityverification.SQLite.InviteRelationDAO;
import site.deercloud.identityverification.SQLite.ProfileDAO;
import site.deercloud.identityverification.Utils.MyLogger;

import static site.deercloud.identityverification.HttpServer.HttpServerManager.*;

public class NewBan implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange){
        try {
            if (!requestHeader(exchange, "POST")) return;
            WebToken webToken = authorizationCheck(exchange, User.ROLE.ADMIN);
            if (webToken == null) return;

            JSONObject jsonObject = getBody(exchange);
            BanRecord record = new BanRecord();
            record.uuid = jsonObject.getString("uuid");
            record.ban_reason = jsonObject.getString("reason");
            record.ban_time = jsonObject.getLong("time");
            BanListDAO.insert(record);

            // 连带处罚
            Boolean with_inviter = jsonObject.getBoolean("with_inviter");
            String inviter_uuid = null;
            if (with_inviter) {
                inviter_uuid = InviteRelationDAO.selectInviterByInvitee(record.uuid);
            }
            Profile inviter;
            if (inviter_uuid == null) {
                jsonResponse(exchange, 200, "操作成功，由于不存在邀请者，跳过连带处罚。", null);
                return;
            }
            inviter = ProfileDAO.selectByUuid(inviter_uuid);
            if (inviter == null) {
                jsonResponse(exchange, 200, "操作成功，由于不存在邀请者，跳过连带处罚。", null);
                return;
            }
            BanRecord inviter_record = new BanRecord();
            inviter_record.uuid = inviter_uuid;
            inviter_record.ban_time = record.ban_time;
            Profile profile = ProfileDAO.selectByUuid(record.uuid);
            String invitee_name;
            if (profile != null) {
                invitee_name = profile.name;
            } else {
                invitee_name = "";
            }
            inviter_record.ban_reason = "由于邀请的玩家["+invitee_name+"]因为["+record.ban_reason+"]被处罚而被连带处罚。";
            BanListDAO.insert(inviter_record);
            jsonResponse(exchange, 200, "操作成功，连带处罚了["+inviter.name+"]。", null);
        } catch (Exception e) {
            exchange.close();
            MyLogger.debug(e);
        }
    }
}
