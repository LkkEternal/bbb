package com.chanle.app.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.FtsTableInfo;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RecipeDatabase_Impl extends RecipeDatabase {
  private volatile DishDao _dishDao;

  private volatile IngredientDao _ingredientDao;

  private volatile StepDao _stepDao;

  private volatile TipDao _tipDao;

  private volatile TagDao _tagDao;

  private volatile AliasDao _aliasDao;

  private volatile DishFtsDao _dishFtsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `dishes` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `namePinyin` TEXT NOT NULL, `category` TEXT NOT NULL, `description` TEXT NOT NULL, `imageUrl` TEXT, `videoUrl` TEXT, `videoTitle` TEXT, `videoPlatform` TEXT, `baseServings` INTEGER NOT NULL, `timeMinutes` INTEGER NOT NULL, `difficulty` TEXT NOT NULL, `nutritionNote` TEXT NOT NULL, `nutritionKcal` INTEGER NOT NULL, `nutritionProtein` REAL NOT NULL, `nutritionFat` REAL NOT NULL, `nutritionCarbs` REAL NOT NULL, `source` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `model` TEXT NOT NULL, `prompt` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingredients` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishId` TEXT NOT NULL, `name` TEXT NOT NULL, `amount` REAL, `unit` TEXT NOT NULL, `note` TEXT NOT NULL, `scalable` INTEGER NOT NULL, FOREIGN KEY(`dishId`) REFERENCES `dishes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingredients_dishId` ON `ingredients` (`dishId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `steps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishId` TEXT NOT NULL, `stepNumber` INTEGER NOT NULL, `description` TEXT NOT NULL, FOREIGN KEY(`dishId`) REFERENCES `dishes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_steps_dishId` ON `steps` (`dishId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tips` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishId` TEXT NOT NULL, `content` TEXT NOT NULL, FOREIGN KEY(`dishId`) REFERENCES `dishes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tips_dishId` ON `tips` (`dishId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishId` TEXT NOT NULL, `tagName` TEXT NOT NULL, FOREIGN KEY(`dishId`) REFERENCES `dishes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_dishId` ON `tags` (`dishId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `aliases` (`alias` TEXT NOT NULL, `dishName` TEXT NOT NULL, PRIMARY KEY(`alias`))");
        db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `dishes_fts` USING FTS4(`name` TEXT NOT NULL, `namePinyin` TEXT NOT NULL, `description` TEXT NOT NULL, content=`dishes`)");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_BEFORE_UPDATE BEFORE UPDATE ON `dishes` BEGIN DELETE FROM `dishes_fts` WHERE `docid`=OLD.`rowid`; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_BEFORE_DELETE BEFORE DELETE ON `dishes` BEGIN DELETE FROM `dishes_fts` WHERE `docid`=OLD.`rowid`; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_AFTER_UPDATE AFTER UPDATE ON `dishes` BEGIN INSERT INTO `dishes_fts`(`docid`, `name`, `namePinyin`, `description`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`namePinyin`, NEW.`description`); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_AFTER_INSERT AFTER INSERT ON `dishes` BEGIN INSERT INTO `dishes_fts`(`docid`, `name`, `namePinyin`, `description`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`namePinyin`, NEW.`description`); END");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '640a64d0827b74c5b946d2bc45e26067')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `dishes`");
        db.execSQL("DROP TABLE IF EXISTS `ingredients`");
        db.execSQL("DROP TABLE IF EXISTS `steps`");
        db.execSQL("DROP TABLE IF EXISTS `tips`");
        db.execSQL("DROP TABLE IF EXISTS `tags`");
        db.execSQL("DROP TABLE IF EXISTS `aliases`");
        db.execSQL("DROP TABLE IF EXISTS `dishes_fts`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_BEFORE_UPDATE BEFORE UPDATE ON `dishes` BEGIN DELETE FROM `dishes_fts` WHERE `docid`=OLD.`rowid`; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_BEFORE_DELETE BEFORE DELETE ON `dishes` BEGIN DELETE FROM `dishes_fts` WHERE `docid`=OLD.`rowid`; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_AFTER_UPDATE AFTER UPDATE ON `dishes` BEGIN INSERT INTO `dishes_fts`(`docid`, `name`, `namePinyin`, `description`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`namePinyin`, NEW.`description`); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_dishes_fts_AFTER_INSERT AFTER INSERT ON `dishes` BEGIN INSERT INTO `dishes_fts`(`docid`, `name`, `namePinyin`, `description`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`namePinyin`, NEW.`description`); END");
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDishes = new HashMap<String, TableInfo.Column>(21);
        _columnsDishes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("namePinyin", new TableInfo.Column("namePinyin", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("videoUrl", new TableInfo.Column("videoUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("videoTitle", new TableInfo.Column("videoTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("videoPlatform", new TableInfo.Column("videoPlatform", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("baseServings", new TableInfo.Column("baseServings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("timeMinutes", new TableInfo.Column("timeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("nutritionNote", new TableInfo.Column("nutritionNote", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("nutritionKcal", new TableInfo.Column("nutritionKcal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("nutritionProtein", new TableInfo.Column("nutritionProtein", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("nutritionFat", new TableInfo.Column("nutritionFat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("nutritionCarbs", new TableInfo.Column("nutritionCarbs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("model", new TableInfo.Column("model", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDishes.put("prompt", new TableInfo.Column("prompt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDishes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDishes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDishes = new TableInfo("dishes", _columnsDishes, _foreignKeysDishes, _indicesDishes);
        final TableInfo _existingDishes = TableInfo.read(db, "dishes");
        if (!_infoDishes.equals(_existingDishes)) {
          return new RoomOpenHelper.ValidationResult(false, "dishes(com.chanle.app.data.database.DishEntity).\n"
                  + " Expected:\n" + _infoDishes + "\n"
                  + " Found:\n" + _existingDishes);
        }
        final HashMap<String, TableInfo.Column> _columnsIngredients = new HashMap<String, TableInfo.Column>(7);
        _columnsIngredients.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("dishId", new TableInfo.Column("dishId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("amount", new TableInfo.Column("amount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngredients.put("scalable", new TableInfo.Column("scalable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIngredients = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysIngredients.add(new TableInfo.ForeignKey("dishes", "CASCADE", "NO ACTION", Arrays.asList("dishId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesIngredients = new HashSet<TableInfo.Index>(1);
        _indicesIngredients.add(new TableInfo.Index("index_ingredients_dishId", false, Arrays.asList("dishId"), Arrays.asList("ASC")));
        final TableInfo _infoIngredients = new TableInfo("ingredients", _columnsIngredients, _foreignKeysIngredients, _indicesIngredients);
        final TableInfo _existingIngredients = TableInfo.read(db, "ingredients");
        if (!_infoIngredients.equals(_existingIngredients)) {
          return new RoomOpenHelper.ValidationResult(false, "ingredients(com.chanle.app.data.database.IngredientEntity).\n"
                  + " Expected:\n" + _infoIngredients + "\n"
                  + " Found:\n" + _existingIngredients);
        }
        final HashMap<String, TableInfo.Column> _columnsSteps = new HashMap<String, TableInfo.Column>(4);
        _columnsSteps.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("dishId", new TableInfo.Column("dishId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("stepNumber", new TableInfo.Column("stepNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSteps.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSteps = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSteps.add(new TableInfo.ForeignKey("dishes", "CASCADE", "NO ACTION", Arrays.asList("dishId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSteps = new HashSet<TableInfo.Index>(1);
        _indicesSteps.add(new TableInfo.Index("index_steps_dishId", false, Arrays.asList("dishId"), Arrays.asList("ASC")));
        final TableInfo _infoSteps = new TableInfo("steps", _columnsSteps, _foreignKeysSteps, _indicesSteps);
        final TableInfo _existingSteps = TableInfo.read(db, "steps");
        if (!_infoSteps.equals(_existingSteps)) {
          return new RoomOpenHelper.ValidationResult(false, "steps(com.chanle.app.data.database.StepEntity).\n"
                  + " Expected:\n" + _infoSteps + "\n"
                  + " Found:\n" + _existingSteps);
        }
        final HashMap<String, TableInfo.Column> _columnsTips = new HashMap<String, TableInfo.Column>(3);
        _columnsTips.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTips.put("dishId", new TableInfo.Column("dishId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTips.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTips = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTips.add(new TableInfo.ForeignKey("dishes", "CASCADE", "NO ACTION", Arrays.asList("dishId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTips = new HashSet<TableInfo.Index>(1);
        _indicesTips.add(new TableInfo.Index("index_tips_dishId", false, Arrays.asList("dishId"), Arrays.asList("ASC")));
        final TableInfo _infoTips = new TableInfo("tips", _columnsTips, _foreignKeysTips, _indicesTips);
        final TableInfo _existingTips = TableInfo.read(db, "tips");
        if (!_infoTips.equals(_existingTips)) {
          return new RoomOpenHelper.ValidationResult(false, "tips(com.chanle.app.data.database.TipEntity).\n"
                  + " Expected:\n" + _infoTips + "\n"
                  + " Found:\n" + _existingTips);
        }
        final HashMap<String, TableInfo.Column> _columnsTags = new HashMap<String, TableInfo.Column>(3);
        _columnsTags.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTags.put("dishId", new TableInfo.Column("dishId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTags.put("tagName", new TableInfo.Column("tagName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTags = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTags.add(new TableInfo.ForeignKey("dishes", "CASCADE", "NO ACTION", Arrays.asList("dishId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTags = new HashSet<TableInfo.Index>(1);
        _indicesTags.add(new TableInfo.Index("index_tags_dishId", false, Arrays.asList("dishId"), Arrays.asList("ASC")));
        final TableInfo _infoTags = new TableInfo("tags", _columnsTags, _foreignKeysTags, _indicesTags);
        final TableInfo _existingTags = TableInfo.read(db, "tags");
        if (!_infoTags.equals(_existingTags)) {
          return new RoomOpenHelper.ValidationResult(false, "tags(com.chanle.app.data.database.TagEntity).\n"
                  + " Expected:\n" + _infoTags + "\n"
                  + " Found:\n" + _existingTags);
        }
        final HashMap<String, TableInfo.Column> _columnsAliases = new HashMap<String, TableInfo.Column>(2);
        _columnsAliases.put("alias", new TableInfo.Column("alias", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAliases.put("dishName", new TableInfo.Column("dishName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAliases = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAliases = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAliases = new TableInfo("aliases", _columnsAliases, _foreignKeysAliases, _indicesAliases);
        final TableInfo _existingAliases = TableInfo.read(db, "aliases");
        if (!_infoAliases.equals(_existingAliases)) {
          return new RoomOpenHelper.ValidationResult(false, "aliases(com.chanle.app.data.database.AliasEntity).\n"
                  + " Expected:\n" + _infoAliases + "\n"
                  + " Found:\n" + _existingAliases);
        }
        final HashSet<String> _columnsDishesFts = new HashSet<String>(3);
        _columnsDishesFts.add("name");
        _columnsDishesFts.add("namePinyin");
        _columnsDishesFts.add("description");
        final FtsTableInfo _infoDishesFts = new FtsTableInfo("dishes_fts", _columnsDishesFts, "CREATE VIRTUAL TABLE IF NOT EXISTS `dishes_fts` USING FTS4(`name` TEXT NOT NULL, `namePinyin` TEXT NOT NULL, `description` TEXT NOT NULL, content=`dishes`)");
        final FtsTableInfo _existingDishesFts = FtsTableInfo.read(db, "dishes_fts");
        if (!_infoDishesFts.equals(_existingDishesFts)) {
          return new RoomOpenHelper.ValidationResult(false, "dishes_fts(com.chanle.app.data.database.DishFtsEntity).\n"
                  + " Expected:\n" + _infoDishesFts + "\n"
                  + " Found:\n" + _existingDishesFts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "640a64d0827b74c5b946d2bc45e26067", "9b4be555eeee128c0ee5385c91684f0a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(1);
    _shadowTablesMap.put("dishes_fts", "dishes");
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "dishes","ingredients","steps","tips","tags","aliases","dishes_fts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `dishes`");
      _db.execSQL("DELETE FROM `ingredients`");
      _db.execSQL("DELETE FROM `steps`");
      _db.execSQL("DELETE FROM `tips`");
      _db.execSQL("DELETE FROM `tags`");
      _db.execSQL("DELETE FROM `aliases`");
      _db.execSQL("DELETE FROM `dishes_fts`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DishDao.class, DishDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IngredientDao.class, IngredientDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StepDao.class, StepDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TipDao.class, TipDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TagDao.class, TagDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AliasDao.class, AliasDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DishFtsDao.class, DishFtsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public DishDao dishDao() {
    if (_dishDao != null) {
      return _dishDao;
    } else {
      synchronized(this) {
        if(_dishDao == null) {
          _dishDao = new DishDao_Impl(this);
        }
        return _dishDao;
      }
    }
  }

  @Override
  public IngredientDao ingredientDao() {
    if (_ingredientDao != null) {
      return _ingredientDao;
    } else {
      synchronized(this) {
        if(_ingredientDao == null) {
          _ingredientDao = new IngredientDao_Impl(this);
        }
        return _ingredientDao;
      }
    }
  }

  @Override
  public StepDao stepDao() {
    if (_stepDao != null) {
      return _stepDao;
    } else {
      synchronized(this) {
        if(_stepDao == null) {
          _stepDao = new StepDao_Impl(this);
        }
        return _stepDao;
      }
    }
  }

  @Override
  public TipDao tipDao() {
    if (_tipDao != null) {
      return _tipDao;
    } else {
      synchronized(this) {
        if(_tipDao == null) {
          _tipDao = new TipDao_Impl(this);
        }
        return _tipDao;
      }
    }
  }

  @Override
  public TagDao tagDao() {
    if (_tagDao != null) {
      return _tagDao;
    } else {
      synchronized(this) {
        if(_tagDao == null) {
          _tagDao = new TagDao_Impl(this);
        }
        return _tagDao;
      }
    }
  }

  @Override
  public AliasDao aliasDao() {
    if (_aliasDao != null) {
      return _aliasDao;
    } else {
      synchronized(this) {
        if(_aliasDao == null) {
          _aliasDao = new AliasDao_Impl(this);
        }
        return _aliasDao;
      }
    }
  }

  @Override
  public DishFtsDao dishFtsDao() {
    if (_dishFtsDao != null) {
      return _dishFtsDao;
    } else {
      synchronized(this) {
        if(_dishFtsDao == null) {
          _dishFtsDao = new DishFtsDao_Impl(this);
        }
        return _dishFtsDao;
      }
    }
  }
}
