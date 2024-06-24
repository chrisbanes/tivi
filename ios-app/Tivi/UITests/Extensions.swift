//
//  Extensions.swift
//  UITests
//
//  Created by Chris Banes on 24/06/2024.
//

import Foundation
import XCTest

extension NSPredicate {
    static func keyPath<T, U>(
        _ keyPath: KeyPath<T, U>,
        is type: NSComparisonPredicate.Operator = .equalTo,
        value: U,
        modifier: NSComparisonPredicate.Modifier = .direct,
        options: NSComparisonPredicate.Options = []
    ) -> NSPredicate {
        return NSComparisonPredicate(
            leftExpression: NSExpression(forKeyPath: keyPath),
            rightExpression: NSExpression(forConstantValue: value),
            modifier: modifier,
            type: type,
            options: options
        )
    }
}

extension XCUIElement {
    func waitToExistOrThrow(timeout: TimeInterval = 3) -> XCUIElement {
        XCTAssertTrue(
            self.waitForExistence(timeout: timeout),
            "Element did not come to exist within timeout"
        )
        return self
    }

    func navigateFromDiscoverToShowDetails() {
        buttons["trending_carousel_item"]
            .firstMatch
            .waitToExistOrThrow()
            .tap()
    }

    func navigateToUpNext() {
        buttons["home_nav_upnext"]
            .firstMatch
            .waitToExistOrThrow()
            .tap()
    }

    func navigateToLibrary() {
        buttons["home_nav_library"]
            .firstMatch
            .waitToExistOrThrow()
            .tap()
    }

    func navigateToSearch() {
        buttons["home_nav_search"]
            .firstMatch
            .waitToExistOrThrow()
            .tap()
    }
}
