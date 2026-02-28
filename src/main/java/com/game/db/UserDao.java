package com.game.db;

import com.game.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import static com.mongodb.client.model.Updates.set;

/**
 * 用户数据访问对象
 *
 * @author Harleysama
 */
@Slf4j
public class UserDao {

    private MongoCollection<Document> getCollection() {
        return MongoManager.getInstance().getDatabase().getCollection("users");
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        Document doc = getCollection().find(Filters.eq("username", username)).first();
        return documentToUser(doc);
    }

    /**
     * 根据ID查找用户
     */
    public User findById(Long userId) {
        Document doc = getCollection().find(Filters.eq("_id", userId)).first();
        return documentToUser(doc);
    }

    /**
     * 创建用户
     */
    public void create(User user) {
        Document doc = userToDocument(user);
        getCollection().insertOne(doc);
        log.info("创建用户: userId={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * 更新用户
     */
    public void update(User user) {
        getCollection().updateOne(
                Filters.eq("_id", user.getId()),
                set("last_login_time", user.getLastLoginTime())
        );
        log.debug("更新用户: userId={}", user.getId());
    }

    /**
     * Document -> User 转换
     */
    private User documentToUser(Document doc) {
        if (doc == null) {
            return null;
        }
        User user = new User();
        user.setId(doc.getLong("_id"));
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setCreateTime(doc.getLong("create_time"));
        user.setLastLoginTime(doc.getLong("last_login_time"));
        return user;
    }

    /**
     * User -> Document 转换
     */
    private Document userToDocument(User user) {
        Document doc = new Document();
        doc.append("_id", user.getId());
        doc.append("username", user.getUsername());
        doc.append("password", user.getPassword());
        doc.append("create_time", user.getCreateTime());
        doc.append("last_login_time", user.getLastLoginTime());
        return doc;
    }
}
