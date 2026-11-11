// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.2-202611110921-42993279902-1/STCore.xcframework.zip",
            checksum: "ea88c25009185a3652c46f9f0070e78a959f561889073068b8cfff33d67b1a18"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.2-202611110921-42993279902-1/STDatabase.xcframework.zip",
            checksum: "ec303a6f6e40798d0482a79fda4e46339d4f6d6f6ff8f6e3f655de53ae39cd28"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.2-202611110921-42993279902-1/STNetwork.xcframework.zip",
            checksum: "69644dccf4f1573690a4f26e13e7bfe8e8a687688d7af6959d11103cfa0b218e"
        ),
    ]
)
