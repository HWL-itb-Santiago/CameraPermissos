package cat.itb.m78.exercices

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import cat.itb.m78.exercices.db.Database

actual fun createDriver(): SqlDriver {
    val appContext = applicationContext
    return AndroidSqliteDriver(
        schema = Database.Schema,
        context = appContext,
        name = "markers.db"
    )
    }

//
//private fun SqlDriver.getVersion(): Long {
//    return executeQuery(
//        query = "PRAGMA user_version",
//        mapper = { cursor ->
//            if (cursor.next().value) cursor.getLong(0) else 0L
//        },
//        parameters = 0
//    )
//}
//
//private fun SqlDriver.setVersion(version: Long) {
//    execute(
//        query = "PRAGMA user_version = ?",
//        parameters = 1
//    ) {
//        bindLong(1, version)
//    }
//}

val database by lazy { createDatabase() }