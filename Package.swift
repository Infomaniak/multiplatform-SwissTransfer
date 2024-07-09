// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "Core", targets: ["Core"]),
        .library(name: "Database", targets: ["Database"]),
        .library(name: "Network", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            path: "Core.xcframework.zip"
        ),
        .binaryTarget(
            name: "Database",
            path: "Database.xcframework.zip"
        ),
        .binaryTarget(
            name: "Network",
            path: "Network.xcframework.zip"
        ),
    ]
)
