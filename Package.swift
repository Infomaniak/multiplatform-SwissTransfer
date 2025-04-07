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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.0/STCore.xcframework.zip",
            checksum: "6529c65da9bd2340b4bebb0f529bba1e07a9e02d771cbbec10d0f3502bc5184d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.0/STDatabase.xcframework.zip",
            checksum: "e4e31404829c179238aeb0e1ba358c933db02b3a742d2c3c922650db9bab0cbf"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.1.0/STNetwork.xcframework.zip",
            checksum: "7b2c4e23134a999c7051b392fe52cb119c87013076b91326ec82409410fb1d5c"
        ),
    ]
)
