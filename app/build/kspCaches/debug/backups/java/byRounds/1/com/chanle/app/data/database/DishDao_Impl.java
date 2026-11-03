package com.chanle.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DishDao_Impl implements DishDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DishEntity> __insertionAdapterOfDishEntity;

  private final EntityDeletionOrUpdateAdapter<DishEntity> __updateAdapterOfDishEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDish;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllDishes;

  public DishDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDishEntity = new EntityInsertionAdapter<DishEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `dishes` (`id`,`name`,`namePinyin`,`category`,`description`,`imageUrl`,`videoUrl`,`videoTitle`,`videoPlatform`,`baseServings`,`timeMinutes`,`difficulty`,`nutritionNote`,`nutritionKcal`,`nutritionProtein`,`nutritionFat`,`nutritionCarbs`,`source`,`createdAt`,`model`,`prompt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DishEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getNamePinyin());
        statement.bindString(4, entity.getCategory());
        statement.bindString(5, entity.getDescription());
        if (entity.getImageUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUrl());
        }
        if (entity.getVideoUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getVideoUrl());
        }
        if (entity.getVideoTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getVideoTitle());
        }
        if (entity.getVideoPlatform() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVideoPlatform());
        }
        statement.bindLong(10, entity.getBaseServings());
        statement.bindLong(11, entity.getTimeMinutes());
        statement.bindString(12, entity.getDifficulty());
        statement.bindString(13, entity.getNutritionNote());
        statement.bindLong(14, entity.getNutritionKcal());
        statement.bindDouble(15, entity.getNutritionProtein());
        statement.bindDouble(16, entity.getNutritionFat());
        statement.bindDouble(17, entity.getNutritionCarbs());
        statement.bindString(18, entity.getSource());
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindString(20, entity.getModel());
        statement.bindString(21, entity.getPrompt());
      }
    };
    this.__updateAdapterOfDishEntity = new EntityDeletionOrUpdateAdapter<DishEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `dishes` SET `id` = ?,`name` = ?,`namePinyin` = ?,`category` = ?,`description` = ?,`imageUrl` = ?,`videoUrl` = ?,`videoTitle` = ?,`videoPlatform` = ?,`baseServings` = ?,`timeMinutes` = ?,`difficulty` = ?,`nutritionNote` = ?,`nutritionKcal` = ?,`nutritionProtein` = ?,`nutritionFat` = ?,`nutritionCarbs` = ?,`source` = ?,`createdAt` = ?,`model` = ?,`prompt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DishEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getNamePinyin());
        statement.bindString(4, entity.getCategory());
        statement.bindString(5, entity.getDescription());
        if (entity.getImageUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUrl());
        }
        if (entity.getVideoUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getVideoUrl());
        }
        if (entity.getVideoTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getVideoTitle());
        }
        if (entity.getVideoPlatform() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVideoPlatform());
        }
        statement.bindLong(10, entity.getBaseServings());
        statement.bindLong(11, entity.getTimeMinutes());
        statement.bindString(12, entity.getDifficulty());
        statement.bindString(13, entity.getNutritionNote());
        statement.bindLong(14, entity.getNutritionKcal());
        statement.bindDouble(15, entity.getNutritionProtein());
        statement.bindDouble(16, entity.getNutritionFat());
        statement.bindDouble(17, entity.getNutritionCarbs());
        statement.bindString(18, entity.getSource());
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindString(20, entity.getModel());
        statement.bindString(21, entity.getPrompt());
        statement.bindString(22, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteDish = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dishes WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllDishes = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dishes";
        return _query;
      }
    };
  }

  @Override
  public Object insertDish(final DishEntity dish, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDishEntity.insert(dish);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDishes(final List<DishEntity> dishes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDishEntity.insert(dishes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDish(final DishEntity dish, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDishEntity.handle(dish);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDish(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDish.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDish.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllDishes(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllDishes.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllDishes.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DishEntity>> getAllDishes() {
    final String _sql = "SELECT * FROM dishes ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dishes"}, new Callable<List<DishEntity>>() {
      @Override
      @NonNull
      public List<DishEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final List<DishEntity> _result = new ArrayList<DishEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DishEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _item = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllDishesList(final Continuation<? super List<DishEntity>> $completion) {
    final String _sql = "SELECT * FROM dishes ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DishEntity>>() {
      @Override
      @NonNull
      public List<DishEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final List<DishEntity> _result = new ArrayList<DishEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DishEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _item = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDishById(final String id, final Continuation<? super DishEntity> $completion) {
    final String _sql = "SELECT * FROM dishes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DishEntity>() {
      @Override
      @Nullable
      public DishEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final DishEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _result = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDishByName(final String name,
      final Continuation<? super DishEntity> $completion) {
    final String _sql = "SELECT * FROM dishes WHERE name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DishEntity>() {
      @Override
      @Nullable
      public DishEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final DishEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _result = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DishEntity>> getDishesByCategory(final String category) {
    final String _sql = "SELECT * FROM dishes WHERE category = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dishes"}, new Callable<List<DishEntity>>() {
      @Override
      @NonNull
      public List<DishEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final List<DishEntity> _result = new ArrayList<DishEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DishEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _item = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDishesByIds(final List<String> ids,
      final Continuation<? super List<DishEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM dishes WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : ids) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DishEntity>>() {
      @Override
      @NonNull
      public List<DishEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNamePinyin = CursorUtil.getColumnIndexOrThrow(_cursor, "namePinyin");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "videoUrl");
          final int _cursorIndexOfVideoTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "videoTitle");
          final int _cursorIndexOfVideoPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "videoPlatform");
          final int _cursorIndexOfBaseServings = CursorUtil.getColumnIndexOrThrow(_cursor, "baseServings");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfNutritionNote = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionNote");
          final int _cursorIndexOfNutritionKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionKcal");
          final int _cursorIndexOfNutritionProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionProtein");
          final int _cursorIndexOfNutritionFat = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionFat");
          final int _cursorIndexOfNutritionCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "nutritionCarbs");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final List<DishEntity> _result = new ArrayList<DishEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DishEntity _item_1;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNamePinyin;
            _tmpNamePinyin = _cursor.getString(_cursorIndexOfNamePinyin);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpVideoTitle;
            if (_cursor.isNull(_cursorIndexOfVideoTitle)) {
              _tmpVideoTitle = null;
            } else {
              _tmpVideoTitle = _cursor.getString(_cursorIndexOfVideoTitle);
            }
            final String _tmpVideoPlatform;
            if (_cursor.isNull(_cursorIndexOfVideoPlatform)) {
              _tmpVideoPlatform = null;
            } else {
              _tmpVideoPlatform = _cursor.getString(_cursorIndexOfVideoPlatform);
            }
            final int _tmpBaseServings;
            _tmpBaseServings = _cursor.getInt(_cursorIndexOfBaseServings);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpNutritionNote;
            _tmpNutritionNote = _cursor.getString(_cursorIndexOfNutritionNote);
            final int _tmpNutritionKcal;
            _tmpNutritionKcal = _cursor.getInt(_cursorIndexOfNutritionKcal);
            final double _tmpNutritionProtein;
            _tmpNutritionProtein = _cursor.getDouble(_cursorIndexOfNutritionProtein);
            final double _tmpNutritionFat;
            _tmpNutritionFat = _cursor.getDouble(_cursorIndexOfNutritionFat);
            final double _tmpNutritionCarbs;
            _tmpNutritionCarbs = _cursor.getDouble(_cursorIndexOfNutritionCarbs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            _item_1 = new DishEntity(_tmpId,_tmpName,_tmpNamePinyin,_tmpCategory,_tmpDescription,_tmpImageUrl,_tmpVideoUrl,_tmpVideoTitle,_tmpVideoPlatform,_tmpBaseServings,_tmpTimeMinutes,_tmpDifficulty,_tmpNutritionNote,_tmpNutritionKcal,_tmpNutritionProtein,_tmpNutritionFat,_tmpNutritionCarbs,_tmpSource,_tmpCreatedAt,_tmpModel,_tmpPrompt);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDishCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dishes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getDishCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM dishes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dishes"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
