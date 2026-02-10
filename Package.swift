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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STCore.xcframework.zip",
            checksum: "22f3a005d3be907e02dc1635e6f1cac589b97b370ef711b29d444b74e0d4bad1"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STDatabase.xcframework.zip",
            checksum: "8e0fa2377aaa5ba6877ace59362f7d2c95a5b93e40ca95efd485aea6041828dd"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STNetwork.xcframework.zip",
            checksum: "ce55027c9535547c16d5713c4fe8466a1b754d0b492ffd1e77af9b327a61ee26"
        ),
    ]
)
