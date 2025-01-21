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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.1/STCore.xcframework.zip",
            checksum: "0b6bd488501ffa07ee82c691e990df2004d4d7606be335a82caec1b1049095bd"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.1/STDatabase.xcframework.zip",
            checksum: "edef10d04e301872cb524f1d19e97f67995eadc3a8123bc417e0bfdfd52397f1"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.1/STNetwork.xcframework.zip",
            checksum: "ab6eab15e8eafc07b80e88233e556dd2809df1d0b8e4fbbb9c5bcbeab238846a"
        ),
    ]
)
