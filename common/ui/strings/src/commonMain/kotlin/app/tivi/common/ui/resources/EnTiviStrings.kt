// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnTiviStrings = TiviStrings(
    emptyPrompt = "Y u no watch some TV?",

    emptyEmoji = "😏",

    headerMore = "More",

    popupSortLastWatched = "Last watched",

    popupSortDateFollowed = "Date followed",

    popupSortAlpha = "A-Z",

    popupSortAirDate = "Air date",

    genreLabelDrama = "Drama",

    genreLabelFantasy = "Fantasy",

    genreLabelScienceFiction = "Science Fiction",

    genreLabelAction = "Action",

    genreLabelAdventure = "Adventure",

    genreLabelCrime = "Crime",

    genreLabelThriller = "Thriller",

    genreLabelComedy = "Comedy",

    genreLabelHorror = "Horror",

    genreLabelMystery = "Mystery",

    discoverTitle = "Discover",

    cdDiscoverTitle = "Discover",

    discoverTrendingTitle = "Being watched now",

    discoverRecommendedTitle = "Recommended for you",

    discoverPopularTitle = "Popular",

    discoverKeepWatchingTitle = "Up next",

    followingShowsTitle = "Followed",

    cdFollowingShowsTitle = "@string/following_shows_title",

    watchedShowsTitle = "Watched",

    cdWatchedShowsTitle = "@string/watched_shows_title",

    libraryTitle = "Library",

    cdLibraryTitle = "@string/library_title",

    libraryEmptyTitle = "No shows found",

    libraryEmptyPrompt = "Follow some shows?",

    upnextTitle = "Up Next",

    cdUpnextTitle = "@string/library_title",

    upnextFilterFollowedShowsOnlyTitle = "Followed only",

    upnextEmptyTitle = "No episodes left",

    upnextEmptyPrompt = "You\'ve been busy",

    login = "Login",

    refreshCredentials = "Refresh credentials",

    logout = "Logout",

    showdetailsNavigationTitle = "Show details",

    episodeNavigationTitle = "Episode details",

    searchNavigationTitle = "Search",

    cdSearchNavigationTitle = "@string/search_navigation_title",

    searchHint = "Search for shows",

    searchEmptyTitle = "Enter a query above",

    searchNoresultsTitle = "No results found",

    searchNoresultsPrompt = "Made a typo?",

    accountTitle = "Trakt Account",

    accountNameUnknown = "Hi there!",

    settingsTitle = "Settings",

    settingsUiCategoryTitle = "User Interface",

    settingsThemeTitle = "Theme",

    settingsDynamicColorTitle = "Dynamic colors",

    settingsDynamicColorSummary = "Use colors derived from your wallpaper",

    settingsDataSaverTitle = "Data saver",

    settingsDataSaverSummaryOn = "Fetch data less often and display lower quality images",

    settingsDataSaverSummarySystem = "Automatically enabled due to system setting",

    settingsAboutCategoryTitle = "About Tivi",

    detailsRelated = "Related shows",

    detailsAbout = "About show",

    percentageFormat = { p0 ->
        "%d%%"
            .fmt(p0)
    },

    minutesFormat = { p0 ->
        "%dm"
            .fmt(p0)
    },

    traktRatingTitle = "Trakt Rating",

    traktRatingText = "%.0f%%",

    cdTraktRating = "Trakt Rating: %.0f%%",

    traktRatingVotes = "%.1fk votes",

    cdEpisodeFirstAired = { p0 ->
        "First aired: %s"
            .fmt(p0)
    },

    networkTitle = "Network",

    certificateTitle = "Certificate",

    runtimeTitle = "Length",

    ratingContentDescriptionFormat = { p0 ->
        "Rating: %d%%"
            .fmt(p0)
    },

    networkContentDescriptionFormat = { p0 ->
        "Network: %s"
            .fmt(p0)
    },

    certificateContentDescriptionFormat = { p0 ->
        "Certificate: %s"
            .fmt(p0)
    },

    detailsViewStats = "View stats",

    detailsNextEpisodeToWatch = "Next episode to watch",

    followShowAdd = "Follow show",

    followShowRemove = "Unfollow show",

    cdFollowShowAdd = "@string/follow_show_add",

    cdFollowShowRemove = "@string/follow_show_remove",

    followShowsAdd = "Follow shows",

    followShowsRemove = "Unfollow shows",

    episodeWatches = "Episode watches",

    episodeMarkWatched = "Mark watched",

    episodeAddWatch = "Add episode watch",

    firstAired = { p0 ->
        "First aired: %s"
            .fmt(p0)
    },

    seasonSummaryToWatch = { p0 ->
        "%d to watch"
            .fmt(p0)
    },

    seasonSummaryToAir = { p0 ->
        "%d to air"
            .fmt(p0)
    },

    seasonSummaryWatched = { p0 ->
        "%d watched"
            .fmt(p0)
    },

    popupSeasonWatchNow = "Watch date: now",

    popupSeasonWatchAirDate = "Watch date: air date",

    popupSeasonMarkWatchedAll = "Watch all",

    popupSeasonMarkWatchedAired = "Watch aired",

    popupSeasonMarkAllUnwatched = "Unwatch all",

    popupSeasonFollow = "Follow season",

    popupSeasonIgnore = "Ignore season",

    popupSeasonIgnorePrevious = "Ignore previous seasons",

    showDetailsSeasons = "Seasons",

    viewPrivacyPolicy = "View Privacy Policy",

    notKnownTitle = "TBD",

    libraryLastWatched = { p0 ->
        "Last watched: %s"
            .fmt(p0)
    },

    nextPrefix = { p0 ->
        "Next: %s"
            .fmt(p0)
    },

    filterShows = { p0 ->
        "Filter %d shows"
            .fmt(p0)
    },

    selectionTitle = { p0 ->
        "%d selected"
            .fmt(p0)
    },

    episodeNumber = { p0 ->
        "#%d"
            .fmt(p0)
    },

    seasonEpisodeNumber = { p0, p1 ->
        "Season %d - Episode %d"
            .fmt(p0, p1)
    },

    seasonTitleFallback = { p0 ->
        "Season %d"
            .fmt(p0)
    },

    episodeTitleFallback = { p0 ->
        "Episode %d"
            .fmt(p0)
    },

    followedWatchStatsComplete = "Watched all",

    menuRefresh = "Refresh",

    privacyPolicyUrl = "https://chrisbanes.github.io/tivi/privacypolicy",

    settingsAppVersion = "Version",

    settingsAppVersionSummary = { p0, p1 ->
        "v%s (%d)"
            .fmt(p0, p1)
    },

    settingsOpenSource = "Open source licenses",

    settingsOpenSourceSummary = "Tivi 💞open source",

    errorGeneric = "An error occurred",

    statusTitle = "Status",

    statusEnded = "Ended",

    statusActive = "Continuing",

    statusInProduction = "In production",

    statusPlanned = "Production planned",

    airsTitle = "Airs",

    airsText = { p0, p1 ->
        "%s at %s"
            .fmt(p0, p1)
    },

    episodeRemoveWatchesDialogTitle = "Remove watches",

    episodeRemoveWatchesDialogMessage = "Are you sure that you want to remove all episode watches?",

    episodeRemoveWatchesDialogConfirm = "Remove all",

    dialogDismiss = "Dismiss",

    cdShowPoster = "Poster image",

    cdShowPosterImage = { p0 ->
        "Poster image for %s"
            .fmt(p0)
    },

    cdProfilePic = { p0 ->
        "Profile picture for %s"
            .fmt(p0)
    },

    cdClearText = "Clear text",

    cdRefresh = "Refresh",

    cdSortList = "Sort list",

    cdDelete = "Delete",

    cdNetworkLogo = "Network logo",

    cdUserProfile = "User profile",

    cdNavigateUp = "Navigate up",

    cdOpenOverflow = "Open more",

    cdClose = "Close",

    cdEpisodeSyncing = "Syncing",

    cdEpisodeWatched = "Watched",

    cdEpisodeDeleted = "Removed",

    episodeTrackPrompt = "Finished watching…",

    episodeTrackNow = "Now?",

    episodeTrackSetFirstAired = "Set air date",

    buttonConfirm = "Confirm",

    timeLabel = "Time",

    episodeWatchTimeTitle = "What time did you finish watching?",

    dateLabel = "Date",

    episodeWatchDateTitle = "Date",

    headerShowCount = { quantity ->
        when (quantity) {
            1 -> "%d show"
            else -> "%d shows"
        }.fmt(quantity)
    },

    headerShowCountFiltered = { quantity ->
        when (quantity) {
            1 -> "%d show (filtered)"
            else -> "%d shows (filtered)"
        }.fmt(quantity)
    },

    followedWatchStatsToWatch = { quantity ->
        when (quantity) {
            1 -> "%d episode to watch"
            else -> "%d episodes to watch"
        }.fmt(quantity)
    },
)
