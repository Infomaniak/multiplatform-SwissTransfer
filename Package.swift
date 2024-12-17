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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.0/STCore.xcframework.zip",
            checksum: "65e885c688bdf6f4cbfe11807e2a7fc32bde907b5fad1e3258d3eee59898b9df"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.0/STDatabase.xcframework.zip",
            checksum: "7cdad93e941634872913577df8dce0c2a80c735b772443e59b06908d721b3ec4"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.0/STNetwork.xcframework.zip",
            checksum: "ea6d56b9010d732efa7ef9461f5cac5a44c629df081015125dfecff062160892"
        ),
    ]
)
