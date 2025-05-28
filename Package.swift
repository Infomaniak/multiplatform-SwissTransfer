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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.1.0/STCore.xcframework.zip",
            checksum: "d12f9663ff6c89c0f979e9c80cfb610890184a33beac93e89093439721b27063"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.1.0/STDatabase.xcframework.zip",
            checksum: "d691205d25e535821f8a052fde5b2720cf243f803f2345d08406489df781d9b0"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.1.0/STNetwork.xcframework.zip",
            checksum: "621d9f925213bf61199f8abd3631f300441e5bf7ad78bcc2c71310fbd8a952a0"
        ),
    ]
)
