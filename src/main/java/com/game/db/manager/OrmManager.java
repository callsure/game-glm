package com.game.db.manager;

import com.game.db.cache.EntityCaches;
import com.game.db.cache.IEntityCaches;
import com.game.db.model.anno.*;
import com.game.db.model.config.CacheStrategy;
import com.game.db.model.config.HostConfig;
import com.game.db.model.config.OrmConfig;
import com.game.db.model.config.PersisterStrategy;
import com.game.db.model.entity.IEntity;
import com.game.db.model.vo.EntityDef;
import com.game.db.model.vo.IndexDef;
import com.game.db.model.vo.IndexTextDef;
import com.game.utils.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ORM 管理器实现类
 * 负责 ORM 框架的核心管理功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于 ORM 管理
 * - 依赖注入：通过配置获取依赖
 * - 工厂模式：创建和管理实体缓存
 *
 * @author Harleysama
 */
@Slf4j
public class OrmManager implements IOrmManager {

    private final OrmConfig ormConfig;

    private MongoClient mongoClient;

    private MongoDatabase mongodbDatabase;

    /**
     * 全部的 Entity 定义，key 为对应的 class，value 为当前的 Entity 是否在当前项目中以缓存的形式使用
     */
    private final Map<Class<?>, Boolean> allEntityCachesUsableMap = new HashMap<>();

    /**
     * 实体缓存映射
     */
    private final Map<Class<? extends IEntity<?>>, IEntityCaches<?, ?>> entityCachesMap = new HashMap<>();

    /**
     * 集合名称映射
     */
    private final Map<Class<? extends IEntity<?>>, String> collectionNameMap = new ConcurrentHashMap<>();

    public OrmManager(OrmConfig ormConfig) {
        this.ormConfig = ormConfig;
    }

    @Override
    public void initBefore() {
        log.info("ORM 初始化开始...");
        Map<Class<? extends IEntity<?>>, EntityDef> entityDefMap = scanEntityClass();

        for (EntityDef entityDef : entityDefMap.values()) {
            EntityCaches<?, ?> entityCaches = new EntityCaches<>(entityDef);
            entityCachesMap.put(entityDef.getClazz(), entityCaches);
            allEntityCachesUsableMap.put(entityDef.getClazz(), false);
        }

        // 配置 CodecRegistry
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings.Builder mongoBuilder = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry);

        // 设置数据库地址
        HostConfig hostConfig = ormConfig.getHost();
        if (CollUtil.isNotEmpty(hostConfig.getAddress())) {
            List<ServerAddress> hostList = hostConfig.getAddress().values().stream()
                    .map(this::parseServerAddress)
                    .collect(Collectors.toList());
            mongoBuilder.applyToClusterSettings(builder -> builder.hosts(hostList));
        }

        // 设置数据库账号密码
        if (StrUtil.isNotBlank(hostConfig.getUser()) && StrUtil.isNotBlank(hostConfig.getPassword())) {
            mongoBuilder.credential(MongoCredential.createCredential(
                    hostConfig.getUser(), "admin", hostConfig.getPassword().toCharArray()));
        }

        // 设置连接池大小
        int maxConnection = Runtime.getRuntime().availableProcessors() * 2 + 1;
        mongoBuilder.applyToConnectionPoolSettings(builder -> builder.maxSize(maxConnection).minSize(1));

        mongoClient = MongoClients.create(mongoBuilder.build());
        mongodbDatabase = mongoClient.getDatabase(hostConfig.getDatabase());

        // 创建索引
        createIndexes(entityDefMap);

