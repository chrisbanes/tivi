//
//  UITestsLaunchTests.swift
//  UITests
//
//  Created by Chris Banes on 22/06/2024.
//

import XCTest

@MainActor
static class ScreenshotsTests: XCTestCase {

    override class func setUp() {
        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()
    }

    func testScreenshots() throws {
        let app = XCUIApplication()
        snapshot("1_Home")

        app.navigateFromDiscoverToShowDetails()
        snapshot("2_ShowDetails")

        app.navigateToUpNext()
        snapshot("3_UpNext")

        app.navigateToLibrary()
        snapshot("4_Library")

        app.navigateToSearch()
        snapshot("5_Search")
    }

}
