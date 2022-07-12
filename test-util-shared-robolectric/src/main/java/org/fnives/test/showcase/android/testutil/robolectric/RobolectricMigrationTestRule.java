package org.fnives.test.showcase.android.testutil.robolectric;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.room.AutoMigration;
import androidx.room.DatabaseConfiguration;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.migration.bundle.DatabaseBundle;
import androidx.room.migration.bundle.DatabaseViewBundle;
import androidx.room.migration.bundle.EntityBundle;
import androidx.room.migration.bundle.FieldBundle;
import androidx.room.migration.bundle.ForeignKeyBundle;
import androidx.room.migration.bundle.FtsEntityBundle;
import androidx.room.migration.bundle.IndexBundle;
import androidx.room.migration.bundle.SchemaBundle;
import androidx.room.util.FtsTableInfo;
import androidx.room.util.TableInfo;
import androidx.room.util.ViewInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;

import org.fnives.test.showcase.android.testutil.SharedMigrationTestRule;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copy of {@link androidx.room.testing.MigrationTestHelper MigrationTestHelper} except it uses resources folder instead of assets.
 *
 * This is needed since in unit tests we cannot add to the asset folder easily, without polluting the apk.
 *
 * reference: https://github.com/robolectric/robolectric/issues/2065
 */
@SuppressLint("RestrictedApi")
public class RobolectricMigrationTestRule extends TestWatcher implements SharedMigrationTestRule {
    private static final String TAG = "RobolectricMigrationTR";
    private final String mAssetsFolder;
    private final SupportSQLiteOpenHelper.Factory mOpenFactory;
    private List<WeakReference<SupportSQLiteDatabase>> mManagedDatabases = new ArrayList<>();
    private List<WeakReference<RoomDatabase>> mManagedRoomDatabases = new ArrayList<>();
    private boolean mTestStarted;
    private Instrumentation mInstrumentation;
    @Nullable
    private List<AutoMigrationSpec> mSpecs;
    @Nullable
    private Class<? extends RoomDatabase> mDatabaseClass;


