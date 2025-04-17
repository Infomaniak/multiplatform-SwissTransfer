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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.1/STCore.xcframework.zip",
            checksum: "66009014642286e8377984e1bc23e16bc9c63290869e6d855134a5b041232d3e"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.1/STDatabase.xcframework.zip",
            checksum: "abaad47a3a90de9519aad074d13a8bec375b8200fb1c7bce7993ad1bdfe2c94f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.1/STNetwork.xcframework.zip",
            checksum: "5cd86daea8445b39251633e6adcb3e26d5aabb8b57dbac8c7cc0d3a4f57e36d0"
        ),
    ]
)
