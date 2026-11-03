package com.chanle.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
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
public final class AliasDao_Impl implements AliasDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AliasEntity> __insertionAdapterOfAliasEntity;

  public AliasDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAliasEntity = new EntityInsertionAdapter<AliasEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `aliases` (`alias`,`dishName`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AliasEntity entity) {
        statement.bindString(1, entity.getAlias());
        statement.bindString(2, entity.getDishName());
      }
    };
  }

  @Override
  public Object insertAlias(final AliasEntity alias, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAliasEntity.insert(alias);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAliases(final List<AliasEntity> aliases,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAliasEntity.insert(aliases);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllAliases(final Continuation<? super List<AliasEntity>> $completion) {
    final String _sql = "SELECT * FROM aliases";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AliasEntity>>() {
      @Override
      @NonNull
      public List<AliasEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "alias");
          final int _cursorIndexOfDishName = CursorUtil.getColumnIndexOrThrow(_cursor, "dishName");
          final List<AliasEntity> _result = new ArrayList<AliasEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AliasEntity _item;
            final String _tmpAlias;
            _tmpAlias = _cursor.getString(_cursorIndexOfAlias);
            final String _tmpDishName;
            _tmpDishName = _cursor.getString(_cursorIndexOfDishName);
            _item = new AliasEntity(_tmpAlias,_tmpDishName);
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
  public Object getAlias(final String alias, final Continuation<? super AliasEntity> $completion) {
    final String _sql = "SELECT * FROM aliases WHERE alias = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, alias);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AliasEntity>() {
      @Override
      @Nullable
      public AliasEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "alias");
          final int _cursorIndexOfDishName = CursorUtil.getColumnIndexOrThrow(_cursor, "dishName");
          final AliasEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpAlias;
            _tmpAlias = _cursor.getString(_cursorIndexOfAlias);
            final String _tmpDishName;
            _tmpDishName = _cursor.getString(_cursorIndexOfDishName);
            _result = new AliasEntity(_tmpAlias,_tmpDishName);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