    /**
     * Creates a new migration helper. It uses the Instrumentation context to load the schema
     * (falls back to the app resources) and the target context to create the database.
     *
     * @param instrumentation The instrumentation instance.
     * @param databaseClass   The Database class to be tested.
     */
    public RobolectricMigrationTestRule(@NonNull Instrumentation instrumentation,
                                          @NonNull Class<? extends RoomDatabase> databaseClass) {
        this(instrumentation, databaseClass, new ArrayList<>(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    /**
     * Creates a new migration helper. It uses the Instrumentation context to load the schema
     * (falls back to the app resources) and the target context to create the database.
     * <p>
     * An instance of a class annotated with {@link androidx.room.ProvidedAutoMigrationSpec} has
     * to be provided to Room using this constructor. MigrationTestHelper will map auto migration
     * spec classes to their provided instances before running and validatingt the Migrations.
     *
     * @param instrumentation The instrumentation instance.
     * @param databaseClass   The Database class to be tested.
     * @param specs           The list of available auto migration specs that will be provided to
     *                        Room at runtime.
     */
    public RobolectricMigrationTestRule(@NonNull Instrumentation instrumentation,
                                          @NonNull Class<? extends RoomDatabase> databaseClass,
                                          @NonNull List<AutoMigrationSpec> specs) {
        this(instrumentation, databaseClass, specs, new FrameworkSQLiteOpenHelperFactory());
    }

    /**
     * Creates a new migration helper. It uses the Instrumentation context to load the schema
     * (falls back to the app resources) and the target context to create the database.
     * <p>
     * An instance of a class annotated with {@link androidx.room.ProvidedAutoMigrationSpec} has
     * to be provided to Room using this constructor. MigrationTestHelper will map auto migration
     * spec classes to their provided instances before running and validatingt the Migrations.
     *
     * @param instrumentation The instrumentation instance.
     * @param databaseClass   The Database class to be tested.
     * @param specs           The list of available auto migration specs that will be provided to
     *                        Room at runtime.
     * @param openFactory     Factory class that allows creation of {@link SupportSQLiteOpenHelper}
     */
    public RobolectricMigrationTestRule(@NonNull Instrumentation instrumentation,
                                          @NonNull Class<? extends RoomDatabase> databaseClass,
                                          @NonNull List<AutoMigrationSpec> specs,
                                          @NonNull SupportSQLiteOpenHelper.Factory openFactory
    ) {
        String assetsFolder = databaseClass.getCanonicalName();
        mInstrumentation = instrumentation;
        if (assetsFolder.endsWith("/")) {
            assetsFolder = assetsFolder.substring(0, assetsFolder.length() - 1);
        }
        mAssetsFolder = assetsFolder;
        mOpenFactory = openFactory;
        mDatabaseClass = databaseClass;
        mSpecs = specs;
    }

    @Override
    protected void starting(Description description) {
        super.starting(description);
        mTestStarted = true;
    }

    /**
     * Creates the database in the given version.
     * If the database file already exists, it tries to delete it first. If delete fails, throws
     * an exception.
     *
     * @param name    The name of the database.
     * @param version The version in which the database should be created.
     * @return A database connection which has the schema in the requested version.
     * @throws IOException If it cannot find the schema description in the assets folder.
     */
    @SuppressLint("RestrictedApi")
    @SuppressWarnings("SameParameterValue")
    @Override
    @NotNull
    public SupportSQLiteDatabase createDatabase(@NotNull String name, int version) throws IOException {
        File dbPath = mInstrumentation.getTargetContext().getDatabasePath(name);
        if (dbPath.exists()) {
            Log.d(TAG, "deleting database file " + name);
            if (!dbPath.delete()) {
                throw new IllegalStateException("There is a database file and I could not delete"
                        + " it. Make sure you don't have any open connections to that database"
                        + " before calling this method.");
            }
        }
        SchemaBundle schemaBundle = loadSchema(version);
        RoomDatabase.MigrationContainer container = new RoomDatabase.MigrationContainer();
        DatabaseConfiguration configuration = new DatabaseConfiguration(
                mInstrumentation.getTargetContext(),
                name,
                mOpenFactory,
                container,
                null,
                true,
                RoomDatabase.JournalMode.TRUNCATE,
                ArchTaskExecutor.getIOThreadExecutor(),
                ArchTaskExecutor.getIOThreadExecutor(),
                null,
                true,
                false,
                Collections.<Integer>emptySet(),
                null,
                null,
                null,
                null,
                null,
                null);
        RoomOpenHelper roomOpenHelper = new RoomOpenHelper(configuration,
                new CreatingDelegate(schemaBundle.getDatabase()),
                schemaBundle.getDatabase().getIdentityHash(),
                // we pass the same hash twice since an old schema does not necessarily have
                // a legacy hash and we would not even persist it.
                schemaBundle.getDatabase().getIdentityHash());
        return openDatabase(name, roomOpenHelper);
    }

    /**
     * Runs the given set of migrations on the provided database.
     * <p>
     * It uses the same algorithm that Room uses to choose migrations so the migrations instances
     * that are provided to this method must be sufficient to bring the database from current
     * version to the desired version.
     * <p>
     * After the migration, the method validates the database schema to ensure that migration
     * result matches the expected schema. Handling of dropped tables depends on the
     * {@code validateDroppedTables} argument. If set to true, the verification will fail if it
     * finds a table that is not registered in the Database. If set to false, extra tables in the
     * database will be ignored (this is the runtime library behavior).
     *
     * @param name                  The database name. You must first create this database via
     *                              {@link #createDatabase(String, int)}.
     * @param version               The final version after applying the migrations.
     * @param validateDroppedTables If set to true, validation will fail if the database has
     *                              unknown
     *                              tables.
     * @param migrations            The list of available migrations.
     * @throws IOException           If it cannot find the schema for {@code toVersion}.
     * @throws IllegalStateException If the schema validation fails.
     */
    @SuppressLint("RestrictedApi")
    @Override
    @NotNull
    public SupportSQLiteDatabase runMigrationsAndValidate(@NotNull String name,
                                                          int version,
                                                          boolean validateDroppedTables,
                                                          @NotNull
                                                                  Migration... migrations) throws IOException {
        File dbPath = mInstrumentation.getTargetContext().getDatabasePath(name);
        if (!dbPath.exists()) {
            throw new IllegalStateException("Cannot find the database file for " + name + ". "
                    + "Before calling runMigrations, you must first create the database via "
                    + "createDatabase.");
        }
        SchemaBundle schemaBundle = loadSchema(version);
        RoomDatabase.MigrationContainer container = new RoomDatabase.MigrationContainer();
        container.addMigrations(getAutoMigrations(mSpecs));
        container.addMigrations(migrations);
        DatabaseConfiguration configuration = new DatabaseConfiguration(
                mInstrumentation.getTargetContext(),
                name,
                mOpenFactory,
                container,
                null,
                true,
                RoomDatabase.JournalMode.TRUNCATE,
                ArchTaskExecutor.getIOThreadExecutor(),
                ArchTaskExecutor.getIOThreadExecutor(),
                null,
                true,
                false,
                Collections.<Integer>emptySet(),
                null,
                null,
                null,
                null,
                null,
                null);
        RoomOpenHelper roomOpenHelper = new RoomOpenHelper(configuration,
                new MigratingDelegate(schemaBundle.getDatabase(), validateDroppedTables),
                // we pass the same hash twice since an old schema does not necessarily have
                // a legacy hash and we would not even persist it.
                schemaBundle.getDatabase().getIdentityHash(),
                schemaBundle.getDatabase().getIdentityHash());
        return openDatabase(name, roomOpenHelper);
    }

    /**
     * Returns a list of {@link Migration} of a database that has been generated using
     * {@link AutoMigration}.
     */
    @NonNull
    private List<Migration> getAutoMigrations(List<AutoMigrationSpec> userProvidedSpecs) {
        if (mDatabaseClass == null) {
            if (userProvidedSpecs.isEmpty()) {
                // TODO: Detect that there are auto migrations to test when a deprecated
                //  constructor is used.
                Log.e(TAG, "If you have any AutoMigrations in your implementation, you must use "
                        + "a non-deprecated MigrationTestHelper constructor to provide the "
                        + "Database class in order to test them. If you do not have any "
                        + "AutoMigrations to test, you may ignore this warning.");
                return new ArrayList<>();
            } else {
                throw new IllegalStateException("You must provide the database class in the "
                        + "MigrationTestHelper constructor in order to test auto migrations.");
            }
        }

        RoomDatabase db = Room.getGeneratedImplementation(mDatabaseClass, "_Impl");
        Set<Class<? extends AutoMigrationSpec>> requiredAutoMigrationSpecs =
                db.getRequiredAutoMigrationSpecs();
        return db.getAutoMigrations(
                createAutoMigrationSpecMap(requiredAutoMigrationSpecs, userProvidedSpecs)
        );
    }

    /**
     * Maps auto migration spec classes to their provided instance.
     */
    private Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> createAutoMigrationSpecMap(
            Set<Class<? extends AutoMigrationSpec>> requiredAutoMigrationSpecs,
            List<AutoMigrationSpec> userProvidedSpecs) {
        Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> specMap = new HashMap<>();
        if (requiredAutoMigrationSpecs.isEmpty()) {
            return specMap;
        }

        if (userProvidedSpecs == null) {
            throw new IllegalStateException(
                    "You must provide all required auto migration specs in the "
                            + "MigrationTestHelper constructor."
            );
        }

        for (Class<? extends AutoMigrationSpec> spec : requiredAutoMigrationSpecs) {
            boolean found = false;
            AutoMigrationSpec match = null;
            for (AutoMigrationSpec provided : userProvidedSpecs) {
                if (spec.isAssignableFrom(provided.getClass())) {
                    found = true;
                    match = provided;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(
                        "A required auto migration spec (" + spec.getCanonicalName() + ") has not"
                                + " been provided."
                );
            }
            specMap.put(spec, match);
        }
        return specMap;
    }


    private SupportSQLiteDatabase openDatabase(String name, RoomOpenHelper roomOpenHelper) {
        SupportSQLiteOpenHelper.Configuration config =
                SupportSQLiteOpenHelper.Configuration
                        .builder(mInstrumentation.getTargetContext())
                        .callback(roomOpenHelper)
                        .name(name)
                        .build();
        SupportSQLiteDatabase db = mOpenFactory.create(config).getWritableDatabase();
        mManagedDatabases.add(new WeakReference<>(db));
        return db;
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        for (WeakReference<SupportSQLiteDatabase> dbRef : mManagedDatabases) {
            SupportSQLiteDatabase db = dbRef.get();
            if (db != null && db.isOpen()) {
                try {
                    db.close();
                } catch (Throwable ignored) {
                }
            }
        }
        for (WeakReference<RoomDatabase> dbRef : mManagedRoomDatabases) {
            final RoomDatabase roomDatabase = dbRef.get();
            if (roomDatabase != null) {
                roomDatabase.close();
            }
        }
    }

    /**
     * Registers a database connection to be automatically closed when the test finishes.
     * <p>
     * This only works if {@code MigrationTestHelper} is registered as a Junit test rule via
     * {@link org.junit.Rule Rule} annotation.
     *
     * @param db The database connection that should be closed after the test finishes.
     */
    @Override
    public void closeWhenFinished(@NotNull SupportSQLiteDatabase db) {
        if (!mTestStarted) {
            throw new IllegalStateException("You cannot register a database to be closed before"
                    + " the test starts. Maybe you forgot to annotate MigrationTestHelper as a"
                    + " test rule? (@Rule)");
        }
        mManagedDatabases.add(new WeakReference<>(db));
    }

    /**
     * Registers a database connection to be automatically closed when the test finishes.
     * <p>
     * This only works if {@code MigrationTestHelper} is registered as a Junit test rule via
     * {@link org.junit.Rule Rule} annotation.
     *
     * @param db The RoomDatabase instance which holds the database.
     */
    @Override
    public void closeWhenFinished(@NotNull RoomDatabase db) {
        if (!mTestStarted) {
            throw new IllegalStateException("You cannot register a database to be closed before"
                    + " the test starts. Maybe you forgot to annotate MigrationTestHelper as a"
                    + " test rule? (@Rule)");
        }
        mManagedRoomDatabases.add(new WeakReference<>(db));
    }

    private SchemaBundle loadSchema(int version) throws IOException {
        try {
            return loadSchema(mInstrumentation.getContext(), version);
        } catch (FileNotFoundException testAssetsIOExceptions) {
            Log.w(TAG, "Could not find the schema file in the test assets. Checking the"
                    + " application assets");
            try {
                return loadSchema(mInstrumentation.getTargetContext(), version);
            } catch (FileNotFoundException appAssetsException) {
                // throw the test assets exception instead
                throw new FileNotFoundException("Cannot find the schema file in the assets folder. "
                        + "Make sure to include the exported json schemas in your test assert "
                        + "inputs. See "
                        + "https://developer.android.com/training/data-storage/room/"
                        + "migrating-db-versions#export-schema for details. Missing file: "
                        + testAssetsIOExceptions.getMessage());
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private SchemaBundle loadSchema(Context context, int version) throws IOException {
        String fileName = mAssetsFolder + "/" + version + ".json";
        try {
            InputStream input = getClass().getClassLoader()
                    .getResourceAsStream(fileName);
            return SchemaBundle.deserialize(input);
        } catch (NullPointerException nullPointerException) {
            throw new IOException("File not found: " + fileName, nullPointerException);
        }
//        InputStream input = context.getAssets().open(mAssetsFolder + "/" + version + ".json");
//        return SchemaBundle.deserialize(input);
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static TableInfo toTableInfo(EntityBundle entityBundle) {
        return new TableInfo(entityBundle.getTableName(), toColumnMap(entityBundle),
                toForeignKeys(entityBundle.getForeignKeys()), toIndices(entityBundle.getIndices()));
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static FtsTableInfo toFtsTableInfo(FtsEntityBundle ftsEntityBundle) {
        return new FtsTableInfo(ftsEntityBundle.getTableName(), toColumnNamesSet(ftsEntityBundle),
                ftsEntityBundle.getCreateSql());
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static ViewInfo toViewInfo(DatabaseViewBundle viewBundle) {
        return new ViewInfo(viewBundle.getViewName(), viewBundle.createView());
    }

    private static Set<TableInfo.Index> toIndices(List<IndexBundle> indices) {
        if (indices == null) {
            return Collections.emptySet();
        }
        Set<TableInfo.Index> result = new HashSet<>();
        for (IndexBundle bundle : indices) {
            result.add(new TableInfo.Index(bundle.getName(), bundle.isUnique(),
                    bundle.getColumnNames(), bundle.getOrders()));
        }
        return result;
    }

    private static Set<TableInfo.ForeignKey> toForeignKeys(
            List<ForeignKeyBundle> bundles) {
        if (bundles == null) {
            return Collections.emptySet();
        }
        Set<TableInfo.ForeignKey> result = new HashSet<>(bundles.size());
        for (ForeignKeyBundle bundle : bundles) {
            result.add(new TableInfo.ForeignKey(bundle.getTable(),
                    bundle.getOnDelete(), bundle.getOnUpdate(),
                    bundle.getColumns(), bundle.getReferencedColumns()));
        }
        return result;
    }

    private static Set<String> toColumnNamesSet(EntityBundle entity) {
        Set<String> result = new HashSet<>();
        for (FieldBundle field : entity.getFields()) {
            result.add(field.getColumnName());
        }
        return result;
    }

    private static Map<String, TableInfo.Column> toColumnMap(EntityBundle entity) {
        Map<String, TableInfo.Column> result = new HashMap<>();
        for (FieldBundle bundle : entity.getFields()) {
            TableInfo.Column column = toColumn(entity, bundle);
            result.put(column.name, column);
        }
        return result;
    }

    private static TableInfo.Column toColumn(EntityBundle entity, FieldBundle field) {
        return new TableInfo.Column(field.getColumnName(), field.getAffinity(),
                field.isNonNull(), findPrimaryKeyPosition(entity, field), field.getDefaultValue(),
                TableInfo.CREATED_FROM_ENTITY);
    }

    private static int findPrimaryKeyPosition(EntityBundle entity, FieldBundle field) {
        List<String> columnNames = entity.getPrimaryKey().getColumnNames();
        int i = 0;
        for (String columnName : columnNames) {
            i++;
            if (field.getColumnName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return 0;
    }

    static class MigratingDelegate extends RoomOpenHelperDelegate {
        private final boolean mVerifyDroppedTables;

        MigratingDelegate(DatabaseBundle databaseBundle, boolean verifyDroppedTables) {
            super(databaseBundle);
            mVerifyDroppedTables = verifyDroppedTables;
        }

        @Override
        protected void createAllTables(SupportSQLiteDatabase database) {
            throw new UnsupportedOperationException("Was expecting to migrate but received create."
                    + "Make sure you have created the database first.");
        }

        @NonNull
        @Override
        protected RoomOpenHelper.ValidationResult onValidateSchema(
                @NonNull SupportSQLiteDatabase db) {
            final Map<String, EntityBundle> tables = mDatabaseBundle.getEntitiesByTableName();
            for (EntityBundle entity : tables.values()) {
                if (entity instanceof FtsEntityBundle) {
                    final FtsTableInfo expected = toFtsTableInfo((FtsEntityBundle) entity);
                    final FtsTableInfo found = FtsTableInfo.read(db, entity.getTableName());
                    if (!expected.equals(found)) {
                        return new ValidationResult(false, expected.name
                                + "\nExpected: " + expected + "\nFound: " + found);
                    }
                } else {
                    final TableInfo expected = toTableInfo(entity);
                    final TableInfo found = TableInfo.read(db, entity.getTableName());
                    if (!expected.equals(found)) {
                        return new ValidationResult(false, expected.name
                                + "\nExpected: " + expected + " \nfound: " + found);
                    }
                }
            }
            for (DatabaseViewBundle view : mDatabaseBundle.getViews()) {
                final ViewInfo expected = toViewInfo(view);
                final ViewInfo found = ViewInfo.read(db, view.getViewName());
                if (!expected.equals(found)) {
                    return new ValidationResult(false, expected
                            + "\nExpected: " + expected + " \nfound: " + found);
                }
            }
            if (mVerifyDroppedTables) {
                // now ensure tables that should be removed are removed.
                Set<String> expectedTables = new HashSet<>();
                for (EntityBundle entity : tables.values()) {
                    expectedTables.add(entity.getTableName());
                    if (entity instanceof FtsEntityBundle) {
                        expectedTables.addAll(((FtsEntityBundle) entity).getShadowTableNames());
                    }
                }
                Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'"
                                + " AND name NOT IN(?, ?, ?)",
                        new String[]{Room.MASTER_TABLE_NAME, "android_metadata",
                                "sqlite_sequence"});
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    while (cursor.moveToNext()) {
                        final String tableName = cursor.getString(0);
                        if (!expectedTables.contains(tableName)) {
                            return new ValidationResult(false, "Unexpected table "
                                    + tableName);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
            return new ValidationResult(true, null);
        }
    }

    static class CreatingDelegate extends RoomOpenHelperDelegate {

        CreatingDelegate(DatabaseBundle databaseBundle) {
            super(databaseBundle);
        }

        @Override
        protected void createAllTables(SupportSQLiteDatabase database) {
            for (String query : mDatabaseBundle.buildCreateQueries()) {
                database.execSQL(query);
            }
        }

        @NonNull
        @Override
        protected RoomOpenHelper.ValidationResult onValidateSchema(
                @NonNull SupportSQLiteDatabase db) {
            throw new UnsupportedOperationException("This open helper just creates the database but"
                    + " it received a migration request.");
        }
    }

    abstract static class RoomOpenHelperDelegate extends RoomOpenHelper.Delegate {
        final DatabaseBundle mDatabaseBundle;

        RoomOpenHelperDelegate(DatabaseBundle databaseBundle) {
            super(databaseBundle.getVersion());
            mDatabaseBundle = databaseBundle;
        }

        @Override
        protected void dropAllTables(SupportSQLiteDatabase database) {
            throw new UnsupportedOperationException("cannot drop all tables in the test");
        }

        @Override
        protected void onCreate(SupportSQLiteDatabase database) {
        }

        @Override
        protected void onOpen(SupportSQLiteDatabase database) {
        }
    }
}
