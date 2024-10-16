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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.2.0/STCore.xcframework.zip",
            checksum: "fc435ace02c438d151bef3ee82711b80742156de1e869c46afa1f0dac69eee37"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.2.0/STDatabase.xcframework.zip",
            checksum: "c67e99a89abce614d28283c197fdd882c81a0611826453d5797b5925f3283e6b"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.2.0/STNetwork.xcframework.zip",
            checksum: "647748c275771da7627172bd0b85234c1a13b8317fb0bad55047b98f575e1279"
        ),
    ]
)
