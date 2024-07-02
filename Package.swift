// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "Core", targets: ["Shared"]),
        .library(name: "DB", targets: ["DB"]),
        .library(name: "Network", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            path: "Core.xcframework.zip"
        ),
        .binaryTarget(
            name: "DB",
            path: "DB.xcframework.zip"
        ),
        .binaryTarget(
            name: "Network",
            path: "Network.xcframework.zip"
        ),
    ]
)
