//
//  UITestsLaunchTests.swift
//  UITests
//
//  Created by Chris Banes on 22/06/2024.
//

import XCTest

class ScreenshotsTests: XCTestCase {

    @MainActor
    override class func setUp() {
        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()
    }

    @MainActor
    func testScreenshots() throws {
        let app = XCUIApplication()

        Thread.sleep(forTimeInterval: 4)
        snapshot("1_Home")

        app.navigateFromDiscoverToShowDetails()
        Thread.sleep(forTimeInterval: 1)
        snapshot("2_ShowDetails")

        app.navigateToUpNext()
        Thread.sleep(forTimeInterval: 1)
        snapshot("3_UpNext")

        app.navigateToLibrary()
        Thread.sleep(forTimeInterval: 1)
        snapshot("4_Library")

        app.navigateToSearch()
        Thread.sleep(forTimeInterval: 1)
        snapshot("5_Search")
    }

}
