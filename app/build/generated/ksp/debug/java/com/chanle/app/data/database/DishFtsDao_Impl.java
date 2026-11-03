package com.chanle.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DishFtsDao_Impl implements DishFtsDao {
  private final RoomDatabase __db;

  public DishFtsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public Object searchDishes(final String query,
      final Continuation<? super List<DishEntity>> $completion) {
    final String _sql = "SELECT dishes.* FROM dishes JOIN dishes_fts ON dishes.id = dishes_fts.rowid WHERE dishes_fts MATCH ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
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
  public Object countSearchResults(final String query,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dishes_fts WHERE dishes_fts MATCH ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
