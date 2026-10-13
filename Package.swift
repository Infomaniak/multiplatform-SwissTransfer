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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.3/STCore.xcframework.zip",
            checksum: "5d1831e8faf3d40e546eece67067c7096084cc8ddc8a8214fad2914b16f2d32d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.3/STDatabase.xcframework.zip",
            checksum: "53ed9e8d9c96800df0d4df600b068721fe70cf1479206d96679a42720af6924c"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.3/STNetwork.xcframework.zip",
            checksum: "1dbabb79ac5a271e59b0af2397e494ddf7ae947dcdc2a82574888895476a5364"
        ),
    ]
)
