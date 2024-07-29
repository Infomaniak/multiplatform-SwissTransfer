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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/Core.xcframework.zip",
            checksum: "48545b599b8a7903fdb416baf921a4b56f734ac77323bba881688e330cbbeec3"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/Database.xcframework.zip",
            checksum: "899e2417472c5683eee8bb5fd8fd60e5d8cf820e87f1831eec6e38c1f4057608"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/Network.xcframework.zip",
            checksum: "9a667005fce83fe45ac46b5e8e405155ce803e7cb411f77000f105d95512e514"
        ),
    ]
)
