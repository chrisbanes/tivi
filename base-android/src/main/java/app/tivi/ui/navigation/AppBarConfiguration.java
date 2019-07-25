/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.ui.navigation;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration options for {@link NavigationUI} methods that interact with implementations of the
 * app bar pattern such as {@link androidx.appcompat.widget.Toolbar},
 * {@link com.google.android.material.appbar.CollapsingToolbarLayout}, and
 * {@link androidx.appcompat.app.ActionBar}.
 */
public final class AppBarConfiguration {
    /**
     * Interface for providing custom 'up' behavior beyond what is provided by
     * {@link androidx.navigation.NavController#navigateUp()}.
     *
     * @see Builder#setFallbackOnNavigateUpListener(OnNavigateUpListener)
     * @see NavigationUI#navigateUp(androidx.navigation.NavController, AppBarConfiguration)
     */
    public interface OnNavigateUpListener {
        /**
         * Callback for handling the Up button.
         *
         * @return true if the listener successfully navigated 'up'
         */
        boolean onNavigateUp();
    }

    @NonNull
    private final Set<Integer> mTopLevelDestinations;
    @Nullable
    private final NavigationView mNavigationView;
    @Nullable
    private final OnNavigateUpListener mFallbackOnNavigateUpListener;

    private AppBarConfiguration(@NonNull Set<Integer> topLevelDestinations,
                                @Nullable NavigationView navigationView,
                                @Nullable OnNavigateUpListener fallbackOnNavigateUpListener) {
        mTopLevelDestinations = topLevelDestinations;
        mNavigationView = navigationView;
        mFallbackOnNavigateUpListener = fallbackOnNavigateUpListener;
    }

    /**
     * The set of destinations by id considered at the top level of your information hierarchy.
     * The Up button will not be displayed when on these destinations.
     *
     * @return The set of top level destinations by id.
     */
    @NonNull
    public Set<Integer> getTopLevelDestinations() {
        return mTopLevelDestinations;
    }

    /**
     * The {@link NavigationView} indicating that the Navigation button should be displayed as
     * a drawer symbol when it is not being shown as an Up button.
     *
     * @return The NavigationView that should be toggled from the Navigation button
     */
    @Nullable
    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    /**
     * The {@link OnNavigateUpListener} that should be invoked if
     * {@link androidx.navigation.NavController#navigateUp} returns <code>false</code>.
     *
     * @return a {@link OnNavigateUpListener} for providing custom up navigation logic,
     * if one was set.
     */
    @Nullable
    public OnNavigateUpListener getFallbackOnNavigateUpListener() {
        return mFallbackOnNavigateUpListener;
    }

    /**
     * The Builder class for constructing new {@link AppBarConfiguration} instances.
     */
    public static final class Builder {
        @NonNull
        private final Set<Integer> mTopLevelDestinations = new HashSet<>();

        @Nullable
        private NavigationView mNavigationView;

        @Nullable
        private OnNavigateUpListener mFallbackOnNavigateUpListener;

        /**
         * Create a new Builder whose only top level destination is the start destination
         * of the given {@link NavGraph}. The Up button will not be displayed when on the
         * start destination of the graph.
         *
         * @param navGraph The NavGraph whose start destination should be considered the only
         *                 top level destination. The Up button will not be displayed when on the
         *                 start destination of the graph.
         */
        public Builder(@NonNull NavGraph navGraph) {
            mTopLevelDestinations.add(NavigationUI.findStartDestination(navGraph).getId());
        }

        /**
         * Create a new Builder using a {@link Menu} containing all top level destinations. It is
         * expected that the {@link MenuItem#getItemId() menu item id} of each item corresponds
         * with a destination in your navigation graph. The Up button will not be displayed when
         * on these destinations.
         *
         * @param topLevelMenu A Menu containing MenuItems corresponding with the destinations
         *                     considered at the top level of your information hierarchy.
         *                     The Up button will not be displayed when on these destinations.
         */
        public Builder(@NonNull Menu topLevelMenu) {
            int size = topLevelMenu.size();
            for (int index = 0; index < size; index++) {
                MenuItem item = topLevelMenu.getItem(index);
                mTopLevelDestinations.add(item.getItemId());
            }
        }

        /**
         * Create a new Builder with a specific set of top level destinations. The Up button will
         * not be displayed when on these destinations.
         *
         * @param topLevelDestinationIds The set of destinations by id considered at the top level
         *                               of your information hierarchy. The Up button will not be
         *                               displayed when on these destinations.
         */
        public Builder(@NonNull int... topLevelDestinationIds) {
            for (int destinationId : topLevelDestinationIds) {
                mTopLevelDestinations.add(destinationId);
            }
        }

        /**
         * Create a new Builder with a specific set of top level destinations. The Up button will
         * not be displayed when on these destinations.
         *
         * @param topLevelDestinationIds The set of destinations by id considered at the top level
         *                               of your information hierarchy. The Up button will not be
         *                               displayed when on these destinations.
         */
        public Builder(@NonNull Set<Integer> topLevelDestinationIds) {
            mTopLevelDestinations.addAll(topLevelDestinationIds);
        }

        @NonNull
        public Builder setNavigationView(@Nullable NavigationView view) {
            mNavigationView = view;
            return this;
        }

        /**
         * Adds a {@link OnNavigateUpListener} that will be called as a fallback if the default
         * behavior of {@link androidx.navigation.NavController#navigateUp}
         * returns <code>false</code>.
         *
         * @param fallbackOnNavigateUpListener Listener that will be invoked if
         *                                     {@link androidx.navigation.NavController#navigateUp}
         *                                     returns <code>false</code>.
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setFallbackOnNavigateUpListener(
                @Nullable OnNavigateUpListener fallbackOnNavigateUpListener) {
            mFallbackOnNavigateUpListener = fallbackOnNavigateUpListener;
            return this;
        }

        /**
         * Construct the {@link AppBarConfiguration} instance.
         *
         * @return a valid {@link AppBarConfiguration}
         */
        @SuppressLint("SyntheticAccessor") /* new AppBarConfiguration() must be private to avoid
                                              conflicting with the public AppBarConfiguration.kt */
        @NonNull
        public AppBarConfiguration build() {
            return new AppBarConfiguration(mTopLevelDestinations, mNavigationView,
                    mFallbackOnNavigateUpListener);
        }
    }
}