        log.info("ORM 初始化完成! 共扫描到 {} 个实体类", entityDefMap.size());
    }

    @Override
    public void inject() {
        log.info("开始注入实体缓存...");
        Set<Class<?>> classes = ClassUtil.scanPackage(ormConfig.getEntityPackage());

        for (Class<?> clz : classes) {
            // 查找所有包含 @EntityCachesInjection 注解的字段
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(EntityCachesInjection.class)) {
                    injectField(clz, field);
                }
            }
        }

        log.info("实体缓存注入完成!");
    }

    @Override
    public void initAfter() {
        // 清理未使用的缓存
        allEntityCachesUsableMap.entrySet().stream()
                .filter(it -> !it.getValue())
                .map(Entry::getKey)
                .forEach(entityCachesMap::remove);
    }

    @Override
    public MongoClient mongoClient() {
        return mongoClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends IEntity<?>> IEntityCaches<?, E> getEntityCaches(Class<E> clazz) {
        Boolean usable = allEntityCachesUsableMap.get(clazz);
        if (usable == null) {
            throw new IllegalArgumentException("没有定义 [" + clazz.getCanonicalName() + "] 的 EntityCaches");
        }
        if (!usable) {
            throw new IllegalArgumentException("Orm 没有使用 [" + clazz.getCanonicalName() + "] 的 EntityCaches");
        }
        return (IEntityCaches<?, E>) entityCachesMap.get(clazz);
    }

    @Override
    public Collection<IEntityCaches<?, ?>> getAllEntityCaches() {
        return Collections.unmodifiableCollection(entityCachesMap.values());
    }

    @Override
    public <E extends IEntity<?>> MongoCollection<E> getCollection(Class<E> entityClazz) {
        String collectionName = collectionNameMap.get(entityClazz);
        if (collectionName == null) {
            collectionName = StrUtil.removeSufAndLowerFirst(entityClazz.getSimpleName(), "Entity");
            collectionNameMap.put(entityClazz, collectionName);
        }
        return mongodbDatabase.getCollection(collectionName, entityClazz);
    }

    @Override
    public MongoCollection<Document> getCollection(String collection) {
        return mongodbDatabase.getCollection(collection);
    }

    // ==================== 私有方法 ====================

    /**
     * 扫描实体类
     */
    private Map<Class<? extends IEntity<?>>, EntityDef> scanEntityClass() {
        Map<Class<? extends IEntity<?>>, EntityDef> cacheDefMap = new HashMap<>();

        Set<String> locationSet = scanEntityCacheAnno(ormConfig.getEntityPackage());
        for (String location : locationSet) {
            Class<? extends IEntity<?>> entityClazz;
            try {
                entityClazz = (Class<? extends IEntity<?>>) Class.forName(location);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("无法获取实体类: " + location, e);
            }
            EntityDef cacheDef = parserEntityDef(entityClazz);
            EntityDef previousCacheDef = cacheDefMap.putIfAbsent(entityClazz, cacheDef);
            AssertUtil.isFalse(previousCacheDef != null, "缓存实体不能包含重复的[class:{}]", entityClazz.getSimpleName());
        }
        return cacheDefMap;
    }

    /**
     * 扫描带有 @EntityCache 注解的类
     */
    private Set<String> scanEntityCacheAnno(String scanLocation) {
        String prefixPattern = "classpath*:";
        String suffixPattern = "**/*.class";

        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        try {
            String packageSearchPath = prefixPattern + scanLocation.replace('.', '/') + '/' + suffixPattern;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            Set<String> result = new HashSet<>();
            String name = EntityCache.class.getName();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annoMeta = metadataReader.getAnnotationMetadata();
                    if (annoMeta.hasAnnotation(name)) {
                        ClassMetadata clazzMeta = metadataReader.getClassMetadata();
                        result.add(clazzMeta.getClassName());
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("无法读取实体信息", e);
        }
    }

    /**
     * 解析实体定义
     */
    public EntityDef parserEntityDef(Class<? extends IEntity<?>> clazz) {
        analyze(clazz);

        List<CacheStrategy> cacheStrategies = ormConfig.getCaches();
        List<PersisterStrategy> persisterStrategies = ormConfig.getPersisters();

        EntityCache entityCache = clazz.getAnnotation(EntityCache.class);
        Cache cache = entityCache.cache();
        Optional<CacheStrategy> cacheStrategyOptional = cacheStrategies.stream()
                .filter(it -> it.getStrategy().equals(cache.value()))
                .findFirst();
        AssertUtil.isTrue(cacheStrategyOptional.isPresent(),
                "实体类Entity[{}]没有找到缓存策略[{}]", clazz.getSimpleName(), cache.value());

        CacheStrategy cacheStrategy = cacheStrategyOptional.get();
        int cacheSize = cacheStrategy.getSize();
        long expireMillisecond = cacheStrategy.getExpireMillisecond();

        Field idField = getFieldsByAnno(clazz, Id.class)[0];
        ReflectUtil.setAccessible(idField);

        Persister persister = entityCache.persister();
        Optional<PersisterStrategy> persisterStrategyOptional = persisterStrategies.stream()
                .filter(it -> it.getStrategy().equals(persister.value()))
                .findFirst();
        AssertUtil.isTrue(persisterStrategyOptional.isPresent(),
                "实体类Entity[{}]没有找到持久化策略[{}]", clazz.getSimpleName(), persister.value());

        PersisterStrategy persisterStrategy = persisterStrategyOptional.get();

        Map<String, IndexDef> indexDefMap = new HashMap<>();
        Field[] fields = getFieldsByAnno(clazz, Index.class);
        for (Field field : fields) {
            Index indexAnnotation = field.getAnnotation(Index.class);
            if (indexAnnotation.ttlExpireAfterSeconds() > 0) {
                Type fieldType = field.getGenericType();
                if (!(fieldType == Date.class || "java.util.List<java.util.Date>".equals(field.getGenericType().toString()))) {
                    throw new IllegalArgumentException(StrUtil.format(
                            "MongoDB规定TTL类型[{}]必须是Date或List<Date>", field.getName()));
                }
            }

            IndexDef indexDef = new IndexDef(field, indexAnnotation.ascending(),
                    indexAnnotation.unique(), indexAnnotation.ttlExpireAfterSeconds());
            indexDefMap.put(field.getName(), indexDef);
        }

        Map<String, IndexTextDef> indexTextDefMap = new HashMap<>();
        fields = getFieldsByAnno(clazz, IndexText.class);
        for (Field field : fields) {
            IndexTextDef indexTextDef = new IndexTextDef(field, field.getAnnotation(IndexText.class));
            indexTextDefMap.put(field.getName(), indexTextDef);
        }

        return EntityDef.valueOf(idField, clazz, cacheSize, expireMillisecond,
                persisterStrategy, indexDefMap, indexTextDefMap);
    }

    /**
     * 分析实体类
     */
    private void analyze(Class<?> clazz) {
        // 是否实现了 IEntity 接口
        AssertUtil.isTrue(IEntity.class.isAssignableFrom(clazz),
                "被[{}]注解标注的实体类[{}]没有实现接口[{}]",
                EntityCache.class.getName(), clazz.getCanonicalName(), IEntity.class.getCanonicalName());

        // 实体类必须被注解 EntityCache 标注
        AssertUtil.notNull(clazz.getAnnotation(EntityCache.class),
                "实体类Entity[{}]必须被注解[{}]标注", clazz.getCanonicalName(), EntityCache.class.getName());

        // 校验 entity 格式
        checkEntity(clazz);

        // 校验 id 字段和 id() 方法的格式
        Field[] idFields = getFieldsByAnno(clazz, Id.class);
        AssertUtil.isTrue(ArrayUtils.isNotEmpty(idFields) && idFields.length == 1,
                "实体类Entity[{}]必须只有且仅有一个Id注解", clazz.getSimpleName());
        Field idField = idFields[0];
        AssertUtil.isTrue(Modifier.isPrivate(idField.getModifiers()),
                "实体类Entity[{}]的id必须是private私有的", clazz.getSimpleName());

        // 验证 id() 方法
        verifyIdMethod(clazz, idField);

        // 校验 gvs() 和 svs() 方法
        verifyVersionMethods(clazz);
    }

    /**
     * 验证 id() 方法
     */
    private void verifyIdMethod(Class<?> clazz, Field idField) {
        Object entityInstance = ReflectUtil.newInstance(clazz);
        Class<?> idFieldType = idField.getType();
        Object idFieldValue = generateRandomValue(idFieldType);

        ReflectUtil.setAccessible(idField);
        ReflectUtil.setFieldValue(entityInstance, idField, idFieldValue);

        Optional<Method> idMethodOptional = Arrays.stream(ReflectUtil.getMethods(clazz))
                .filter(it -> it.getName().equalsIgnoreCase("id"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();

        AssertUtil.isTrue(idMethodOptional.isPresent(),
                "实体类Entity[{}]必须重写id()方法", clazz.getSimpleName());

        Method idMethod = idMethodOptional.get();
        ReflectUtil.setAccessible(idMethod);
        Object idMethodReturnValue = ReflectUtil.invoke(entityInstance, idMethod);

        AssertUtil.isTrue(idFieldValue.equals(idMethodReturnValue),
                "实体类Entity[{}]的id字段值[field:{}]和id方法返回值[method:{}]不相等",
                clazz.getSimpleName(), idFieldValue, idMethodReturnValue);
    }

    /**
     * 验证版本方法
     */
    private void verifyVersionMethods(Class<?> clazz) {
        Object entityInstance = ReflectUtil.newInstance(clazz);

        Optional<Method> gvsMethodOptional = Arrays.stream(ReflectUtil.getMethods(clazz))
                .filter(it -> it.getName().equals("gvs"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();

        Optional<Method> svsMethodOptional = Arrays.stream(ReflectUtil.getMethods(clazz))
                .filter(it -> it.getName().equals("svs"))
                .filter(it -> it.getParameterCount() == 1)
                .filter(it -> it.getParameterTypes()[0].equals(long.class))
                .findFirst();

        // gvs 和 svs 要实现都实现，不实现都不实现
        if (!gvsMethodOptional.isPresent() || !svsMethodOptional.isPresent()) {
            AssertUtil.isTrue(!gvsMethodOptional.isPresent() && !svsMethodOptional.isPresent(),
                    "实体类Entity[{}]的gvs和svs方法要实现都实现，不实现都不实现", clazz.getSimpleName());
            return;
        }

        Method gvsMethod = gvsMethodOptional.get();
        Method svsMethod = svsMethodOptional.get();
        long vsValue = RandomUtil.randomLong();
        ReflectUtil.invoke(entityInstance, svsMethod, vsValue);
        Object gvsReturnValue = ReflectUtil.invoke(entityInstance, gvsMethod);

        AssertUtil.isTrue(gvsReturnValue.equals(vsValue),
                "实体类Entity[{}]的gvs方法和svs方法定义格式不正确", clazz.getSimpleName());
    }

    /**
     * 检查实体类
     * 使用 ReflectionUtils 进行 POJO 类验证
     */
    private void checkEntity(Class<?> clazz) {
        // 验证是否为 POJO 类
        ReflectionUtils.assertIsPojoClass(clazz);

        // 不能是泛型类
        AssertUtil.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()),
                "[class:{}]不能是泛型类", clazz.getCanonicalName());

        // 必须要有一个空的构造器
        ReflectionUtils.publicEmptyConstructor(clazz);

        // 检查所有字段
        List<Field> fieldList = ReflectionUtils.notStaticAndTransientFields(clazz);

        for (Field field : fieldList) {
            Class<?> fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // 基本类型，跳过
            } else if (fieldType.isArray()) {
                checkSubEntity(clazz, fieldType.getComponentType());
            } else if (Set.class.isAssignableFrom(fieldType)) {
                AssertUtil.isTrue(fieldType.equals(Set.class),
                        "ORM[class:{}]类型声明不正确，必须是Set接口类型", clazz.getCanonicalName());
                checkGenericType(clazz, field, 1);
            } else if (List.class.isAssignableFrom(fieldType)) {
                AssertUtil.isTrue(fieldType.equals(List.class),
                        "ORM[class:{}]类型声明不正确，必须是List接口类型", clazz.getCanonicalName());
                checkGenericType(clazz, field, 1);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                AssertUtil.isTrue(fieldType.equals(Map.class),
                        "ORM[class:{}]类型声明不正确，必须是Map接口类型", clazz.getCanonicalName());
                checkMapType(clazz, field);
            } else {
                checkEntity(fieldType);
            }
        }
    }

    /**
     * 检查子实体
     */
    private void checkSubEntity(Class<?> currentEntityClass, Type type) {
        // 简化实现，省略复杂嵌套检查
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (!isBaseType(clazz)) {
                checkEntity(clazz);
            }
        }
    }

    /**
     * 检查泛型类型
     */
    private void checkGenericType(Class<?> clazz, Field field, int expectedTypeArgs) {
        Type type = field.getGenericType();
        AssertUtil.isTrue(type instanceof ParameterizedType,
                "ORM[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());

        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        AssertUtil.isTrue(types.length == expectedTypeArgs,
                "ORM[class:{}]中集合类型声明不正确，[field:{}]必须声明泛型类",
                clazz.getCanonicalName(), field.getName());
    }

    /**
     * 检查 Map 类型
     */
    private void checkMapType(Class<?> clazz, Field field) {
        Type type = field.getGenericType();
        AssertUtil.isTrue(type instanceof ParameterizedType,
                "ORM[class:{}]中Map类型声明不正确，[field:{}]不是泛型类",
                clazz.getCanonicalName(), field.getName());

        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        AssertUtil.isTrue(types.length == 2,
                "ORM[class:{}]中Map类型声明不正确，[field:{}]必须声明两个泛型",
                clazz.getCanonicalName(), field.getName());

        Type keyType = types[0];
        if (!String.class.isAssignableFrom((Class<?>) keyType)) {
            throw new IllegalArgumentException("ORM[class:{}]中Map的key类型必须为String类型");
        }
    }

    /**
     * 判断是否为基本类型
     */
    private boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz);
    }

    /**
     * 获取带有指定注解的所有字段
     *
     * @param clazz     类
     * @param annoClass 注解类型
     * @return 带有指定注解的字段数组
     */
    private Field[] getFieldsByAnno(Class<?> clazz, Class<? extends Annotation> annoClass) {
        Field[] allFields = clazz.getDeclaredFields();
        java.util.List<Field> filteredFields = new java.util.ArrayList<>();

        for (Field field : allFields) {
            if (field.isAnnotationPresent(annoClass)) {
                filteredFields.add(field);
            }
        }

        return filteredFields.toArray(new Field[0]);
    }

    /**
     * 生成随机值
     */
    private Object generateRandomValue(Class<?> type) {
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return RandomUtil.randomInt();
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return RandomUtil.randomLong();
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return (float) RandomUtil.randomDouble();
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return RandomUtil.randomDouble();
        } else if (type.equals(String.class)) {
            return RandomUtil.randomString(10);
        } else {
            throw new IllegalArgumentException("不支持的ID类型: " + type);
        }
    }

    /**
     * 解析服务器地址
     * 使用 HostAndPort 工具类解析
     */
    private ServerAddress parseServerAddress(String address) {
        HostAndPort hostAndPort = HostAndPort.valueOf(address);
        return new ServerAddress(hostAndPort.getHost(), hostAndPort.getPort());
    }

    /**
     * 创建索引
     */
    private void createIndexes(Map<Class<? extends IEntity<?>>, EntityDef> entityDefMap) {
        for (EntityDef entityDef : entityDefMap.values()) {
            Map<String, IndexDef> indexDefMap = entityDef.getIndexDefMap();
            if (CollUtil.isNotEmpty(indexDefMap)) {
                MongoCollection<? extends IEntity<?>> collection = getCollection(entityDef.getClazz());
                for (Entry<String, IndexDef> indexDef : indexDefMap.entrySet()) {
                    String fieldName = indexDef.getKey();
                    IndexDef index = indexDef.getValue();

                    if (!hasIndex(collection, fieldName)) {
                        IndexOptions indexOptions = new IndexOptions();
                        indexOptions.unique(index.isUnique());

                        if (index.getTtlExpireAfterSeconds() > 0) {
                            indexOptions.expireAfter(index.getTtlExpireAfterSeconds(), TimeUnit.SECONDS);
                        }

                        if (index.isAscending()) {
                            collection.createIndex(Indexes.ascending(fieldName), indexOptions);
                        } else {
                            collection.createIndex(Indexes.descending(fieldName), indexOptions);
                        }
                        log.info("创建索引: collection={}, field={}", getCollectionName(entityDef.getClazz()), fieldName);
                    }
                }
            }

            Map<String, IndexTextDef> indexTextDefMap = entityDef.getIndexTextDefMap();
            if (CollUtil.isNotEmpty(indexTextDefMap)) {
                MongoCollection<? extends IEntity<?>> collection = getCollection(entityDef.getClazz());
                for (Entry<String, IndexTextDef> indexTextDef : indexTextDefMap.entrySet()) {
                    String fieldName = indexTextDef.getKey();
                    if (!hasIndex(collection, fieldName)) {
                        collection.createIndex(Indexes.text(fieldName));
                        log.info("创建文本索引: collection={}, field={}", getCollectionName(entityDef.getClazz()), fieldName);
                    }
                }
            }
        }
    }

    /**
     * 检查索引是否存在
     */
    private boolean hasIndex(MongoCollection<?> collection, String fieldName) {
        for (Document document : collection.listIndexes()) {
            Document keyDocument = (Document) document.get("key");
            if (keyDocument.containsKey(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取集合名称
     */
    private String getCollectionName(Class<?> entityClazz) {
        return StrUtil.removeSufAndLowerFirst(entityClazz.getSimpleName(), "Entity");
    }

    /**
     * 注入字段
     */
    private void injectField(Class<?> clz, Field field) {
        Type type = field.getGenericType();

        if (!(type instanceof ParameterizedType)) {
            throw new RuntimeException("变量[" + field.getName() + "]的类型不是泛型类");
        }

        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        Class<? extends IEntity<?>> entityClazz = (Class<? extends IEntity<?>>) types[1];
        IEntityCaches<?, ?> entityCaches = entityCachesMap.get(entityClazz);

        if (entityCaches == null) {
            throw new RuntimeException("实体缓存对象不存在，请检查配置: " + entityClazz);
        }

        // 获取单例实例并注入
        try {
            Object bean = getSingletonInstance(clz);
            ReflectUtil.setAccessible(field);
            ReflectUtil.setFieldValue(bean, field, entityCaches);
            allEntityCachesUsableMap.put(entityClazz, true);
            log.debug("注入实体缓存: class={}, field={}, entity={}",
                    clz.getSimpleName(), field.getName(), entityClazz.getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("注入字段失败: " + field.getName(), e);
        }
    }

    /**
     * 获取单例实例
     */
    private Object getSingletonInstance(Class<?> clz) {
        try {
            Method method = clz.getMethod("getInstance");
            if (method != null) {
                return ReflectUtil.invokeStatic(method);
            }
            return ReflectUtil.newInstance(clz);
        } catch (NoSuchMethodException e) {
            try {
                return ReflectUtil.newInstance(clz);
            } catch (Exception ex) {
                throw new RuntimeException("无法创建实例: " + clz.getSimpleName(), ex);
            }
        }
    }
}
