//
//  TiviApp.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import SwiftUI
import TiviKt

@main
struct TiviApp: App {
    let applicationComponent = IosApplicationComponent.companion.create()
    
    var body: some Scene {
        WindowGroup {
            ContentView(applicationComponent: applicationComponent)
        }
    }
}
