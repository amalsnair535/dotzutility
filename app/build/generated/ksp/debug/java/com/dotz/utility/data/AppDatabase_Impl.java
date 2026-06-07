package com.dotz.utility.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.dotz.utility.data.calendar.CalendarEventDao;
import com.dotz.utility.data.calendar.CalendarEventDao_Impl;
import com.dotz.utility.data.clock.AlarmDao;
import com.dotz.utility.data.clock.AlarmDao_Impl;
import com.dotz.utility.data.notes.NoteDao;
import com.dotz.utility.data.notes.NoteDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile NoteDao _noteDao;

  private volatile CalendarEventDao _calendarEventDao;

  private volatile AlarmDao _alarmDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `isPinned` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `calendar_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epochDay` INTEGER NOT NULL, `title` TEXT NOT NULL, `note` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `label` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0c13f9b9f7fcd3301bfc7b5254cbb9fd')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `notes`");
        db.execSQL("DROP TABLE IF EXISTS `calendar_events`");
        db.execSQL("DROP TABLE IF EXISTS `alarms`");
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
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsNotes = new HashMap<String, TableInfo.Column>(6);
        _columnsNotes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("isPinned", new TableInfo.Column("isPinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotes = new TableInfo("notes", _columnsNotes, _foreignKeysNotes, _indicesNotes);
        final TableInfo _existingNotes = TableInfo.read(db, "notes");
        if (!_infoNotes.equals(_existingNotes)) {
          return new RoomOpenHelper.ValidationResult(false, "notes(com.dotz.utility.data.notes.NoteEntity).\n"
                  + " Expected:\n" + _infoNotes + "\n"
                  + " Found:\n" + _existingNotes);
        }
        final HashMap<String, TableInfo.Column> _columnsCalendarEvents = new HashMap<String, TableInfo.Column>(5);
        _columnsCalendarEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("epochDay", new TableInfo.Column("epochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarEvents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCalendarEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCalendarEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCalendarEvents = new TableInfo("calendar_events", _columnsCalendarEvents, _foreignKeysCalendarEvents, _indicesCalendarEvents);
        final TableInfo _existingCalendarEvents = TableInfo.read(db, "calendar_events");
        if (!_infoCalendarEvents.equals(_existingCalendarEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "calendar_events(com.dotz.utility.data.calendar.CalendarEvent).\n"
                  + " Expected:\n" + _infoCalendarEvents + "\n"
                  + " Found:\n" + _existingCalendarEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsAlarms = new HashMap<String, TableInfo.Column>(5);
        _columnsAlarms.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarms.put("hour", new TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarms.put("minute", new TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarms.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarms.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlarms = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlarms = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlarms = new TableInfo("alarms", _columnsAlarms, _foreignKeysAlarms, _indicesAlarms);
        final TableInfo _existingAlarms = TableInfo.read(db, "alarms");
        if (!_infoAlarms.equals(_existingAlarms)) {
          return new RoomOpenHelper.ValidationResult(false, "alarms(com.dotz.utility.data.clock.AlarmEntity).\n"
                  + " Expected:\n" + _infoAlarms + "\n"
                  + " Found:\n" + _existingAlarms);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0c13f9b9f7fcd3301bfc7b5254cbb9fd", "97d9ef38daa7ca43c0eced2fb1def771");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "notes","calendar_events","alarms");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `notes`");
      _db.execSQL("DELETE FROM `calendar_events`");
      _db.execSQL("DELETE FROM `alarms`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
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
    _typeConvertersMap.put(NoteDao.class, NoteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CalendarEventDao.class, CalendarEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlarmDao.class, AlarmDao_Impl.getRequiredConverters());
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
  public NoteDao noteDao() {
    if (_noteDao != null) {
      return _noteDao;
    } else {
      synchronized(this) {
        if(_noteDao == null) {
          _noteDao = new NoteDao_Impl(this);
        }
        return _noteDao;
      }
    }
  }

  @Override
  public CalendarEventDao calendarEventDao() {
    if (_calendarEventDao != null) {
      return _calendarEventDao;
    } else {
      synchronized(this) {
        if(_calendarEventDao == null) {
          _calendarEventDao = new CalendarEventDao_Impl(this);
        }
        return _calendarEventDao;
      }
    }
  }

  @Override
  public AlarmDao alarmDao() {
    if (_alarmDao != null) {
      return _alarmDao;
    } else {
      synchronized(this) {
        if(_alarmDao == null) {
          _alarmDao = new AlarmDao_Impl(this);
        }
        return _alarmDao;
      }
    }
  }
}
