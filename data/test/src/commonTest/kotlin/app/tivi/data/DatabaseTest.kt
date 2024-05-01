// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import com.benasher44.uuid.uuid4
import kotlin.test.AfterTest

internal expect fun createTestSqlDriver(name: String): SqlDriver

abstract class DatabaseTest {
  private val sqlDriver: SqlDriver by lazy {
    createTestSqlDriver("${this@DatabaseTest::class.simpleName}_${uuid4()}")
  }

  protected val database: Database by lazy { DatabaseFactory(sqlDriver).build() }

  @AfterTest
  fun closeDatabase() {
    sqlDriver.close()
  }
}
