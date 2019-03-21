/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import app.tivi.R;

import java.util.Set;

/**
 * Class which hooks up elements typically in the 'chrome' of your application such as global
 * navigation patterns like a navigation drawer or bottom nav bar with your {@link NavController}.
 */
public final class NavigationUI {

    // No instances. Static utilities only.
    private NavigationUI() {
    }

    /**
     * Attempt to navigate to the {@link NavDestination} associated with the given MenuItem. This
     * MenuItem should have been added via one of the helper methods in this class.
     *
     * <p>Importantly, it assumes the {@link MenuItem#getItemId() menu item id} matches a valid
     * {@link NavDestination#getAction(int) action id} or
     * {@link NavDestination#getId() destination id} to be navigated to.</p>
     * <p>
     * By default, the back stack will be popped back to the navigation graph's start destination.
     * Menu items that have <code>android:menuCategory="secondary"</code> will not pop the back
     * stack.
     *
     * @param item          The selected MenuItem.
     * @param navController The NavController that hosts the destination.
     * @return True if the {@link NavController} was able to navigate to the destination
     * associated with the given MenuItem.
     */
    public static boolean onNavDestinationSelected(@NonNull MenuItem item,
                                                   @NonNull NavController navController) {
        NavOptions.Builder builder = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim);
        if ((item.getOrder() & Menu.CATEGORY_SECONDARY) == 0) {
            builder.setPopUpTo(findStartDestination(navController.getGraph()).getId(), false);
        }
        NavOptions options = builder.build();
        try {
            //TODO provide proper API instead of using Exceptions as Control-Flow.
            navController.navigate(item.getItemId(), null, options);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Handles the Up button by delegating its behavior to the given NavController. This should
     * generally be called from {@link AppCompatActivity#onSupportNavigateUp()}.
     * <p>If you do not have a {@link NavigationView}, you should call
     * {@link NavController#navigateUp()} directly.
     *
     * @param navController  The NavController that hosts your content.
     * @param navigationView The NavigationView that should be opened if you are on the topmost level
     *                       of the app.
     * @return True if the {@link NavController} was able to navigate up.
     */
    public static boolean navigateUp(@NonNull NavController navController,
                                     @Nullable NavigationView navigationView) {
        return navigateUp(navController, new AppBarConfiguration.Builder(navController.getGraph())
                .setNavigationView(navigationView)
                .build());
    }

    /**
     * Handles the Up button by delegating its behavior to the given NavController. This is
     * an alternative to using {@link NavController#navigateUp()} directly when the given
     * {@link AppBarConfiguration} needs to be considered when determining what should happen
     * when the Up button is pressed.
     * <p>
     * In cases where no Up action is available, the
     * {@link AppBarConfiguration#getFallbackOnNavigateUpListener()} will be called to provide
     * additional control.
     *
     * @param navController The NavController that hosts your content.
     * @param configuration Additional configuration options for determining what should happen
     *                      when the Up button is pressed.
     * @return True if the {@link NavController} was able to navigate up.
     */
    public static boolean navigateUp(@NonNull NavController navController,
                                     @NonNull AppBarConfiguration configuration) {
        NavigationView view = configuration.getNavigationView();
        NavDestination currentDestination = navController.getCurrentDestination();
        Set<Integer> topLevelDestinations = configuration.getTopLevelDestinations();
        if (view != null && currentDestination != null
                && matchDestinations(currentDestination, topLevelDestinations)) {
            view.toggle();
            return true;
        } else {
            if (navController.navigateUp()) {
                return true;
            } else if (configuration.getFallbackOnNavigateUpListener() != null) {
                return configuration.getFallbackOnNavigateUpListener().onNavigateUp();
            } else {
                return false;
            }
        }
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link NavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link NavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On all other destinations, the Toolbar will show the Up button. This
     * method will call {@link NavController#navigateUp()} when the Navigation button
     * is clicked.
     *
     * @param toolbar       The Toolbar that should be kept in sync with changes to the NavController.
     * @param navController The NavController that supplies the secondary menu. Navigation actions
     *                      on this NavController will be reflected in the title of the Toolbar.
     * @see #setupWithNavController(Toolbar, NavController, AppBarConfiguration)
     */
    public static void setupWithNavController(@NonNull Toolbar toolbar,
                                              @NonNull NavController navController) {
        setupWithNavController(toolbar, navController,
                new AppBarConfiguration.Builder(navController.getGraph()).build());
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link NavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link NavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On the start destination of your navigation graph, the Toolbar will show
     * the drawer icon if the given NavigationView is non null. On all other destinations,
     * the Toolbar will show the Up button. This method will call
     * {@link #navigateUp(NavController, NavigationView)} when the Navigation button is clicked.
     *
     * @param toolbar       The Toolbar that should be kept in sync with changes to the NavController.
     * @param navController The NavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param view          The NavigationView that should be toggled from the Navigation button
     * @see #setupWithNavController(Toolbar, NavController, AppBarConfiguration)
     */
    public static void setupWithNavController(@NonNull Toolbar toolbar,
                                              @NonNull final NavController navController,
                                              @Nullable final NavigationView view) {
        setupWithNavController(toolbar, navController,
                new AppBarConfiguration.Builder(navController.getGraph())
                        .setNavigationView(view)
                        .build());
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link NavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link NavDestination#getLabel label}).
     *
     * <p>The {@link AppBarConfiguration} you provide controls how the Navigation button is
     * displayed and what action is triggered when the Navigation button is tapped. This method
     * will call {@link #navigateUp(NavController, AppBarConfiguration)} when the Navigation button
     * is clicked.
     *
     * @param toolbar       The Toolbar that should be kept in sync with changes to the NavController.
     * @param navController The NavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param configuration Additional configuration options for customizing the behavior of the
     *                      Toolbar
     */
    public static void setupWithNavController(@NonNull Toolbar toolbar,
                                              @NonNull final NavController navController,
                                              @NonNull final AppBarConfiguration configuration) {
        navController.addOnDestinationChangedListener(
                new ToolbarOnDestinationChangedListener(toolbar, configuration));
        toolbar.setNavigationOnClickListener(v -> navigateUp(navController, configuration));
    }

    /**
     * Determines whether the given <code>destinationIds</code> match the NavDestination. This
     * handles both the default case (the destination's id is in the given ids) and the nested
     * case where the given ids is a parent/grandparent/etc of the destination.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static boolean matchDestinations(@NonNull NavDestination destination,
                                     @NonNull Set<Integer> destinationIds) {
        NavDestination currentDestination = destination;
        do {
            if (destinationIds.contains(currentDestination.getId())) {
                return true;
            }
            currentDestination = currentDestination.getParent();
        } while (currentDestination != null);
        return false;
    }

    /**
     * Finds the actual start destination of the graph, handling cases where the graph's starting
     * destination is itself a NavGraph.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static NavDestination findStartDestination(@NonNull NavGraph graph) {
        NavDestination startDestination = graph;
        while (startDestination instanceof NavGraph) {
            NavGraph parent = (NavGraph) startDestination;
            startDestination = parent.findNode(parent.getStartDestination());
        }
        return startDestination;
    }
}
