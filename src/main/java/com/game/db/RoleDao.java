package com.game.db;

import com.game.model.Role;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据访问对象
 *
 * @author Harleysama
 */
@Slf4j
public class RoleDao {

    private MongoCollection<Document> getCollection() {
        return MongoManager.getInstance().getDatabase().getCollection("roles");
    }

    /**
     * 根据用户ID查找角色列表
     */
    public List<Role> findByUserId(Long userId) {
        List<Role> roles = new ArrayList<>();
        getCollection().find(Filters.eq("user_id", userId))
                .forEach(doc -> roles.add(documentToRole(doc)));
        return roles;
    }

    /**
     * 根据角色ID查找角色
     */
    public Role findById(Long roleId) {
        Document doc = getCollection().find(Filters.eq("_id", roleId)).first();
        return documentToRole(doc);
    }

    /**
     * 创建角色
     */
    public void create(Role role) {
        Document doc = roleToDocument(role);
        getCollection().insertOne(doc);
        log.info("创建角色: roleId={}, name={}", role.getId(), role.getName());
    }

    /**
     * 更新角色
     */
    public void update(Role role) {
        getCollection().updateOne(
                Filters.eq("_id", role.getId()),
                new Document("$set", roleToDocument(role))
        );
        log.debug("更新角色: roleId={}", role.getId());
    }

    /**
     * 删除角色
     */
    public void delete(Long roleId) {
        getCollection().deleteOne(Filters.eq("_id", roleId));
        log.info("删除角色: roleId={}", roleId);
    }

    /**
     * Document -> Role 转换
     */
    private Role documentToRole(Document doc) {
        if (doc == null) {
            return null;
        }
        Role role = new Role();
        role.setId(doc.getLong("_id"));
        role.setUserId(doc.getLong("user_id"));
        role.setName(doc.getString("name"));
        role.setLevel(doc.getInteger("level"));
        role.setProfession(doc.getInteger("profession"));
        role.setExp(doc.getLong("exp"));
        role.setGold(doc.getLong("gold"));
        role.setDiamond(doc.getLong("diamond"));
        role.setHp(doc.getInteger("hp"));
        role.setMaxHp(doc.getInteger("max_hp"));
        role.setMp(doc.getInteger("mp"));
        role.setMaxMp(doc.getInteger("max_mp"));
        role.setAttack(doc.getInteger("attack"));
        role.setDefense(doc.getInteger("defense"));
        role.setSpeed(doc.getInteger("speed"));
        role.setLastLoginTime(doc.getLong("last_login_time"));
        return role;
    }

    /**
     * Role -> Document 转换
     */
    private Document roleToDocument(Role role) {
        Document doc = new Document();
        doc.append("_id", role.getId());
        doc.append("user_id", role.getUserId());
        doc.append("name", role.getName());
        doc.append("level", role.getLevel());
        doc.append("profession", role.getProfession());
        doc.append("exp", role.getExp());
        doc.append("gold", role.getGold());
        doc.append("diamond", role.getDiamond());
        doc.append("hp", role.getHp());
        doc.append("max_hp", role.getMaxHp());
        doc.append("mp", role.getMp());
        doc.append("max_mp", role.getMaxMp());
        doc.append("attack", role.getAttack());
        doc.append("defense", role.getDefense());
        doc.append("speed", role.getSpeed());
        doc.append("last_login_time", role.getLastLoginTime());
        return doc;
    }
}
