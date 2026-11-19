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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.1.0/STCore.xcframework.zip",
            checksum: "a33aae6f3bbd121bbfc7f73b66cfe7de3369f7ad868e7b5488e194a764e4443b"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.1.0/STDatabase.xcframework.zip",
            checksum: "f504ac2b1a4b687795f1ef2fe77cfcefb70e6cdf25c8e67b2b36f8727cf93cef"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.1.0/STNetwork.xcframework.zip",
            checksum: "b1302536b18ec465ea579d8489cab4003404857bdd68588970e24cf7ceecc718"
        ),
    ]
)
