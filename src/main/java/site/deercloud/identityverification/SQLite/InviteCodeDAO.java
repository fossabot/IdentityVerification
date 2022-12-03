package site.deercloud.identityverification.SQLite;

import site.deercloud.identityverification.HttpServer.model.InviteCode;
import site.deercloud.identityverification.Utils.MyLogger;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class InviteCodeDAO {
    public static void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS invite_code (\n"
                + "	code text PRIMARY KEY,\n"              // 邀请码
                + "	inviter_uuid text NOT NULL,\n"         // 所有者
                + "	create_time integer NOT NULL,\n"       // 创建时间
                + "	is_used integer NOT NULL,\n"           // 是否被使用
                + "	use_time integer NOT NULL\n"           // 使用时间
                + ");";
        Statement stat = null;
        stat = SqlManager.session.createStatement();
        stat.executeUpdate(sql);
    }

    public static void insert(String code, String inviterUUID, boolean isUsed, long useTime) throws SQLException {
        String sql = "INSERT INTO invite_code(code,inviter_uuid,create_time,is_used,use_time) VALUES(?,?,?,?,?)";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setString(1, code);
        prep.setString(2, inviterUUID);
        prep.setLong(3, System.currentTimeMillis());
        prep.setBoolean(4, isUsed);
        prep.setLong(5, useTime);
        prep.executeUpdate();
    }

    public static String getInviterUUID(String code) throws SQLException {
        String sql = "SELECT inviter_uuid FROM invite_code WHERE code = ?";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setString(1, code);
        ResultSet rs = prep.executeQuery();
        if (rs.next()) {
            return rs.getString("inviter_uuid");
        }
        return null;
    }

    public static boolean isUsed(String code) throws SQLException {
        String sql = "SELECT is_used FROM invite_code WHERE code = ?";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setString(1, code);
        ResultSet rs = prep.executeQuery();
        if (rs.next()) {
            return rs.getBoolean("is_used");
        }
        return true;
    }

    public static void setUsed(String code, boolean isUsed, long useTime) throws SQLException {
        String sql = "UPDATE invite_code SET is_used = ?,use_time = ? WHERE code = ?";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setBoolean(1, isUsed);
        prep.setLong(2, useTime);
        prep.setString(3, code);
        prep.executeUpdate();
    }

    public static long getUseTime(String code) throws SQLException {
        String sql = "SELECT use_time FROM invite_code WHERE code = ?";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setString(1, code);
        ResultSet rs = prep.executeQuery();
        if (rs.next()) {
            return rs.getLong("use_time");
        }
        return 0;
    }

    public static Set<InviteCode> selectByInviter(String inviterUUID) throws SQLException {
        String sql = "SELECT * FROM invite_code WHERE inviter_uuid = ?";
        PreparedStatement prep = SqlManager.session.prepareStatement(sql);
        prep.setString(1, inviterUUID);
        ResultSet rs = prep.executeQuery();
        Set<InviteCode> inviteCodes = new HashSet<>();
        while (rs.next()) {
            InviteCode inviteCode = new InviteCode();
            inviteCode.code = rs.getString("code");
            inviteCode.inviter = rs.getString("inviter_uuid");
            inviteCode.createTime = rs.getLong("create_time");
            inviteCode.isUsed = rs.getBoolean("is_used");
            inviteCode.usedTime = rs.getLong("use_time");
            inviteCodes.add(inviteCode);
        }
        return inviteCodes;
    }
}
