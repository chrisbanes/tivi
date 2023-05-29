// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0
// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0
// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0
// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "app.tivi.common.ui"
}

dependencies {
    api(projects.data.models)
    api(projects.common.ui.resources)
    api(projects.core.logging)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.kotlin.coroutines.core)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.emoji)
}
