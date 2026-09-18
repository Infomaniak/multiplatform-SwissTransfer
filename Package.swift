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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.1-202609180831-35324218774-1/STCore.xcframework.zip",
            checksum: "29b52baf04597dd0ec288a384b8747c1c900adfab2769184ff2d70977caeebf7"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.1-202609180831-35324218774-1/STDatabase.xcframework.zip",
            checksum: "1531566bb2222b46b895a27ce1695ce71381ebcdcbd066b6f85369482d33b449"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-11.0.1-202609180831-35324218774-1/STNetwork.xcframework.zip",
            checksum: "f68944eb7dbe78c26ffc060c84f78297e82070bfae47477c38f9cb0294ff6471"
        ),
    ]
)
