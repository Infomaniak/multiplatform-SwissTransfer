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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.0-organizations-202608280747-33152367973-1/STCore.xcframework.zip",
            checksum: "65a87966ca3b45094a391db986b9bc16289ab32e67cd491a599f86d1a0a1c2ce"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.0-organizations-202608280747-33152367973-1/STDatabase.xcframework.zip",
            checksum: "9f0ad5941cfba41becd901678cb17a050b9c35fdda4b4a3e512d3abff346f7ea"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.0-organizations-202608280747-33152367973-1/STNetwork.xcframework.zip",
            checksum: "9a2b8cab01ef1080de398d7663b10dd23433680fe1060265048a55922e4b6742"
        ),
    ]
)
