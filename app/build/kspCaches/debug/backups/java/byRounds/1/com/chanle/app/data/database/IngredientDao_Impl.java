package com.chanle.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class IngredientDao_Impl implements IngredientDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<IngredientEntity> __insertionAdapterOfIngredientEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteIngredientsByDishId;

  public IngredientDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIngredientEntity = new EntityInsertionAdapter<IngredientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ingredients` (`id`,`dishId`,`name`,`amount`,`unit`,`note`,`scalable`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IngredientEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDishId());
        statement.bindString(3, entity.getName());
        if (entity.getAmount() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getAmount());
        }
        statement.bindString(5, entity.getUnit());
        statement.bindString(6, entity.getNote());
        final int _tmp = entity.getScalable() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__preparedStmtOfDeleteIngredientsByDishId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ingredients WHERE dishId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertIngredient(final IngredientEntity ingredient,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIngredientEntity.insert(ingredient);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertIngredients(final List<IngredientEntity> ingredients,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIngredientEntity.insert(ingredients);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteIngredientsByDishId(final String dishId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteIngredientsByDishId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, dishId);
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
          __preparedStmtOfDeleteIngredientsByDishId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getIngredientsByDishId(final String dishId,
      final Continuation<? super List<IngredientEntity>> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE dishId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dishId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<IngredientEntity>>() {
      @Override
      @NonNull
      public List<IngredientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDishId = CursorUtil.getColumnIndexOrThrow(_cursor, "dishId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfScalable = CursorUtil.getColumnIndexOrThrow(_cursor, "scalable");
          final List<IngredientEntity> _result = new ArrayList<IngredientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IngredientEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDishId;
            _tmpDishId = _cursor.getString(_cursorIndexOfDishId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Double _tmpAmount;
            if (_cursor.isNull(_cursorIndexOfAmount)) {
              _tmpAmount = null;
            } else {
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpScalable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfScalable);
            _tmpScalable = _tmp != 0;
            _item = new IngredientEntity(_tmpId,_tmpDishId,_tmpName,_tmpAmount,_tmpUnit,_tmpNote,_tmpScalable);
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
  public Object getAllIngredientNames(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT name FROM ingredients ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
  public Object searchIngredients(final String name,
      final Continuation<? super List<IngredientEntity>> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE name LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<IngredientEntity>>() {
      @Override
      @NonNull
      public List<IngredientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDishId = CursorUtil.getColumnIndexOrThrow(_cursor, "dishId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfScalable = CursorUtil.getColumnIndexOrThrow(_cursor, "scalable");
          final List<IngredientEntity> _result = new ArrayList<IngredientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IngredientEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDishId;
            _tmpDishId = _cursor.getString(_cursorIndexOfDishId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Double _tmpAmount;
            if (_cursor.isNull(_cursorIndexOfAmount)) {
              _tmpAmount = null;
            } else {
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpScalable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfScalable);
            _tmpScalable = _tmp != 0;
            _item = new IngredientEntity(_tmpId,_tmpDishId,_tmpName,_tmpAmount,_tmpUnit,_tmpNote,_tmpScalable);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
